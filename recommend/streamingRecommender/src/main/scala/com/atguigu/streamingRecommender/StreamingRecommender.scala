package com.atguigu.streamingRecommender

import java.sql.ResultSet

import com.atguigu.scala.model.MySqlConfig
import com.atguigu.scala.pool.{CreateMySqlPool, CreateRedisPool, MySqlProxy, QueryCallback}
import com.atguigu.commons.{ConfigManager, Constants}
import kafka.api.{OffsetRequest, PartitionOffsetRequestInfo, TopicMetadataRequest}
import kafka.common.TopicAndPartition
import kafka.consumer.SimpleConsumer
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

import scala.collection.JavaConversions._
import scala.collection.mutable

object StreamingRecommender {

  val USER_RECENTLY_RATING_COUNT = 50
  val SIMILAR_MOVIE_COUNT = 50
  val MAX_REC_COUNT = 20

  private val minSimilarity = 0.7

  /**
    * 从Redis中获取当前用户最近K次评分，返回 Buffer[(Int,Double)]
    *
    * @param K       获取当前用户K次评分
    * @param userId  用户ID
    * @param movieId 新评分的电影ID
    * @param score   新评分的电影评分
    * @return 最近K次评分的数组，每一项是<movieId, score>
    */
  def getUserRecentRatings(redisClient: Jedis, K: Int, userId: Int, movieId: Int, score: Double): Array[(Int, Double)] = {
    redisClient.lrange("uid:" + userId.toString, 0, K).map { line =>
      val attr = line.asInstanceOf[String].split(":")
      (attr(0).toInt, attr(1).toDouble)
    }.toArray
  }

  /**
    * 从广播变量中获取movieId最相似的K个电影，并通过MONGODB来过滤掉已被评分的电影
    *
    * @param K
    * @return 返回相似的电影ID数组
    */
  def getSimilarMovies(redisClient: Jedis, mysqlClient: MySqlProxy, mid: Int, uid: Int, K: Int): Array[Int] = {

    val similarMoviesBeforeFilter = redisClient.smembers("set:" + mid).map { line =>
      val attr = line.split(":")
      attr(0).toInt
    }.take(K)

    val querySQL = "SELECT mid FROM " + Constants.DB_RATING + " WHERE uid=?"
    val params = Array[Any](uid)

    val ratedMovies = mutable.ArrayBuffer[Int]()

    mysqlClient.executeQuery(querySQL, params, new QueryCallback {
      override def process(rs: ResultSet): Unit = {
        while (rs.next) {
          ratedMovies.add(rs.getString("mid").toInt)
        }
      }
    })
    similarMoviesBeforeFilter.filter(!ratedMovies.contains(_)).toArray
  }

  /**
    * 从Redis中获取movieId1与movieId2的相似度，不存在、或movieId1=movieId2视为毫无相似，相似度为0
    *
    * @param movieId1
    * @param movieId2
    * @return movieId1与movieId2的相似度
    */
  def getSimilarityBetween2Movies(redisClient: Jedis, movieId1: Int, movieId2: Int): Double = {

    val (smallerId, biggerId) = if (movieId1 < movieId2) (movieId1, movieId2) else (movieId2, movieId1)
    if (smallerId == biggerId) {
      return 0.0
    }

    if (redisClient.hexists("map:" + movieId1, movieId2.toString)) {
      redisClient.hget("map:" + movieId1, movieId2.toString).toDouble
    } else
      0.0
  }

  def log(m: Double): Double = math.log(m) / math.log(2)

  /**
    * 核心算法，计算每个备选电影的预期评分
    *
    * @param recentRatings   用户最近评分的K个电影集合
    * @param candidateMovies 当前评分的电影的备选电影集合
    * @return 备选电影预计评分的数组，每一项是<movieId, maybe_rate>
    */
  def createUpdatedRatings(redisClient: Jedis, recentRatings: Array[(Int, Double)], candidateMovies: Array[Int]): Array[(Int, Double)] = {

    val allSimilars = mutable.ArrayBuffer[(Int, Double)]()

    val increaseCounter = mutable.Map[Int, Int]()
    val reduceCounter = mutable.Map[Int, Int]()

    for (cmovieId <- candidateMovies; (rmovieId, rate) <- recentRatings) {
      val sim = getSimilarityBetween2Movies(redisClient, rmovieId, cmovieId)
      if (sim > minSimilarity) {
        allSimilars += ((cmovieId, sim * rate))
        if (rate >= 3.0) {
          increaseCounter(cmovieId) = increaseCounter.getOrElse(cmovieId, 0) + 1
        } else {
          reduceCounter(cmovieId) = reduceCounter.getOrElse(cmovieId, 0) + 1
        }
      }
    }
    allSimilars.toArray.groupBy { case (movieId, value) => movieId }
      .map { case (movieId, simArray) =>
        (movieId, simArray.map(_._2).sum / simArray.length + log(increaseCounter.getOrElse[Int](movieId, 1)) - log(reduceCounter.getOrElse[Int](movieId, 1)))
      }.toArray
  }

