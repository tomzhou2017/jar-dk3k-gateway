package com.dk3k.framework.redis.cache.mybatis;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import com.dk3k.framework.redis.utils.SerializeUtil;
import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import redis.clients.jedis.ShardedJedis;

/**
 * Package Name: com.mobanker.common.redis.cache.mybatis Description: Author:
 * qiuyangjun Create Date:2015/6/15
 */
public class MybatisRedisCache implements Cache {
	private static Logger logger = LoggerFactory.getLogger(MybatisRedisCache.class);

	/**
	 * The ReadWriteLock.
	 */
	private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

	private static final int DEFAULT_EXPIRE_TIME = 5 * 60;

	private String id;

	// private static JedisPool pool;

	private static String REDIS_HOST;

	private static int EXPIRE_TIME = DEFAULT_EXPIRE_TIME;

	public MybatisRedisCache(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("Cache instances require an ID");
		}
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>MybatisRedisCache:id=" + id);
		this.id = id;
		ConfigWithHost configWithHost = RedisConfigurationBuilder.getInstance().parseConfiguration();
		Object hostObject = PropertyPlaceholderConfigurer.getContextProperty("redis.host");
		Object expireObject = PropertyPlaceholderConfigurer.getContextProperty("redis.expire");
		REDIS_HOST = hostObject != null ? hostObject.toString() : configWithHost.getHost();
		EXPIRE_TIME = expireObject != null ? Integer.valueOf(expireObject.toString()) : DEFAULT_EXPIRE_TIME;
		// pool = new JedisPool(configWithHost, REDIS_HOST);

	}

	private Object execute(RedisCallback callback) {
		ShardedJedis shardedJedis = CachePool.getInstance().getJedis();
		try {
			return callback.doWithRedis(shardedJedis);
		} finally {
			shardedJedis.close();
		}
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public int getSize() {
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>getSize,id:{}", id);
		Integer result = Integer.valueOf(execute(new RedisCallback() {

			@Override
			public Object doWithRedis(ShardedJedis jedis) {
				Map<byte[], byte[]> result = jedis.hgetAll(id.toString().getBytes());
				return result.size();

			}
		}).toString());
		// Iterator<Jedis> iterator =
		// CachePool.getInstance().getJedis().getAllShards().iterator();
		// Long dbSize = 0l;
		// while (iterator.hasNext()) {
		// Jedis jedis = iterator.next();
		// if (dbSize < jedis.dbSize()) {
		// dbSize = jedis.dbSize();
		// }
		// }

		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>getSize,id:{},size:{}", id, result);
		return result;
	}

	@Override
	public void putObject(final Object key, final Object value) {
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>putObject:id:{},key={},value={}", id, key, value);

		execute(new RedisCallback() {
			@Override
			public Object doWithRedis(ShardedJedis jedis) {
				jedis.hset(id.toString().getBytes(), key.toString().getBytes(), SerializeUtil.serialize(value));
				jedis.expire(id.toString().getBytes(), EXPIRE_TIME);
				return null;
			}
		});
		// CachePool.getInstance().getJedis().set(SerializeUtil.serialize(key.toString()),
		// SerializeUtil.serialize(value));
	}

	@Override
	public Object getObject(final Object key) {
		Object value = execute(new RedisCallback() {
			@Override
			public Object doWithRedis(ShardedJedis jedis) {
				return SerializeUtil.unserialize(jedis.hget(id.toString().getBytes(), key.toString().getBytes()));
			}
		});
		// Object value =
		// SerializeUtil.unserialize(CachePool.getInstance().getJedis().get(SerializeUtil.serialize(key.toString())));
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>getObject:id:{},key={},value={}", id, key, value);
		return value;
	}

	@Override
	public Object removeObject(final Object key) {
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>removeObject:id:{},key={}", id, key);

		return execute(new RedisCallback() {
			@Override
			public Object doWithRedis(ShardedJedis jedis) {
				return jedis.hdel(id.toString(), key.toString());
			}
		});
		// return
		// CachePool.getInstance().getJedis().expire(SerializeUtil.serialize(key.toString()),
		// 0);
	}

	@Override
	public void clear() {
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>clear,id:{}", id);
		/*
		 * Iterator<Jedis> iterator =
		 * CachePool.getInstance().getJedis().getAllShards().iterator(); while
		 * (iterator.hasNext()) { Jedis jedis = iterator.next();
		 * jedis.flushAll(); }
		 */
		execute(new RedisCallback() {
			@Override
			public Object doWithRedis(ShardedJedis jedis) {
				jedis.del(id.toString());
				return null;
			}
		});

	}

	@Override
	public ReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

}
