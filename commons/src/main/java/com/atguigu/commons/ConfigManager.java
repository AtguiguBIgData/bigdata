package com.atguigu.commons;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    private static Configuration config = null;

    static {

        String configfile = "bigdata.properties";
        if(null != System.getenv(Constants.BIGDATA_HOME)){

        }
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(configfile));
        try {
            config = builder.getConfiguration();
        } catch(ConfigurationException cex) {
            // loading of the configuration file failed
            LOG.error("Error while loading bigdata.properties ", cex);
            System.exit(1);
        }

    }

    public static String getServerAddress(){
        return config.getString("jetty.host");
    }

    public static int getServerPort(){
        return config.getInt("jetty.port");
    }

    public static String getServerContextPath(){
        return "/";
    }

    public static String getSparkAddress(){
        return config.getString("spark.address");
    }

    public static String getDataFileMovies(){
        return config.getString("datafile_movies");
    }

    public static String getDataFileRatings(){
        return config.getString("datafile_ratings");
    }

    public static String getDataFileTags(){
        return config.getString("datafile_tags");
    }

    public static String getMySqlUrl(){
        return config.getString("jdbc.url");
    }

    public static String getMySqlUsername(){
        return config.getString("jdbc.user");
    }

    public static String getMySqlPassword(){
        return config.getString("jdbc.password");
    }

    public static String getEsHttpHosts(){
        return config.getString("es.httpHosts");
    }

    public static String getEsTransportHosts(){
        return config.getString("es.transportHosts");
    }

    public static String getEsClusterName(){
        return config.getString("es.cluster.name");
    }

    public static String getEsIndexName(){
        return config.getString("es.index.name");
    }

    public static int getPoolSize(){
        return config.getInt("pool.size");
    }

    public static String getKafkaBrokers(){
        return config.getString("kafka.broker.list");
    }

    public static String getZookeeper(){
        return config.getString("zookeeper.list");
    }

    public static String getKafkaRecommendFromTopic(){
        return config.getString("kafka.recom.from.topic");
    }

    public static String getKafkaRecommendToTopic(){
        return config.getString("kafka.recom.to.topic");
    }

    public static String getkafkaAdTopic(){
        return config.getString("kafka.ad.topic");
    }

    public static String getRedisHost(){
        return config.getString("redis.host");
    }

    public static String getTaskJson(){
        return config.getString("task.params.json");
    }

}
