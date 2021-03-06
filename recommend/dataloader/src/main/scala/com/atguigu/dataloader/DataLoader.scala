package com.atguigu.dataloader

import java.net.InetAddress

import com.atguigu.commons.{ConfigManager, Constants}
import com.atguigu.scala.model._
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient

object DataLoader {

  /**
    * Store Data In ElasticSearch
    *
    * @param movies 电影数据集
    * @param esConf ElasticSearch的配置对象
    */
  private def storeMoiveDataInES(movies: DataFrame)(implicit esConf: ESConfig): Unit = {

    // 需要操作的Index名称
    val indexName = esConf.index

    // 新建一个到ES的连接配置
    var settings: Settings = Settings.builder().put("cluster.name", esConf.clustername).build()

    // 创建到ES的连接客户端
    val esClient = new PreBuiltTransportClient(settings)

    //对于设定的多个Node分别通过正则表达式进行模式匹配，并添加到客户端实例

    // 配置主机名:端口号的正则表达式
    val ES_HOST_PORT_REGEX = "(.+):(\\d+)".r

    esConf.transportHosts.split(";")
      .foreach {
        case ES_HOST_PORT_REGEX(host: String, port: String) =>
          esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port.toInt))
      }

    // 检查如果Index存在，那么删除Index
    if (esClient.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().isExists) {
      // 删除Index
      esClient.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet()
    }
    // 创建Index
    esClient.admin().indices().create(new CreateIndexRequest(indexName)).actionGet()

    // 声明写出时的ES配置信息
    val movieOptions = Map("es.nodes" -> esConf.httpHosts,
      "es.http.timeout" -> "100m",
      "es.mapping.id" -> "mid")

    // 电影数据写出时的Type名称【表】
    val movieTypeName = indexName + "/" + Constants.ES_TYPE

    // 将Movie信息保存到ES
    movies
      .write.options(movieOptions)
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(movieTypeName)
  }

  /**
    * Store Data In MySQL
    *
    * @param movies      电影数据集
    * @param ratings     评分数据集
    * @param tags        标签数据集
    * @param mySqlConfig MySql的配置
    */
  private def storeDataInMySQL(movies: DataFrame, ratings: DataFrame, tags: DataFrame)(implicit mySqlConfig: MySqlConfig): Unit = {

    //将Movie数据集写入到MySql
    movies.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    //将Rating数据集写入到MySql
    ratings.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATING)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    //将Tag数据集写入到MySql
    tags.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_TAG)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }

  def main(args: Array[String]): Unit = {

    var sparkMaster = ConfigManager.getSparkAddress

    // [mid,name,descri,timelong,issue,shoot,language,genres,actors,directors]
    var DATAFILE_MOVIES = ConfigManager.getDataFileMovies

    // [userId,movieId,rating,timestamp]
    var DATAFILE_RATINGS = ConfigManager.getDataFileRatings

    // [userId,movieId,tag,timestamp]
    var DATAFILE_TAGS = ConfigManager.getDataFileTags

    if (args.length == 4) {
      sparkMaster = args(0)
      DATAFILE_MOVIES = args(1)
      DATAFILE_RATINGS = args(2)
      DATAFILE_TAGS = args(3)
    }

    //创建全局配置
    val params = scala.collection.mutable.Map[String, String]()
    params += Constants.SPARK_MASTER -> sparkMaster

    params += Constants.JDBC_URL -> ConfigManager.getMySqlUrl
    params += Constants.JDBC_USER -> ConfigManager.getMySqlUsername
    params += Constants.JDBC_PASSWORD -> ConfigManager.getMySqlPassword

    params += Constants.ES_HTTPHOSTS -> ConfigManager.getEsHttpHosts
    params += Constants.ES_TRANSPORTHOSTS -> ConfigManager.getEsTransportHosts
    params += Constants.ES_INDEX_NAME -> ConfigManager.getEsIndexName
    params += Constants.ES_CLUSTER_NAME -> ConfigManager.getEsClusterName

    // 定义MySqlDB的配置对象
    implicit val mysqlConf = new MySqlConfig(params(Constants.JDBC_URL), params(Constants.JDBC_USER), params(Constants.JDBC_PASSWORD))

    // 定义ElasticSearch的配置对象
    implicit val esConf = new ESConfig(params(Constants.ES_HTTPHOSTS), params(Constants.ES_TRANSPORTHOSTS), params(Constants.ES_INDEX_NAME), params(Constants.ES_CLUSTER_NAME))

    // 声明Spark的配置信息
    val conf = new SparkConf().setAppName("Dataloader").setMaster(params(Constants.SPARK_MASTER))

    // 创建SparkSession
    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    // 引入SparkSession内部的隐式转换
    import spark.implicits._

    // 加载Movie数据集
    val movieRDD = spark.sparkContext.textFile(DATAFILE_MOVIES)

    // 加载Rating数据集
    val ratingRDD = spark.sparkContext.textFile(DATAFILE_RATINGS)

    // 加载Tag数据集
    val tagRDD = spark.sparkContext.textFile(DATAFILE_TAGS)

    // 将电影RDD转换为DataFrame
    val movieDF = movieRDD.map(line => {
      val x = line.split("\\^")
      Movie(x(0).trim.toInt, x(1).trim, x(2).trim, x(3).trim, x(4).trim, x(5).trim, x(6).trim, x(7).trim, x(8).trim, x(9).trim)
    }).toDF()

    // 将评分RDD转换为DataFrame
    val ratingDF = ratingRDD.map(line => {
      val x = line.split(",")
      MovieRating(x(0).toInt, x(1).toInt, x(2).toDouble, x(3).toInt)
    }).toDF()

    // 将标签RDD转换为DataFrame
    val tagDF = tagRDD.map(line => {
      val x = line.split(",")
      Tag(x(0).toInt, x(1).toInt, x(2).toString, x(3).toInt)
    }).toDF()

    //缓存
    movieDF.cache()
    tagDF.cache()

    // 将数据保存到MongoDB
    storeDataInMySQL(movieDF, ratingDF, tagDF)

    //引入内置函数库
    import org.apache.spark.sql.functions._

    //将tagDF中的标签合并在一起
    val tagCollectDF = tagDF.groupBy($"mid").agg(concat_ws("|", collect_set($"tag")).as("tags"))

    //将tags合并到movie数据集中产生新的movie数据集
    val esMovieDF = movieDF.join(tagCollectDF, Seq("mid", "mid"), "left").select("mid", "name", "descri", "timelong", "issue", "shoot", "language", "genres", "actors", "directors", "tags")

    // 将数据保存到ES
    storeMoiveDataInES(esMovieDF)

    //去除缓存
    tagDF.unpersist()
    movieDF.unpersist()

    //关闭Spark
    spark.close()

  }

}
