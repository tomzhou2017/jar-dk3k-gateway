package com.dk3k.framework.redis.dataSource;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Package Name: com.mobanker.common.redis.dataSource
 * Description:
 * Author: qiuyangjun
 * Create Date:2015/6/11
 */
public interface RedisDataSource {
    ShardedJedisPool getShardedJedisPool();

    public abstract ShardedJedis getRedisClient();

    public void returnResource(ShardedJedis shardedJedis);

    public void returnResource(ShardedJedis shardedJedis, boolean broken);
}
