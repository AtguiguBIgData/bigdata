package com.atguigu.statisticsRecommender

import java.text.{DecimalFormat}

import com.atguigu.commons.{ConfigManager, Constants}
import com.atguigu.scala.model._
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}

object StatisticsRecommender{

  def main(args: Array[String]): Unit = {

    var sparkMaster = ConfigManager.getSparkAddress

    if (args.length == 1) {
      sparkMaster = args(0)
    }

    val params = scala.collection.mutable.Map[String, String]()
    params += Constants.SPARK_MASTER -> sparkMaster

    params += Constants.JDBC_URL -> ConfigManager.getMySqlUrl
    params += Constants.JDBC_USER -> ConfigManager.getMySqlUsername
    params += Constants.JDBC_PASSWORD -> ConfigManager.getMySqlPassword

    // 定义MySqlDB的配置对象
    implicit val mySqlConfig = new MySqlConfig(params(Constants.JDBC_URL), params(Constants.JDBC_USER), params(Constants.JDBC_PASSWORD))

    val conf = new SparkConf().setAppName("statisticsRecommender").setMaster(params(Constants.SPARK_MASTER)).set("spark.executor.memory","4G")

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    import spark.implicits._

    val ratings = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATING)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .as[MovieRating]
      .cache

    val movies = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .as[Movie]
      .cache

    movies.createOrReplaceTempView("movies")
    ratings.createOrReplaceTempView("ratings")

    val df = new DecimalFormat("#.00")
    spark.udf.register("format", (x: Double) => df.format(x).toDouble)

    // 电影平均分 + Genres Top10
    averageMovieScore(spark)(movies)

    // 统计优质电影
    rateMore(spark)

    // 统计最近最火的电影
    rateMoreRecently(spark)

    ratings.unpersist()

    spark.close()
  }

  /**
    * 统计优质电影
    */
  def rateMore(spark: SparkSession)(implicit mySqlConfig: MySqlConfig): Unit = {

    //统计优质电影
    val rateMoreMoviesDF = spark.sql("SELECT " +
      "average.mid, " +
      "format(average.avg) avg, " +
      "average.count, " +
      "row_number() over(order by average.avg desc,average.count desc) rank " +
      " FROM " +
      " (SELECT mid, avg(score) avg, count(*) count FROM ratings GROUP BY mid) average WHERE avg > 3.5 AND count > 50")

    rateMoreMoviesDF
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_RATE_MORE_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

  }

  /**
    * 最热电影统计 三个月
    */
  def rateMoreRecently(spark: SparkSession)(implicit mySqlConfig: MySqlConfig): Unit = {

    val hotMovies = spark.sql("select " +
      "mid, " +
      "count(*) count  " +
      "from  ratings " +
      "where timestamp > (select max(timestamp)-7776000 max from ratings) " +
      "group by mid " +
      "order by count desc " +
      "limit 10")

    hotMovies
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_RATE_MORE_RECENTLY_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }

  /**
    * 电影平均评分 + TOP10 Genres
    */
  def averageMovieScore(spark: SparkSession)(movies: Dataset[Movie])(implicit mySqlConfig: MySqlConfig): Unit = {

    // 统计电影的平均评分
    val movieAverageScoreDF = spark.sql("select mid, format(avg(score)) avg from ratings group by mid")

    movieAverageScoreDF.createOrReplaceTempView("averageMovies")

    movieAverageScoreDF
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_AVERAGE_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()


    // 统计每个类别的TOP10电影
    import org.apache.spark.sql.functions._

    val movieWithScore = spark.sql("select a.mid, genres, if(isnull(b.avg),0,b.avg) score from movies a left join averageMovies b on a.mid = b.mid")

    movieWithScore.createOrReplaceTempView("movieWithScore")

    spark.udf.register("splitGe",(genres:String) => {
      genres.split("\\|")
    })

    val genresTop10Movies = spark.sql("select * from (select " +
      "mid," +
      "gen," +
      "score, " +
      "row_number() over(partition by gen order by score desc) rank " +
      "from " +
      "(select mid,score,explode(splitGe(genres)) gen from movieWithScore) genresMovies) rankGenresMovies " +
      "where rank <= 10")

    genresTop10Movies.write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_GENRES_TOP_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }
}
