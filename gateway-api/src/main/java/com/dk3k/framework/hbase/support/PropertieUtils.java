package com.dk3k.framework.hbase.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author lait.zhang@gmail.com
 * @Date 2016年8月12日-下午5:09:46
 * @Description:<br>
 * 					统一加载所有.properties文件到集合,并提供相应的转换：propertie -> json | properties->bean 等</br>
 *
 */
public class PropertieUtils implements BeanFactoryAware {

	private static final String DEFAULT_ENCODING = "UTF-8";

	private static PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
	private static ResourceLoader resourceLoader = new DefaultResourceLoader();

	private static final Pattern floatPattern = Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$");
	private static final Pattern intPattern = Pattern.compile("^-?[0-9]+$");

	private BeanFactory beanFactory;
	private Properties properties;



	/**
	 * 根据前缀获取key-value
	 * @param prefix
	 * @return
	 */
	public List<String> getList(String prefix) {
		if (properties == null || prefix == null) {
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<String>();
		Enumeration<?> en = properties.propertyNames();
		String key;
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.startsWith(prefix)) {
				list.add(properties.getProperty(key));
			}
		}
		return list;
	}

	public Set<String> getSet(String prefix) {
		if (properties == null || prefix == null) {
			return Collections.emptySet();
		}
		Set<String> set = new TreeSet<String>();
		Enumeration<?> en = properties.propertyNames();
		String key;
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.startsWith(prefix)) {
				set.add(properties.getProperty(key));
			}
		}
		return set;
	}

	public Map<String, String> getMap(String prefix) {
		if (properties == null || prefix == null) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<?> en = properties.propertyNames();
		String key;
		int len = prefix.length();
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.startsWith(prefix)) {
				map.put(key.substring(len), properties.getProperty(key));
			}
		}
		return map;
	}

	public Properties getProperties(String prefix) {
		Properties props = new Properties();
		if (properties == null || prefix == null) {
			return props;
		}
		Enumeration<?> en = properties.propertyNames();
		String key;
		int len = prefix.length();
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.startsWith(prefix)) {
				props.put(key.substring(len), properties.getProperty(key));
			}
		}
		return props;
	}

	public String getPropertiesString(String prefix) {
		String property = "";
		if (properties == null || prefix == null) {
			return property;
		}
		Enumeration<?> en = properties.propertyNames();
		String key;
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.equals(prefix)) {
				return properties.getProperty(key);
			}
		}
		return property;
	}

	/**
	 * 获取bean map $ PropertyUtils.java Map<String,Object>
	 * 
	 * @param prefix
	 * @return
	 */
	public Map<String, Object> getBeanMap(String prefix) {
		Map<String, String> keyMap = getMap(prefix);
		if (keyMap.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> resultMap = new HashMap<String, Object>(keyMap.size());
		String key, value;
		for (Map.Entry<String, String> entry : keyMap.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			resultMap.put(key, beanFactory.getBean(value, Object.class));
		}
		return resultMap;
	}

	public static Properties getProperties(File file) {
		Properties props = new Properties();
		InputStream in;
		try {
			in = new FileInputStream(file);
			props.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public static String getPropertyValue(File file, String key) {
		Properties props = getProperties(file);
		return (String) props.get(key);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * 载入多个properties文件, 相同的属性在最后载入的文件中的值将会覆盖之前的载入. 文件路径使用Spring
	 * Resource格式,文件编码使用UTF-8.
	 * 
	 * @param resourcesPaths
	 * @return
	 * @throws IOException
	 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
	 */
	public static Properties loadProperties(String... resourcesPaths) throws IOException {
		Properties props = new Properties();
		for (String location : resourcesPaths) {
			InputStream is = null;
			try {
				Resource resource = resourceLoader.getResource(location);
				is = resource.getInputStream();
				propertiesPersister.load(props, new InputStreamReader(is, DEFAULT_ENCODING));
			} catch (IOException ex) {
				throw new IOException("Could not load properties from classpath:" + location + ": " + ex.getMessage());
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		return props;
	}
	
	public static Properties loadProperties(String prefix,String... resourcesPaths) throws IOException {
		Properties props = new Properties();
		for (String location : resourcesPaths) {
			InputStream is = null;
			try {
				Resource resource = resourceLoader.getResource(location);
				is = resource.getInputStream();
				propertiesPersister.load(props, new InputStreamReader(is, DEFAULT_ENCODING));
			} catch (IOException ex) {
				throw new IOException("Could not load properties from classpath:" + location + ": " + ex.getMessage());
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		return getProperties(props,prefix);
	}
	
	public static Properties getProperties(Properties properties,String prefix) {
		Properties props = new Properties();
		if (properties == null || prefix == null) {
			return props;
		}
		Enumeration<?> en = properties.propertyNames();
		String key;
		int len = prefix.length();
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			if (key.startsWith(prefix)) {
				props.put(key.substring(len), properties.getProperty(key));
			}
		}
		return props;
	}


}
