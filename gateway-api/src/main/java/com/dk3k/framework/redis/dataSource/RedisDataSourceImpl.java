package com.dk3k.framework.redis.dataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Properties;

/**
 * Package Name: com.mobanker.common.redis.dataSource
 * Description:
 * Author: qiuyangjun
 * Create Date:2015/6/11
 */
@Repository("redisDataSource")
public class RedisDataSourceImpl implements  RedisDataSource{
    private static final Logger log = LoggerFactory.getLogger(RedisDataSourceImpl.class);

    @Resource
    private ShardedJedisPool shardedJedisPool;

    @Override
    public ShardedJedisPool getShardedJedisPool(){
        return shardedJedisPool;
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    public ShardedJedis getRedisClient() {
        try {
            ShardedJedis shardJedis = shardedJedisPool.getResource();
            return shardJedis;
        } catch (Exception e) {
            log.error("getRedisClent error", e);
        }
        return null;
    }

    public void returnResource(ShardedJedis shardedJedis) {
        shardedJedisPool.returnResource(shardedJedis);
    }

    public void returnResource(ShardedJedis shardedJedis, boolean broken) {
        if (broken) {
            shardedJedisPool.returnBrokenResource(shardedJedis);
        } else {
            shardedJedisPool.returnResource(shardedJedis);
        }
    }

    public  static final String REDIS_CONFIG_PROPERTIES = "configs/redis.properties";

    public static Properties getProperties(){
        return getProperties(REDIS_CONFIG_PROPERTIES);
    }

    public static Properties getProperties(String config) {
        if(config==null){
            config=REDIS_CONFIG_PROPERTIES;
        }

        Properties properties = new Properties();
        try {
            org.springframework.core.io.Resource res = new ClassPathResource(config);
            properties.load(res.getInputStream());
        } catch (IOException e) {
            //throw new RuntimeException("Failed to load redis.properties!");
            log.warn("Failed to load redis.properties!");
        }

        //如果本地没有配置文件则走配置中心
//        if (properties.isEmpty()) {
//            log.info("no configrations redis.key by redis.properties! load configrations by zookeeper!");
//            properties = PropertyPlaceholderConfigurer.getPro();
//        }

        if (properties.isEmpty()) {
            throw new RuntimeException("no configrations redis.key by redis.properties or zookeeper!");
        }

        return properties;
    }

}
