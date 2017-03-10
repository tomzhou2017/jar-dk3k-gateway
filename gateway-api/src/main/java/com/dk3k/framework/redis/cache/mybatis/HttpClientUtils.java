/*
 * Copyright @ 2016QIANLONG.
 * All right reserved.
 */

package com.dk3k.framework.redis.cache.mybatis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtils {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	// private static CloseableHttpClient httpClient;
	public static final String CHARSET = "UTF-8";
	private static ThreadLocal<Map<String, String>> httpHeader = new ThreadLocal<Map<String, String>>();
	private static ThreadLocal<Map<String, Object>> httpClientConfig = new ThreadLocal<Map<String, Object>>();

	// 杩炴帴瓒呮椂鏃堕棿
	public static final String CONNECT_TIMEOUT = "connect_timeout";
	// socket瓒呮椂鏃堕棿
	public static final String SOCKET_TIMEOUT = "socket_timeout";
	public static final Integer DEFAULT_CONNECT_TIMEOUT = 600000;
	public static final Integer DEFAULT_SOCKET_TIMEOUT = 600000;

	public static CloseableHttpClient buildHttpClient() {
		Map<String, Object> configSetting = new HashMap<String, Object>();
		if (httpClientConfig != null && null != httpClientConfig.get()) {
			configSetting = httpClientConfig.get();
		}
		RequestConfig.Builder builder = RequestConfig.custom();
		Integer connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		if (configSetting.get(CONNECT_TIMEOUT) != null) {
			try {
				connectTimeout = Integer.valueOf(configSetting.get(CONNECT_TIMEOUT).toString());
			} catch (Exception e) {
				logger.warn("class covert error!", e);
				connectTimeout = DEFAULT_CONNECT_TIMEOUT;
			}
		}
		builder.setConnectTimeout(connectTimeout);
		Integer socketTimeout = DEFAULT_SOCKET_TIMEOUT;
		if (configSetting.get(SOCKET_TIMEOUT) != null) {
			try {
				socketTimeout = Integer.valueOf(configSetting.get(SOCKET_TIMEOUT).toString());
			} catch (Exception e) {
				logger.warn("class covert error!", e);
				socketTimeout = DEFAULT_SOCKET_TIMEOUT;
			}
		}
		builder.setSocketTimeout(socketTimeout);
		RequestConfig config = builder.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}

	public static String doGet(String url, Map<String, String> params) throws IOException {
		return doGet(url, params, CHARSET);
	}

	public static String doPost(String url, Map<String, String> params) throws IOException {
		return doPost(url, params, CHARSET);
	}

	/**
	 * HTTP Get 鑾峰彇鍐呭
	 * 
	 * @param url
	 *            璇锋眰鐨剈rl鍦板潃 ?涔嬪墠鐨勫湴鍧�	 * @param params
	 *            璇锋眰鐨勫弬鏁�	 * @param charset
	 *            缂栫爜鏍煎紡
	 * @return 椤甸潰鍐呭
	 */
	public static String doGet(String url, Map<String, String> params, String charset) throws IOException {
		if (url == null || url.length() <= 0) {
			return null;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
				for (Map.Entry<String, String> entry : params.entrySet()) {
					String value = entry.getValue();
					if (value != null) {
						pairs.add(new BasicNameValuePair(entry.getKey(), value));
					}
				}
				url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
			}
			httpGet = new HttpGet(url);
			handlerHeader(httpGet);

			httpClient = buildHttpClient();
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
			}
			EntityUtils.consume(entity);
			response.close();
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
			if (httpGet != null) {
				httpGet.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

	private static void handlerHeader(HttpRequestBase requestBase) {
		if (httpHeader != null && httpHeader.get() != null) {
			Map<String, String> map = httpHeader.get();
			for (String key : map.keySet()) {
				requestBase.addHeader(key, map.get(key));
			}
		}
	}

	/**
	 * HTTP Post 鑾峰彇鍐呭
	 * 
	 * @param url
	 *            璇锋眰鐨剈rl鍦板潃 ?涔嬪墠鐨勫湴鍧�	 * @param params
	 *            璇锋眰鐨勫弬鏁�	 * @param charset
	 *            缂栫爜鏍煎紡
	 * @return 椤甸潰鍐呭
	 */
	public static String doPost(String url, Map<String, String> params, String charset) throws IOException {
		if (url == null || url.length() <= 0) {
			return null;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
			List<NameValuePair> pairs = null;
			if (params != null && !params.isEmpty()) {
				pairs = new ArrayList<NameValuePair>(params.size());
				for (Map.Entry<String, String> entry : params.entrySet()) {
					String value = entry.getValue();
					if (value != null) {
						pairs.add(new BasicNameValuePair(entry.getKey(), value));
					}
				}
			}
			httpPost = new HttpPost(url);
			handlerHeader(httpPost);
			if (pairs != null && pairs.size() > 0) {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
			}
			httpClient = buildHttpClient();
			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
			}
			EntityUtils.consume(entity);
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

	/**
	 * HTTP Post 鑾峰彇鍐呭
	 * 
	 * @param url
	 *            璇锋眰鐨剈rl鍦板潃
	 * @param jsonParam
	 *            璇锋眰鐨凧SON鍙傛暟
	 * @return
	 */
	public static String doPost(String url, String jsonParam) throws IOException {
		if (url == null || url.length() <= 0) {
			return null;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			handlerHeader(httpPost);
			if (jsonParam != null && jsonParam.length() > 0) {
				StringEntity entity = new StringEntity(jsonParam);
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				httpPost.setEntity(entity);
			}
			httpClient = buildHttpClient();
			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
			}
			EntityUtils.consume(entity);
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

	public static void setHeader(Map<String, String> header) {
		if (header != null) {
			httpHeader.set(header);
		}
	}

	public static void setConfig(Map<String, Object> config) {
		if (config != null) {
			httpClientConfig.set(config);
		}
	}
}