  /**
    * 将备选电影的预期评分合并后回写到DB中
    *
    * @param newRecommends
    * @param startTimeMillis
    * @return
    */
  def updateRecommends2DB(mysqlClient: MySqlProxy, newRecommends: Array[(Int, Double)], uid: Int, startTimeMillis: Long): Unit = {

    val querySQL = "SELECT recs FROM " + Constants.DB_STREAM_RECS + " WHERE uid=?"
    val params = Array[Any](uid)

    var lastTimeRecs = mutable.ArrayBuffer[(Int,Double)]()

    mysqlClient.executeQuery(querySQL, params, new QueryCallback {
      override def process(rs: ResultSet): Unit = {
        if (rs.next) {
          rs.getString("recs").split("\\|").map{item=>
            val attr = item.split(":")
            lastTimeRecs.add((attr(0).toInt, attr(1).toDouble))
          }
        }
      }
    })

    lastTimeRecs.addAll(newRecommends.toIterable)
    val newRecs = lastTimeRecs.toList.sortWith(_._2 < _._2).slice(0, MAX_REC_COUNT).toArray
      .map(item=> item._1+":"+item._2).mkString("|")

    val updateSQL = "UPDATE "+Constants.DB_STREAM_RECS+" SET recs=? WHERE uid=? "
    val updateParams = Array[Any](newRecs,uid)
    mysqlClient.executeUpdate(updateSQL, updateParams)

  }

