package com.dk3k.framework.redis.session;

import redis.clients.jedis.JedisPool;

/**
 * @author lait.zhang@gmail.com
 * @Date 2016年8月22日-下午5:27:03
 * @Description:<br>
 * 					</br>
 *
 */
public class MobankerSession extends Session {

	public MobankerSession(JedisPool pool, String appName, int timeout) {
		redisPool = pool;
		this.appName = appName;
		this.seconds = timeout;
	}

	public MobankerSession() {

	}

	/**
	 * 获取用户
	 * 
	 * @param key
	 * @return
	 */
	public String getUser(String key) {
		Object user = getAttribute(key);
		return user.toString();
	}
}
