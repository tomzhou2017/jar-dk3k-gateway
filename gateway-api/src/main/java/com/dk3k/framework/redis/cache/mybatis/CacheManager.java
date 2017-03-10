/*
 * Copyright @ 2015QIANLONG.
 * All right reserved.
 */

package com.dk3k.framework.redis.cache.mybatis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ws.commons.schema.constants.Constants.SystemConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("all")
public class CacheManager {
	
	public static final String HTTPOK = "1";

	public static final String HTTPFAIL = "0";

	public static final String SERVER_SUCCESS = "00000000";
	

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

	private static Map<String, Object> staticMap;

	private static Map<String, Map<String, Object>> activeMap;

	/**
	 * 
	 * Description:鑾峰彇闈欐�绯荤粺鍙橀噺闆嗗悎
	 * 
	 * @return
	 * @author xiongweitao
	 * @date 2016骞�鏈�9鏃�涓嬪崍4:36:20
	 */
	public static Map<String, Object> getStaticMap() {
		if (staticMap == null) {
			staticMap = new HashMap<String, Object>();
		}
		return staticMap;
	}

	/**
	 * 
	 * Description:鑾峰彇鍔ㄦ�绯荤粺鍙橀噺闆嗗悎()
	 * 
	 * @return
	 * @author xiongweitao
	 * @date 2016骞�鏈�9鏃�涓嬪崍4:37:03
	 */
	public static Map<String, Map<String, Object>> getActiveMap() {
		if (activeMap == null) {
			activeMap = new HashMap<String, Map<String, Object>>();
		}
		return activeMap;
	}

	/**
	 * 
	 * Description:閫氳繃key鑾峰彇闈欐�绯荤粺鍙橀噺鍊紇alue(渚嬪,jdbc/redis/mq鐩稿叧)
	 * 
	 * @param key
	 * @return
	 * @author xiongweitao
	 * @date 2016骞�鏈�9鏃�涓嬪崍4:37:18
	 */
	public static String getStaticMapValue(String nid) {
		return (String) getStaticMap().get(nid);
	}

	/**
	 * 
	 * Description:閫氳繃key鑾峰彇鍔ㄦ�绯荤粺鍙橀噺鍊紇alue(渚嬪:鎺ュ彛杩炴帴http://www.mobanker.com)
	 * 
	 * @param nid
	 * @return
	 * @throws Exception
	 * @author xiongweitao
	 * @date 2016骞�鏈�9鏃�涓嬪崍4:37:44
	 */
	public static String getActiveMapValue(String nid) {
		String value = null;
		for (Entry<String, Map<String, Object>> entry : getActiveMap().entrySet()) {
			Map<String, Object> map = entry.getValue();
			if (map != null && !map.isEmpty()) {
				value = (String) map.get(nid);
				if (value != null) {
					break;
				}
			}
		}
		LOGGER.info("浠嶤acheManager涓幏鍙朅CTIVE鏁版嵁,key涓簕},value涓簕}", nid, value);
		return value;
	}

	/**
	 * 
	 * Description:淇敼鍔ㄦ�绯荤粺鍙傛暟,杩斿洖鍊艰〃绀烘垚鍔熸垨澶辫触,true涓烘垚鍔�false涓哄け璐�	 * 
	 * @param nid
	 * @param value
	 * @return
	 * @author xiongweitao
	 * @throws IOException
	 * @date 2016骞�鏈�鏃�涓嬪崍5:23:32
	 */
	public static boolean modifyActiveValue(String nid, String value) throws IOException {
		boolean flag = false;
		if (nid == null || value == null || nid.isEmpty() || value.isEmpty()) {
			throw new RuntimeException("nid 鎴栬� value 涓嶅緱涓虹┖");
		}
		String activeNode = getStaticMapValue("zk.node.active");
		String url = getStaticMapValue("zk.disconnect.configServer");
		if (activeNode == null || activeNode.isEmpty() || url == null || url.isEmpty()) {
			LOGGER.info("淇敼鍔ㄦ�绯荤粺鍙傛暟澶辫触,application.properties涓壘涓嶅埌zk.disconnect.configServer/zk.node.active淇℃伅");
			return flag;
		}
		String path = activeNode.substring(activeNode.lastIndexOf("/",activeNode.lastIndexOf("/", activeNode.lastIndexOf("/")-1)-1));
		String uri = url + path + "/" + nid + "&" + value;
		LOGGER.info("淇敼鍔ㄦ�绯荤粺鍙傛暟,璺緞涓簕}", uri);
		String result = HttpClientUtils.doGet(uri, null);
		LOGGER.info("淇敼鍔ㄦ�绯荤粺鍙傛暟,璺緞涓簕},杩斿洖鍊间负{}", uri, result);
		if (result != null || !result.isEmpty()) {
			JSONObject jsonObject = JSONObject.parseObject(result);
			if (jsonObject != null && HTTPOK.equals((String) jsonObject.get("status"))
					&& SERVER_SUCCESS.equals((String) jsonObject.get("error"))) {
				flag = true;
			}
		}
		return flag;
	}
}
