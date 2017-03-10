/*
 *    Copyright 2015 The original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.dk3k.framework.redis.cache.mybatis;

/**
 * Cache adapter for Redis.
 *
 * @author Eduardo Macarron
 */
public final class RedisCache // implements Cache
{

	// private static final int DEFAULT_EXPIRE_TIME = 5*60;
	//
	// private final ReadWriteLock readWriteLock = new DummyReadWriteLock();
	//
	// private String id;
	//
	// private static JedisPool pool;
	//
	// private static String REDIS_HOST ;
	//
	// private static int EXPIRE_TIME = DEFAULT_EXPIRE_TIME;
	//
	//
	// public RedisCache(final String id) {
	// if (id == null) {
	// throw new IllegalArgumentException("Cache instances require an ID");
	// }
	// this.id = id;
	// ConfigWithHost configWithHost =
	// RedisConfigurationBuilder.getInstance().parseConfiguration();
	// Object hostObject =
	// PropertyPlaceholderConfigurer.getContextProperty("redis.host");
	// Object expireObject =
	// PropertyPlaceholderConfigurer.getContextProperty("redis.expire");
	// REDIS_HOST =
	// hostObject!=null?hostObject.toString():configWithHost.getHost();
	// EXPIRE_TIME =
	// expireObject!=null?Integer.valueOf(expireObject.toString()):DEFAULT_EXPIRE_TIME;
	// pool = new JedisPool(configWithHost, REDIS_HOST);
	// }
	//
	// private Object execute(RedisCallback callback) {
	// Jedis jedis = pool.getResource();
	// try {
	// return callback.doWithRedis(jedis);
	// } finally {
	// jedis.close();
	// }
	// }
	//
	// public String getId() {
	// return this.id;
	// }
	//
	// public int getSize() {
	// return (Integer) execute(new RedisCallback() {
	// public Object doWithRedis(Jedis jedis) {
	// Map<byte[], byte[]> result = jedis.hgetAll(id.toString().getBytes());
	// return result.size();
	// }
	//
	// @Override
	// public Object doWithRedis(ShardedJedis jedis) {
	// return null;
	// }
	// });
	// }
	//
	// public void putObject(final Object key, final Object value) {
	// execute(new RedisCallback() {
	// public Object doWithRedis(Jedis jedis) {
	// jedis.hset(id.toString().getBytes(), key.toString().getBytes(),
	// SerializeUtil.serialize(value));
	// jedis.expire(id.toString().getBytes(), EXPIRE_TIME);
	// return null;
	// }
	//
	// @Override
	// public Object doWithRedis(ShardedJedis jedis) {
	// return null;
	// }
	// });
	// }
	//
	// public Object getObject(final Object key) {
	// return execute(new RedisCallback() {
	// public Object doWithRedis(Jedis jedis) {
	// return SerializeUtil.unserialize(jedis.hget(id.toString().getBytes(),
	// key.toString().getBytes()));
	// }
	//
	// @Override
	// public Object doWithRedis(ShardedJedis jedis) {
	// return null;
	// }
	// });
	// }
	//
	// public Object removeObject(final Object key) {
	// return execute(new RedisCallback() {
	// public Object doWithRedis(Jedis jedis) {
	// return jedis.hdel(id.toString(), key.toString());
	// }
	//
	// @Override
	// public Object doWithRedis(ShardedJedis jedis) {
	// return null;
	// }
	// });
	// }
	//
	// public void clear() {
	// execute(new RedisCallback() {
	// public Object doWithRedis(Jedis jedis) {
	// jedis.del(id.toString());
	// return null;
	// }
	//
	// @Override
	// public Object doWithRedis(ShardedJedis jedis) {
	// return null;
	// }
	// });
	//
	// }
	//
	// public ReadWriteLock getReadWriteLock() {
	// return readWriteLock;
	// }
	//
	// @Override
	// public String toString() {
	// return "Redis {" + id + "}";
	// }

}
