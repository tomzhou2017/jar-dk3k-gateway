package com.dk3k.framework.rabbitmq;

/**
 * @author lait.zhang@gmail.com
 * @date Sep 7, 2016-10:03:20 PM
 * @description <code></code>
 *
 */
public class MQItem {
	private static final String EXCHANGE_TYPE = "direct";
	private static final boolean DURABLE = true;
	private static final boolean AUTO_DELETE = false;
	private static final boolean EXCLUSIVE = false;
	private static final boolean TRANSACTIONAL = false;

	private String exchange;
	private String routingKey;
	private String queue;
	
	
	
	/**
	 * 消息转换器
	 */
	// @Autowired
	// private MessageConverter messageConverter;
	/**
	 * callback
	 */

	/**
	 * 其中类型有：direct,fanout,topic,header,默认为direct
	 */
	private String exchangeType = EXCHANGE_TYPE;
	/**
	 * 该队列是否开启事务
	 */
	private boolean transactional = TRANSACTIONAL;
	/**
	 * 属性说明：Flag indicating that the queue is durable, meaning that it will
	 * survive broker restarts (not that the messages in it will, although they
	 * might if they are persistent). Default is true.
	 */
	private boolean durable = DURABLE;
	/**
	 * 属性说明：Flag indicating that an queue will be deleted when it is no longer
	 * in use, i.e. the connection that declared it is closed. Default is false.
	 */
	private boolean autoDelete = AUTO_DELETE;
	/**
	 * 属性说明： Flag indicating that the queue is exclusive to this connection.
	 * Default is false.
	 * 
	 */
	private boolean exclusive = EXCLUSIVE;

	public MQItem setExchange(String exchange) {
		this.exchange = exchange;
		return this;
	}

	public MQItem setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
		return this;
	}

	public MQItem setQueue(String queue) {
		this.queue = queue;
		return this;
	}

	public MQItem setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
		return this;
	}

	public MQItem setDurable(boolean durable) {
		this.durable = durable;
		return this;
	}

	public MQItem setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	public MQItem setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
		return this;
	}
	public MQItem setTransactional(boolean transactional) {
		this.transactional = transactional;
		return this;
	}
	
	
	
	/************************** get **************************/
	public String getExchange() {
		return exchange;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public String getQueue() {
		return queue;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public boolean isDurable() {
		return durable;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isTransactional() {
		return transactional;
	}


	

}
