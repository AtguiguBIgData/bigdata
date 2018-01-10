package com.atguigu.business.utils;

import com.atguigu.commons.ConfigManager;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class Configure {

    private String esClusterName;
    private String esHost;
    private int esPort;
    private String redisHost;

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPwd;

    public Configure() {
        this.esClusterName = ConfigManager.getEsClusterName();
        this.esHost = ConfigManager.getEsTransportHosts().split(":")[0];
        this.esPort = Integer.parseInt(ConfigManager.getEsTransportHosts().split(":")[1]);
        this.redisHost = ConfigManager.getRedisHost();

        this.jdbcUrl = ConfigManager.getMySqlUrl();
        this.jdbcUser = ConfigManager.getMySqlUsername();
        this.jdbcPwd = ConfigManager.getMySqlPassword();
    }

    @Bean(name = "transportClient")
    public TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", esClusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        return esClient;
    }

    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(redisHost);
        return jedis;
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource getDriverManagerDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUser);
        dataSource.setPassword(jdbcPwd);
        return dataSource;
    }
}
