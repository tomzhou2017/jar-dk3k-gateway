package com.dk3k.framework.redis.session;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @author lait.zhang@gmail.com
 * @Date 2016年8月22日-下午4:08:15
 * @Description:<br>
 * 					</br>
 *
 */
public abstract class Session implements ISession {
	
	protected static JedisPool redisPool = null;

	// redis中的key前缀
	protected String appName = "Monbaker_";

	// 超时时间
	protected int seconds = 30 * 1;

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public boolean setAttribute(String key, Object value) {
		String l = null;
		Jedis jedis = null;
		String _value = JSONObject.toJSONString(value);
		jedis = redisPool.getResource();
		l = jedis.set(appName + key, _value);
		jedis.expire(appName + key, seconds);
		return l.equals("OK");
	}

	@Override
	public Object getAttribute(String key) {
		String result = null;
		Jedis jedis = redisPool.getResource();
		result = jedis.get(appName + key);
		if (result != null) {
			jedis.expire(appName + key, seconds);
		}
		return result;
	}

	@Override
	public boolean removeAttribute(String name) {
		return false;
	}

	@Override
	public ISession getSession() {
		return this;
	}
	
	
}
