package com.dk3k.framework.redis.session;

/**
 * @author lait.zhang@gmail.com
 * @Date 2016年8月22日-下午4:05:35
 * @Description:<br>
 * </br>
 *
 */
public interface ISession {
	/**
	 * 该方法将对象作为属性存储到会话对象中。
	 * 
	 * @param name
	 * @param value
	 */
	public boolean setAttribute(String name, Object value);

	/**
	 * 该方法从会话对象中获取属性。
	 * 
	 * @param name
	 * @return
	 */
	public Object getAttribute(String name);

	/**
	 * 该方法从会话对象中删除属性。
	 * 
	 * @param name
	 */
	public boolean removeAttribute(String name);

	/**
	 * 该方法从会话对象中删除属性。
	 * 
	 * @return
	 */
	public ISession getSession();

}
