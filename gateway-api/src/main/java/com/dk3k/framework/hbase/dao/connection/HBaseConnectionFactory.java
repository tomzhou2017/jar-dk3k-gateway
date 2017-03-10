package com.dk3k.framework.hbase.dao.connection;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.hadoop.hbase.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Created by lilin on 2016/11/11.
 */
public class HBaseConnectionFactory implements PoolableObjectFactory<Connection> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseConnectionFactory.class);

    private HBaseConnection connection;

    public void setConnection(HBaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public Connection makeObject() throws Exception {
        logger.debug("New conntection has been made.");
        return connection.getConnection();
    }

    @Override
    public void destroyObject(Connection conn) throws Exception {
        logger.debug("Conntection has been destroy.");
        Assert.notNull(conn);
        connection.releaseConnection(conn);
    }

    @Override
    public boolean validateObject(Connection conn) {
        logger.debug("Conntection has been validated.");
        return true;
    }

    @Override
    public void activateObject(Connection conn) throws Exception {
        logger.debug("Conntection has been activated.");
    }

    @Override
    public void passivateObject(Connection conn) throws Exception {
        logger.debug("Conntection has been passivated.");
    }

}
