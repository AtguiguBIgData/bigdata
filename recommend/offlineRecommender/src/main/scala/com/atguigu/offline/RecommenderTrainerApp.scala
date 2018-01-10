package com.atguigu.offline

import java.text.DecimalFormat

import com.atguigu.commons.{ConfigManager, Constants}
import com.atguigu.commons.model.{MovieRecs, MySqlConfig, Recommendation, UserRecs}
import com.atguigu.commons.pool.CreateRedisPool
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import org.jblas.DoubleMatrix

import scala.collection.JavaConversions._

/**
  * @author wuyufei
  */
object RecommenderTrainerApp {

  val df = new DecimalFormat("#.00")

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

    implicit val mySqlConfig = new MySqlConfig(params(Constants.JDBC_URL), params(Constants.JDBC_USER), params(Constants.JDBC_PASSWORD))

    val conf = new SparkConf().setAppName("RecommenderTrainerApp").setMaster(params(Constants.SPARK_MASTER)).set("spark.executor.memory", "6G")

    // 创建SparkSession
    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    calculateRecs(spark, Constants.MAX_RECOMMENDATIONS)

    spark.close()

  }

  /**
    * 计算推荐数据
    *
    * @param maxRecs
    */
  def calculateRecs(spark: SparkSession, maxRecs: Int)(implicit mySqlConfig: MySqlConfig): Unit = {

    import spark.implicits._

    val ratings = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATING)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .select($"mid", $"uid", $"score")
      .cache

    val users = ratings
      .select($"uid")
      .distinct
      .map(r => r.getAs[Int]("uid"))
      .cache

    val movies = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .select($"mid")
      .distinct
      .map(r => r.getAs[Int]("mid"))
      .cache

    val trainData = ratings.map { line =>
      Rating(line.getAs[Int]("uid"), line.getAs[Int]("mid"), line.getAs[Double]("score"))
    }.rdd.cache()

    val (rank, iterations, lambda) = (50, 5, 0.01)
    val model = ALS.train(trainData, rank, iterations, lambda)

    calculateUserRecs(maxRecs, model, users, movies)
    calculateProductRecs(maxRecs, model, movies)

    ratings.unpersist()
    users.unpersist()
    movies.unpersist()
    trainData.unpersist()

  }


  /**
    * 计算为用户推荐的电影集合矩阵 RDD[UserRecommendation(id: Int, recs: Seq[Rating])]
    *
    * @param maxRecs
    * @param model
    * @param users
    * @param products
    */
  private def calculateUserRecs(maxRecs: Int, model: MatrixFactorizationModel, users: Dataset[Int], products: Dataset[Int])(implicit mySqlConfig: MySqlConfig): Unit = {

    import users.sparkSession.implicits._

    val userProductsJoin = users.crossJoin(products)

    val userRating = userProductsJoin.map { row => (row.getAs[Int](0), row.getAs[Int](1)) }.rdd

    object RatingOrder extends Ordering[Rating] {
      def compare(x: Rating, y: Rating) = y.rating compare x.rating
    }

    val recommendations = model.predict(userRating)
      .filter(_.rating > 0)
      .groupBy(p => p.user)
      .map { case (uid, predictions) =>
        val recommendations = predictions.toSeq.sorted(RatingOrder)
          .take(maxRecs)
          .map(p => Recommendation(p.product, df.format(p.rating).toDouble))

        UserRecs(uid, recommendations.mkString("|"))
      }.toDF()

    recommendations.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_USER_RECS)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }


  private def calculateProductRecs(maxRecs: Int, model: MatrixFactorizationModel, products: Dataset[Int])(implicit mySqlConfig: MySqlConfig): Unit = {

    import products.sparkSession.implicits._

    object RatingOrder extends Ordering[(Int, Int, Double)] {
      def compare(x: (Int, Int, Double), y: (Int, Int, Double)) = y._3 compare x._3
    }

    val productsVectorRdd = model.productFeatures
      .map { case (movieId, factor) =>
        val factorVector = new DoubleMatrix(factor)
        (movieId, factorVector)
      }

    val minSimilarity = 0.6

    val movieRecommendation = productsVectorRdd.cartesian(productsVectorRdd)
      .filter { case ((movieId1, vector1), (movieId2, vector2)) => movieId1 != movieId2 }
      .map { case ((movieId1, vector1), (movieId2, vector2)) =>
        val sim = cosineSimilarity(vector1, vector2)
        (movieId1, movieId2, sim)
      }.filter(_._3 >= minSimilarity)
      .groupBy(p => p._1)

      .map { case (mid: Int, predictions: Iterable[(Int, Int, Double)]) =>

        val sortedRecs = predictions.toSeq.sorted(RatingOrder)

        // Update Redis
        val redisPool = CreateRedisPool(ConfigManager.getRedisHost)
        val client = redisPool.borrowObject()

        // 插入50个最相似的电影
        client.del("set:"+mid)
        client.sadd("set:"+mid, sortedRecs.take(maxRecs).map(item => item._2 + ":" + item._3):_*)

        // 插入map
        client.del("map:"+mid)
        client.hmset("map:"+mid, sortedRecs.map(item => (item._2.toString,item._3.toString)).toMap)

        redisPool.returnObject(client)

        val recommendations = sortedRecs.map(p => Recommendation(p._2, df.format(p._3).toDouble))
        MovieRecs(mid, recommendations.mkString("|"))
      }.toDF().cache()

    // 将数据插入到MySQL
    movieRecommendation.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE_RECS)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    movieRecommendation.unpersist()
  }

  /**
    * 余弦相似度
    *
    * @param vec1
    * @param vec2
    * @return
    */
  private def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }

}
