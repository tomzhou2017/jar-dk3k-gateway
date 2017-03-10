package com.dk3k.framework.redis.dataSource;

import com.dk3k.framework.redis.template.RedisClientTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import javax.annotation.Resource;
import java.io.*;
import java.util.concurrent.Callable;

public class RedisClientCache implements Cache{

	@Resource
	private RedisClientTemplate redisClient;
	private int liveTime = 0;
	private String name;

	public void setLiveTime(int liveTime) {
		this.liveTime = liveTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public Object getNativeCache() {
		// TODO Auto-generated method stub
		return this.redisClient;
	}

//	@Override
//	public ValueWrapper get(Object key) {
//		ValueWrapper wrapper = null;
//		String keyStr=key.toString();
//		final String keyf = name+":"+keyStr;
//		String value = redisClient.get(keyf);
//	    if (value != null) {
//	    	wrapper = new SimpleValueWrapper(value);
//	    }
//	    return wrapper;
//	}
//	@Override
//	public void put(Object key, Object value) {
//		// TODO Auto-generated method stub
//		final String keyf = name+":"+key.toString();
//		final String valuef = value.toString();
//		redisClient.set(keyf, valuef);
//		if (liveTime > 0) {
//			redisClient.expire(keyf, liveTime);
//		}
//	}
//    
	@Override
	public void evict(Object key) {
		redisClient.del(name+":"+key.toString());
	}



	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
	    Object cacheValue = redisClient.get(key.toString());
	    Object value = (cacheValue != null ? cacheValue : null);
	    if (type != null && !type.isInstance(value)) {
	      throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
	    }
	    return (T) value;
	}

	public <T> T get(Object o, Callable<T> callable) {
		return null;
	}



	@Override
	public ValueWrapper putIfAbsent(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 描述 : <Object转byte[]>. <br>
	 * <p>
	 * <使用方法说明>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unused")
	private byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	/**
	 * 描述 : <byte[]转Object>. <br>
	 * <p>
	 * <使用方法说明>
	 * </p>
	 * 
	 * @param bytes
	 * @return
	 */
	
	@SuppressWarnings("unused")
	private Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}
	@Override
	public ValueWrapper get(Object key) {
		final String keyf = name+":"+(String) key;
		Object object = null;
		byte[] value = redisClient.get(keyf.getBytes());
		if (value == null) {
			return null;
		}
		object= toObject(value);
		return (object != null ? new SimpleValueWrapper(object) : null);
	}	
	@Override
	public void put(Object key, Object value) {
		// TODO Auto-generated method stub
		final String keyf = name+":"+(String) key;
		final Object valuef = value;

		byte[] keyb = keyf.getBytes();
		byte[] valueb = toByteArray(valuef);
		redisClient.set(keyb, valueb);
		if (liveTime > 0) {
			redisClient.expire(keyb, liveTime);
		}


	}

}
