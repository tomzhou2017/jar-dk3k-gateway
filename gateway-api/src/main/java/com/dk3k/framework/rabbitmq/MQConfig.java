package com.dk3k.framework.rabbitmq;

import java.io.IOException;
import java.util.Properties;

import com.dk3k.framework.rabbitmq.utils.PropertieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


@Configuration
// boot方式注入属性
// @ConfigurationProperties(locations = { "classpath:mq.properties" }, prefix =
// "mobanker.mq")
public class MQConfig {
	private Logger logger = LoggerFactory.getLogger(MQConfig.class);
	public static final String MQ_CONFIG_PROPERTIES = "configs/rabbitmq.properties";
	@Bean
	ConnectionFactory connectionFactory() {
		/**
		 * 走本地configs/rabbitmq.properties的文件
		 */
		Properties properties = new Properties();
		try {
			Resource res = new ClassPathResource(MQ_CONFIG_PROPERTIES);
			properties.load(res.getInputStream());
		} catch (IOException e) {
			logger.info("Failed to load rabbitmq.properties!");
		}
		
		if(properties.isEmpty()){
			throw new RuntimeException("Failed to load rabbitmq.properties!");
		}
		properties = PropertieUtils.getProperties(properties, "rabbitMQ.");
		String host = properties.getProperty("host");
		int port = Integer.valueOf(properties.getProperty("port"));
		String userName = properties.getProperty("username");
		String password = properties.getProperty("password");

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
		
		connectionFactory.setUsername(userName);
		connectionFactory.setPassword(password);
		connectionFactory.setPublisherConfirms(true);
		// enable confirm mode
		connectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true);
		return connectionFactory;
	}
}
