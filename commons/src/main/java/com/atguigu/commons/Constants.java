package com.atguigu.commons;

public class Constants {

    public static final String ADMIN = "admin";

    //************* Platform  **************
    public static final String BIGDATA_HOME = "BIGDATA_HOME";

    public static final String BIGDATA_CONF_DIR = "/conf";


    //****************  属性信息  *******************
    public static final String POOL_SIZE = "pool.size";
    public static final String JDBC_URL = "jdbc.url";
    public static final String JDBC_USER = "jdbc.user";
    public static final String JDBC_PASSWORD = "jdbc.password";

    public static final String ES_HTTPHOSTS = "es.httpHosts";
    public static final String ES_TRANSPORTHOSTS = "es.transportHosts";
    public static final String ES_CLUSTER_NAME = "es.cluster.name";
    public static final String ES_INDEX_NAME = "es.index.name";

    public static final String REDIS_HOST = "redis.host";
    public static final String REDIS_PORT = "redis.port";

    public static final String KAFKA_BROKERS = "kafka.brokers";
    public static final String KAFKA_ZOOKEEPER = "kafka.zookeeper";
    public static final String KAFKA_FROM_TOPIC = "kafka.from.topic";
    public static final String KAFKA_TO_TOPIC = "kafka.to.topic";


    //*****************  MySQL中的表名  ***************

    //电影表名
    public static final String DB_MOVIE = "Movie";

    //电影评分的表名
    public static final String DB_RATING = "MovieRating";

    //电影标签的表名
    public static final String DB_TAG = "Tag";

    //用户表
    public static final String DB_USER = "User";

    //电影的平均评分表
    public static final String DB_AVERAGE_MOVIES = "AverageMovies";

    //电影类别TOp10表
    public static final String DB_GENRES_TOP_MOVIES = "GenresTopMovies";

    //优质电影表
    public static final String DB_RATE_MORE_MOVIES = "RateMoreMovies";

    //最热电影表
    public static final String DB_RATE_MORE_RECENTLY_MOVIES = "RateMoreRecentlyMovies";

    //用户的推荐矩阵
    public static final String DB_USER_RECS = "UserRecs";

    //电影的相似度矩阵
    public static final String DB_MOVIE_RECS = "MovieRecs";

    //实时推荐电影表
    public static final String DB_STREAM_RECS = "StreamRecs";

    public static final String DB_ORDER_LIST = "OrderList";
    public static final String DB_COMMENT = "MovieComment";


    //***************  ES ******************

    //使用的Type
    public static final String ES_TYPE = "Movie";

    //**************  Redis ****************
    public static final int USER_RATING_QUEUE_SIZE = 20;

    //**************  LOG ****************

    public static final String USER_RATING_LOG_PREFIX = "USER_RATING_LOG_PREFIX:";

    //**************** Driver Class ***************

    public static final String ES_DRIVER_CLASS = "org.elasticsearch.spark.sql";

    public static final double MAX_RATING = 5.0F;

    public static final int MAX_RECOMMENDATIONS = 50;

    //**************** Spark *********************
    public static final String SPARK_MASTER = "spark.master";









    /**
     * Spark作业相关的常量
     */
    public static final String SPARK_APP_NAME_SESSION = "UserVisitSessionAnalyzeSpark";
    public static final String SPARK_APP_NAME_PAGE = "PageOneStepConvertRateSpark";
    public static final String FIELD_SESSION_ID = "sessionid";
    public static final String FIELD_SEARCH_KEYWORDS = "searchKeywords";
    public static final String FIELD_CLICK_CATEGORY_IDS = "clickCategoryIds";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_PROFESSIONAL = "professional";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_VISIT_LENGTH = "visitLength";
    public static final String FIELD_STEP_LENGTH = "stepLength";
    public static final String FIELD_START_TIME = "startTime";
    public static final String FIELD_CLICK_COUNT = "clickCount";
    public static final String FIELD_ORDER_COUNT = "orderCount";
    public static final String FIELD_PAY_COUNT = "payCount";
    public static final String FIELD_CATEGORY_ID = "categoryid";

    public static final String SESSION_COUNT = "session_count";

    public static final String TIME_PERIOD_1s_3s = "1s_3s";
    public static final String TIME_PERIOD_4s_6s = "4s_6s";
    public static final String TIME_PERIOD_7s_9s = "7s_9s";
    public static final String TIME_PERIOD_10s_30s = "10s_30s";
    public static final String TIME_PERIOD_30s_60s = "30s_60s";
    public static final String TIME_PERIOD_1m_3m = "1m_3m";
    public static final String TIME_PERIOD_3m_10m = "3m_10m";
    public static final String TIME_PERIOD_10m_30m = "10m_30m";
    public static final String TIME_PERIOD_30m = "30m";

    public static final String STEP_PERIOD_1_3 = "1_3";
    public static final String STEP_PERIOD_4_6 = "4_6";
    public static final String STEP_PERIOD_7_9 = "7_9";
    public static final String STEP_PERIOD_10_30 = "10_30";
    public static final String STEP_PERIOD_30_60 = "30_60";
    public static final String STEP_PERIOD_60 = "60";

    /**
     * 任务相关的常量
     */
    public static final String TASK_PARAMS = "task.params.json";
    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_END_DATE = "endDate";
    public static final String PARAM_START_AGE = "startAge";
    public static final String PARAM_END_AGE = "endAge";
    public static final String PARAM_PROFESSIONALS = "professionals";
    public static final String PARAM_CITIES = "cities";
    public static final String PARAM_SEX = "sex";
    public static final String PARAM_KEYWORDS = "keywords";
    public static final String PARAM_CATEGORY_IDS = "categoryIds";
    public static final String PARAM_TARGET_PAGE_FLOW = "targetPageFlow";

    //Session
    public static final String DB_TOP10SESSION = "Top10Session";
    public static final String DB_TOP10CATEGORY = "Top10Category";
    public static final String DB_SESSION_DETAIL = "SessionDetail";
    public static final String DB_SESSION_RANDOM_EXTRACT = "SessionRandomExtract";
    public static final String DB_SESSION_AGGR_STAT = "SessionAggrStat";

    //PRODUCT
    public static final String DB_AREA_TOP3_PRODUCT = "AreaTop3Product";

    // PAGE
    public static final String DB_PAGE_SPLIT_CONVERT_RATE = "PageSplitConvertRate";

    // AD
    public static final String DB_AD_BLACK_LIST = "AdBlacklist";
    public static final String DB_AD_USER_CLICK_COUNT = "AdUserClickCount";
    public static final String DB_AD_STAT = "AdStat";
    public static final String DB_AD_PROVINCE_TOP3 = "AdProvinceTop3";
    public static final String DB_AD_CLICK_TREND = "AdClickTrend";
    
}
