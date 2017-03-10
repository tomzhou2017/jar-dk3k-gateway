package com.dk3k.framework.hbase.dao.connection;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by lilin on 2016/11/11.
 */
public class HBaseConnection {

    protected static final String HBASE_CONFIG_PROPERTIES = "configs/hbase.properties";
    private static final Logger logger = LoggerFactory.getLogger(HBaseConnection.class);
    private Configuration configuration;

    public HBaseConnection() {
        Properties properties = getProperties(HBASE_CONFIG_PROPERTIES);

        String quorum = properties.getProperty("hbase.zookeeper.quorum");
        if (StringUtils.isBlank(quorum)) {
            throw new IllegalArgumentException("Blank hbase.zookeeper.quorum");
        }

        String port = properties.getProperty("hbase.zookeeper.property.clientPort");
        String pause = properties.getProperty("hbase.client.pause");
        String number = properties.getProperty("hbase.client.retries.number");
        String timeout = properties.getProperty("hbase.rpc.timeout");
        String period = properties.getProperty("hbase.client.scanner.timeout.period");

        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", quorum);
        configuration.set("hbase.zookeeper.property.clientPort", port);
        configuration.set("hbase.client.pause", pause);
        configuration.set("hbase.client.retries.number", number);
        configuration.set("hbase.rpc.timeout", timeout);
        configuration.set("hbase.client.scanner.timeout.period", period);
    }

    public static Properties getProperties() {
        return getProperties(HBASE_CONFIG_PROPERTIES);
    }

    public static Properties getProperties(String config) {
        if (config == null) {
            config = HBASE_CONFIG_PROPERTIES;
        }

        Properties properties = new Properties();
        try {
            Resource res = new ClassPathResource(config);
            properties.load(res.getInputStream());
        } catch (IOException e) {
            //throw new RuntimeException("Failed to load hbase.properties!");
            logger.warn("Failed to load hbase.properties!");
        }

        //如果本地没有配置文件则走配置中心
//        if (properties.isEmpty()) {
//            logger.info("no configrations hbase.key by hbase.properties! load configrations by zookeeper!");
//            properties = PropertyPlaceholderConfigurer.getPro();
//        }

        if (properties.isEmpty()) {
            throw new RuntimeException("no configrations hbase.key by hbase.properties or zookeeper!");
        }

        return properties;
    }

    public Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(configuration);
        //return ConnectionProxyFactory.createConnection(configuration);
    }

    public void releaseConnection(Connection conn) throws IOException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

}
