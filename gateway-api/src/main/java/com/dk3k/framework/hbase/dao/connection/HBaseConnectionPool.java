package com.dk3k.framework.hbase.dao.connection;

import com.dk3k.framework.hbase.support.PropertieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.hadoop.hbase.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * Created by lilin on 2016/11/11.
 */
public class HBaseConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(HBaseConnectionPool.class);

    private static GenericObjectPool<Connection> pool = null;

    private Properties propertyPlaceholder;

    public HBaseConnectionPool(HBaseConnectionFactory connectionFactory) {
        propertyPlaceholder = HBaseConnection.getProperties(HBaseConnection.HBASE_CONFIG_PROPERTIES);

        Properties properties = PropertieUtils.getProperties(propertyPlaceholder, "hbase.pool.");
        String maxActive = properties.getProperty("maxActive");
        String maxWait = properties.getProperty("maxWait");
        String maxIdle = properties.getProperty("maxIdle");
        String minIdle = properties.getProperty("minIdle");

        Config config = new Config();
        if (StringUtils.isNotBlank(maxActive)) {
            config.maxActive = Integer.valueOf(maxActive);
        }
        if (StringUtils.isNotBlank(maxWait)) {
            config.maxWait = Integer.valueOf(maxWait);
        }
        if (StringUtils.isNotBlank(maxIdle)) {
            config.maxIdle = Integer.valueOf(maxIdle);
        }
        if (StringUtils.isNotBlank(minIdle)) {
            config.minIdle = Integer.valueOf(minIdle);
        }

        pool = new GenericObjectPool<Connection>(connectionFactory, config);
    }

    public Properties getPropertyPlaceholder() {
        return propertyPlaceholder;
    }

    public String getProperty(String key) {
        return propertyPlaceholder.getProperty(key);
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = pool.borrowObject();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return conn;
    }

    public void release(Connection conn) {
        try {
            pool.returnObject(conn);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
