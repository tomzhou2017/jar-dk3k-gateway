package com.dk3k.framework.server.nettyServer.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author lait.zhang@gmail.com
 * @date Sep 13, 2016-2:00:00 PM
 * @description <code></code>
 *
 */
public class Property {
	private static Properties properties = new Properties();
	static {
		File file = new File(System.getProperty("user.dir") + "setting.properties");
		if (!file.exists()) {
			file = new File(Property.class.getResource("/").getPath() + "setting.properties");
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			properties.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
	}

	private Property() {
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getSaveFileDir() {
		return System.getProperty("user.dir") + File.separator;
	}
}