  def createKafkaStream(ssc: StreamingContext, brokers:String, topics:String, zookeeper:String) = {

    // kafka消费者配置
    val kafkaParam = Map[String,String](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,//用于初始化链接到集群的地址
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      //用于标识这个消费者属于哪个消费团体
      ConsumerConfig.GROUP_ID_CONFIG -> "recommend",
      //如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "largest"
    )

    // 声明Kafka保存在Zookeeper上的路径对象
    val topicDirs = new ZKGroupTopicDirs("recommend",topics)
    val zkTopicPath = s"${topicDirs.consumerOffsetDir}"

    // 创建一个Zookeeper的连接
    val zkClient = new ZkClient(zookeeper)

    // 获取偏移的保存目录下的子节点
    val children = zkClient.countChildren(zkTopicPath)

    // 生成的Kafka连接实例
    var adRealTimeLogDStream :InputDStream[(String,String)] = null

    // 如果子节点存在，则说明有偏移值保存
    if(children > 0){

      // 开始消费的偏移量
      var fromOffsets: Map[TopicAndPartition, Long] = Map()

      //---首先获取每一个Partition的主节点信息----
      val topicList = List(topics)
      val request = new TopicMetadataRequest(topicList,0)  //得到该topic的一些信息，比如broker,partition分布情况
      val getLeaderConsumer = new SimpleConsumer("linux",9092,10000,10000,"OffsetLookup") // low level api interface
      val response = getLeaderConsumer.send(request)  //TopicMetadataRequest   topic broker partition 的一些信息
      val topicMetaOption = response.topicsMetadata.headOption
      val partitions = topicMetaOption match{
        case Some(tm) =>
          tm.partitionsMetadata.map(pm=>(pm.partitionId,pm.leader.get.host)).toMap[Int,String]
        case None =>
          Map[Int,String]()
      }
      getLeaderConsumer.close()
      println("partitions information is: "+ partitions)
      println("children information is: "+ children)


      for (i <- 0 until children) {

        val partitionOffset = zkClient.readData[String](s"${topicDirs.consumerOffsetDir}/${i}")
        println(s"Partition【${i}】 目前的偏移值是:${partitionOffset}")

        val tp = TopicAndPartition(topics, i)
        //---获取当前Partition的最小偏移值【主要防止Kafka中的数据过期】-----
        val requestMin = OffsetRequest(Map(tp -> PartitionOffsetRequestInfo(OffsetRequest.EarliestTime,1)))  // -2,1
        val consumerMin = new SimpleConsumer(partitions(i),9092,10000,10000,"getMinOffset")

        val curOffsets = consumerMin.getOffsetsBefore(requestMin).partitionErrorAndOffsets(tp).offsets

        consumerMin.close()
        var nextOffset = partitionOffset.toLong
        if(curOffsets.length >0 && nextOffset < curOffsets.head){  //如果下一个offset小于当前的offset
          nextOffset = curOffsets.head
        }
        //---additional end-----
        println(s"Partition【${i}】 修正后的偏移值是:${nextOffset}")

        fromOffsets += (tp -> nextOffset)
      }
      val messageHandler = (mmd : MessageAndMetadata[String, String]) => (mmd.topic, mmd.message())
      println("从Zookeeper获取偏移量创建DStream")
      zkClient.close()
      adRealTimeLogDStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParam, fromOffsets, messageHandler)

    }else{
      println("直接创建DStream")
      adRealTimeLogDStream=KafkaUtils.createDirectStream[String,String,StringDecoder, StringDecoder](ssc,kafkaParam,Set(topics))
    }
    adRealTimeLogDStream
  }

  def main(args: Array[String]) {

    var sparkMaster = ConfigManager.getSparkAddress

    if (args.length == 1) {
      sparkMaster = args(0)
    }

    //创建全局配置
    val params = scala.collection.mutable.Map[String, String]()
    params += Constants.SPARK_MASTER -> sparkMaster

    params += Constants.JDBC_URL -> ConfigManager.getMySqlUrl
    params += Constants.JDBC_USER -> ConfigManager.getMySqlUsername
    params += Constants.JDBC_PASSWORD -> ConfigManager.getMySqlPassword

    // 定义MySqlDB的配置对象
    implicit val mySqlConfig = new MySqlConfig(params(Constants.JDBC_URL), params(Constants.JDBC_USER), params(Constants.JDBC_PASSWORD))

    val sparkConf = new SparkConf().setAppName("streamingRecommendingSystem")
      .setMaster(params(Constants.SPARK_MASTER))
      .set("spark.executor.memory", "4g")

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(5))


    // 设置检查点目录
    ssc.checkpoint("./streaming_checkpoint")

    // 获取Kafka配置
    val broker_list = ConfigManager.getKafkaBrokers
    val topics = ConfigManager.getKafkaRecommendToTopic
    val zookeeper = ConfigManager.getZookeeper

    var adRealTimeLogDStream = createKafkaStream(ssc,broker_list,topics,zookeeper)

    adRealTimeLogDStream.foreachRDD(rdd => {

      // 获取Offset
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      rdd.map{ case (key,value) =>
        val dataArr: Array[String] = value.split("\\|")
        val userId = dataArr(0).toInt
        val movieId = dataArr(1).toInt
        val score = dataArr(2).toDouble
        val timestamp = dataArr(3).toLong
        (userId, movieId, score, timestamp)
      }.map { case (userId, movieId, score, timestamp) =>

        val redisPool = CreateRedisPool(ConfigManager.getRedisHost)
        val redisClient = redisPool.borrowObject()

        val mysqlPool = CreateMySqlPool(ConfigManager.getMySqlUrl, ConfigManager.getMySqlUsername, ConfigManager.getMySqlPassword)
        val mysqlClient = mysqlPool.borrowObject()

        //从Redis中获取当前用户近期K次评分记录
        val recentRatings = getUserRecentRatings(redisClient, USER_RECENTLY_RATING_COUNT, userId, movieId, score)

        //获取当前评分电影相似的K个备选电影
        val candidateMovies = getSimilarMovies(redisClient, mysqlClient, movieId, userId, SIMILAR_MOVIE_COUNT)

        //为备选电影推测评分结果
        val updatedRecommends = createUpdatedRatings(redisClient, recentRatings, candidateMovies)

        //当前推荐与往期推荐进行Merge结果回写到MySQL
        updateRecommends2DB(mysqlClient, updatedRecommends, userId, timestamp)

        redisPool.returnObject(redisClient)
        mysqlPool.returnObject(mysqlClient)

      }.count()

      // 更新Zookeeper中的偏移值
      val updateTopicDirs = new ZKGroupTopicDirs("recommend",topics)
      val updateZkClient = new ZkClient(zookeeper)
      for(offset <- offsetRanges ){
        println(offset)
        val zkPath = s"${updateTopicDirs.consumerOffsetDir}/${offset.partition}"
        ZkUtils.updatePersistentPath(updateZkClient,zkPath,offset.fromOffset.toString)
      }
      updateZkClient.close()

    })

    ssc.start()
    ssc.awaitTermination()
  }
}

