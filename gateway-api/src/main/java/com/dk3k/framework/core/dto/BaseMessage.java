package com.dk3k.framework.core.dto;

/**
 * @author lait.zhang@gmail.com
 * @Date 2016年8月19日-下午5:38:22
 * @Description:<br>
 * 					</br>
 *
 */
public class BaseMessage {

	private String messageId;

	private String systemName;

	private String ip;

	private String queueName;

	private String content; // 消息内容

	private String type; // 1、发送。2.接收

	private String errMessage;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

}
