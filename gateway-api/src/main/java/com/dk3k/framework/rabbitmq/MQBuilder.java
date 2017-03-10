package com.dk3k.framework.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

//import com.dianping.cat.Cat;
//import com.dianping.cat.message.Transaction;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 
 * @author lait.zhang@gmail.com
 * @Date 2016年8月19日-下午5:38:22
 * @Description:<br>
 * 					抽象化MQ,暴露sender和consumer,sender/consumer由客户端定制</br>
 *
 */
// @Log
// @Component
public class MQBuilder {
	static Logger log = LoggerFactory.getLogger(MQBuilder.class);
	/**
	 * 链接工厂
	 */
	private ConnectionFactory connectionFactory;

	public MQBuilder(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public MQBuilder() {
	}

	/**
	 * 
	 * @param mqItem
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public MQSender buildMessageSender(final MQItem mqItem) throws IOException, TimeoutException {
		Connection connection = connectionFactory.createConnection();
		boolean createQueue = false;
		// 构造template, exchange, routingkey等
		buildQueue(mqItem, connection,createQueue);
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setExchange(mqItem.getExchange());
		rabbitTemplate.setRoutingKey(mqItem.getRoutingKey());
		// 设置message序列化方法
		rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

		// 设置发送确认
		rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				//Transaction transaction = Cat.newTransaction("Service", "RabbitTemplate.ConfirmCallback.confirm()");
				if (!ack) {
					log.info("发送消息失败: " + cause + correlationData.toString());
					//transaction.setStatus("1");
					//transaction.complete();
					throw new RuntimeException("发送失败： " + cause);
				}
				//transaction.setStatus("0");
				//transaction.complete();
			}
		});
		
		// 构造sender方法
		return new MQSender() {
			@Override
			public MQResponse send(Object message) {
				//final Transaction trans = Cat.newTransaction("Service", "MessageSender.send()");
				// 埋点统计数量
				//Cat.logMetricForCount("MessageSender.count");
				try {
					rabbitTemplate.convertAndSend(message);
					//trans.setStatus("0");
				} catch (RuntimeException e) {
					e.printStackTrace();
					log.info("发送失败 " + e);
					try {
						// retry
						rabbitTemplate.convertAndSend(message);
						//trans.setStatus("0");
					} catch (RuntimeException error) {
						error.printStackTrace();
						log.info("发送失败,尝试重发 " + error);
						//trans.setStatus("1");
						
						return new MQResponse(false, error.toString());
					}finally{
						//trans.complete();
					}
				} /*finally{
					//trans.complete();
				}*/
				return new MQResponse(true, "");
			}
		};
	}

	/**
	 * 
	 * @param mqItem
	 * @param messageProcess
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public <T> MQConsumer buildMessageConsumer(final MQItem mqItem, final MQProcess<T> messageProcess)
			throws IOException, TimeoutException {
		final Connection connection = connectionFactory.createConnection();
		boolean createQueue = true;
		// 创建连接和channel
		buildQueue(mqItem, connection,createQueue);
		// 设置message序列化方法
		final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
		final MessageConverter messageConverter = new Jackson2JsonMessageConverter();
		// 构造consumer
		return new MQConsumer() {
			QueueingConsumer consumer;
			{
				consumer = buildQueueConsumer(connection, mqItem);
			}

			@Override
			public MQResponse consume() {
				//final Transaction trans = Cat.newTransaction("Service", "MQBuilder.buildMessageConsumer()");
				QueueingConsumer.Delivery delivery = null;
				Channel channel = consumer.getChannel();
				try {
					// 通过delivery获取原始数据
					delivery = consumer.nextDelivery();
					Message message = new Message(delivery.getBody(), messagePropertiesConverter
							.toMessageProperties(delivery.getProperties(), delivery.getEnvelope(), "UTF-8"));
					// 将原始数据转换为特定类型的包
					@SuppressWarnings("unchecked")
					T messageBean = (T) messageConverter.fromMessage(message);
					// 处理数据
					MQResponse mqResponse = messageProcess.process(messageBean);
					// 手动发送ack确认
					if (mqResponse.isSuccess()) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					} else {
						log.info("发送失败: " + mqResponse.getErrMsg());
						channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
					}
					
					//trans.setStatus("0");
					return mqResponse;
				} catch (InterruptedException e) {
					//trans.setStatus(e);
					e.printStackTrace();
					return new MQResponse(false, "发生中断异常：  " + e.toString());
				} catch (IOException e) {
					//trans.setStatus(e);
					e.printStackTrace();
					//retry(delivery, channel);
					log.info("io exception : " + e);
					return new MQResponse(false, "发生IO异常：  " + e.toString());
				} catch (ShutdownSignalException e) {
					//trans.setStatus(e);
					e.printStackTrace();
					try {
						channel.close();
					} catch (IOException io) {
						io.printStackTrace();
					} catch (TimeoutException timeout) {
						timeout.printStackTrace();
					}
					consumer = buildQueueConsumer(connection, mqItem);
					return new MQResponse(false, "关闭异常： " + e.toString());
				} catch (Exception e) {
					//trans.setStatus(e);
					e.printStackTrace();
					log.info("exception : " + e);
					//retry(delivery, channel);
					return new MQResponse(false, "未知异常： " + e.toString());
				}/*finally{
					//trans.complete();
				}*/
			}
		};
	}

	/**
	 * 重试
	 * 
	 * @param delivery
	 * @param channel
	 */
	private void retry(QueueingConsumer.Delivery delivery, Channel channel) {
		//final Transaction trans = Cat.newTransaction("Service", "MQBuilder.retry()");
		
		try {
			if (null != delivery) {
				channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
			}
			
			//trans.setStatus("0");
		} catch (IOException e) {
			//trans.setStatus(e);
			e.printStackTrace();
			log.info("IO异常： " + e);
		}/*finally{
			//trans.complete();
		}*/
	}

	/**
	 * 创建queue <br>
	 * 参考： <br>
	 * <rabbit:direct-exchange name="ss" id="" auto-delete="" durable="">
	 * <rabbit:fanout-exchange name="s" > <rabbit:topic-exchange name="a" > <br>
	 * 参考：<br>
	 * <rabbit:queue auto-delete="" durable="" exclusive="" name="" id="">
	 * 
	 * @param mqItem
	 * @param connection
	 * @throws IOException
	 * @throws TimeoutException
	 */
	private void buildQueue(MQItem mqItem, Connection connection,boolean createQueue) throws IOException, TimeoutException {
		//final Transaction trans = Cat.newTransaction("Service", "MQBuilder.buildQueue()");
		
		Channel channel = connection.createChannel(false);
		channel.exchangeDeclare(mqItem.getExchange(), mqItem.getExchangeType(), mqItem.isDurable(),
				mqItem.isAutoDelete(), null);
		
		String exchangeType = mqItem.getExchangeType();
		if (exchangeType.equals("direct")) {
			createQueue(channel,mqItem);
			/*
			 * byte[] messageBodyBytes = "hello world".getBytes(); //需要绑定路由键
			 * channel.basicPublish(exchange, routingKey,
			 * MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
			 */
		}
		if (exchangeType.equals("fanout") && createQueue) {
			createQueue(channel,mqItem);
			//channel.queueDeclare(mqItem.getQueue(), mqItem.isDurable(), mqItem.isExclusive(), mqItem.isAutoDelete(), null);
			//channel.queueBind(mqItem.getQueue(), mqItem.getExchange(), mqItem.getRoutingKey());
			/*
			 * byte[] messageBodyBytes = "hello world".getBytes(); //路由键需要设置为空
			 * channel.basicPublish("exchangeName", "",
			 * MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
			 */

		}
		if (exchangeType.equals("topic") && createQueue) {
			createQueue(channel,mqItem);
			/*
			 * byte[] messageBodyBytes = "hello world".getBytes();
			 * channel.basicPublish("exchangeName", "routingKey.one",
			 * MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
			 */
		}

		try {
			//trans.setStatus("0");
			channel.close();
		} catch (TimeoutException e) {
			//trans.setStatus(e);
			e.printStackTrace();
			log.info("关闭Channel超时： " + e);
			throw e;
		}/*finally{
			//trans.complete();
		}*/
	}
	
	private void createQueue(Channel channel,MQItem mqItem) throws IOException{
		channel.queueDeclare(mqItem.getQueue(), mqItem.isDurable(), mqItem.isExclusive(), mqItem.isAutoDelete(), null);
		channel.queueBind(mqItem.getQueue(), mqItem.getExchange(), mqItem.getRoutingKey());
	}

	private QueueingConsumer buildQueueConsumer(Connection connection, MQItem mqItem) {
		//final Transaction trans = Cat.newTransaction("Service", "MQBuilder.buildQueueConsumer()");
		
		Channel channel = connection.createChannel(false);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		try {
			/**
			 * 通过 BasicQos 方法设置prefetchCount =
			 * 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
			 * 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
			 */
			channel.basicQos(1);
			channel.basicConsume(mqItem.getQueue(), false, consumer);
			//trans.setStatus("0");
		} catch (IOException e) {
			//trans.setStatus(e);
			e.printStackTrace();
			log.info("创建消息消费异常 : " + e);
		} /*finally{
			trans.complete();
		}*/
		return consumer;
	}

	/**
	 * 获取队列数量
	 * 
	 * @param queue
	 * @return
	 * @throws IOException
	 */

	public int getMessageCount(final String queue) throws IOException {
		Connection connection = connectionFactory.createConnection();
		final Channel channel = connection.createChannel(false);
		final AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);
		return declareOk.getMessageCount();
	}
}
