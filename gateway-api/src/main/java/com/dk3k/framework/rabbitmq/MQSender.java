package com.dk3k.framework.rabbitmq;

/**
 * 
 * @author lait.zhang@gmail.com
 * @Date 2016年8月19日-下午5:38:22
 * @Description:<br>
 * 					消息发送 </br>
 *
 */
public interface MQSender {
	MQResponse send(Object message);
}
