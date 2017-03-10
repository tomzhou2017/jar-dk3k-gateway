package com.dk3k.framework.rabbitmq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.core.ReplyToAddressCallback;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by Lait on 10/17/2016.
 */
public class RabbitmqProducerProxy implements RabbitOperations {

    private RabbitTemplate rabbitTemplate;

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public <T> T execute(ChannelCallback<T> channelCallback) throws AmqpException {
        T t = null;
        try {
            t = rabbitTemplate.execute(channelCallback);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        } 
        return t;
    }

    @Override
    public void send(Message message) throws AmqpException {
        try {
            rabbitTemplate.send(message);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        } 

    }

    @Override
    public void send(String s, Message message) throws AmqpException {
        try {
            rabbitTemplate.send(s, message);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        } 

    }

    @Override
    public void send(String s, String s1, Message message) throws AmqpException {
        try {
            rabbitTemplate.send(s, s1, message);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
    }

    @Override
    public void convertAndSend(Object o) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        } 
    }

    @Override
    public void convertAndSend(String s, Object o) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(s, o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
    }

    @Override
    public void convertAndSend(String s, String s1, Object o) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(s, s1, o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
    }

    @Override
    public void convertAndSend(Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(o, messagePostProcessor);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        } 
    }

    @Override
    public void convertAndSend(String s, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(s, o, messagePostProcessor);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
    }

    @Override
    public void convertAndSend(String s, String s1, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(s, s1, o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
    }

    @Override
    public Message receive() throws AmqpException {
        Message message = null;
        try {
            message = rabbitTemplate.receive();
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return message;
    }

    @Override
    public Message receive(String s) throws AmqpException {
        Message message = null;
        try {
            message = rabbitTemplate.receive(s);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return message;
    }

    @Override
    public Message receive(long l) throws AmqpException {
        Message message = null;
        try {
            message = rabbitTemplate.receive(l);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return message;
    }

    @Override
    public Message receive(String s, long l) throws AmqpException {
        Message message = rabbitTemplate.receive(s, l);
        // 埋点统计数量
        return message;
    }

    @Override
    public Object receiveAndConvert() throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.receiveAndConvert();
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object receiveAndConvert(String s) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.receiveAndConvert(s);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object receiveAndConvert(long l) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.receiveAndConvert(l);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object receiveAndConvert(String s, long l) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.receiveAndConvert(s, l);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(receiveAndReplyCallback);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }

    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(s, receiveAndReplyCallback);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, String s, String s1) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(receiveAndReplyCallback, s, s1);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }


    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, String s1, String s2) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(receiveAndReplyCallback, s1, s2);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, ReplyToAddressCallback<S> replyToAddressCallback) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(receiveAndReplyCallback, replyToAddressCallback);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }

    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, ReplyToAddressCallback<S> replyToAddressCallback) throws AmqpException {
        Boolean b = null;
        try {
            b = rabbitTemplate.receiveAndReply(s, receiveAndReplyCallback, replyToAddressCallback);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return b;
    }

    @Override
    public Message sendAndReceive(Message message) throws AmqpException {
        Message _message = null;
        try {
            _message = rabbitTemplate.sendAndReceive(message);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return _message;
    }

    @Override
    public Message sendAndReceive(String s, Message message) throws AmqpException {
        Message _message = null;
        try {
            // 埋点统计数量
        } catch (AmqpException ae) {
            _message = rabbitTemplate.sendAndReceive(s, message);

            throw ae;
        }
        return _message;

    }

    @Override
    public Message sendAndReceive(String s, String s1, Message message) throws AmqpException {
        Message _message = null;
        try {
            _message = rabbitTemplate.sendAndReceive(s, s1, message);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }

        return _message;
    }

    @Override
    public Object convertSendAndReceive(Object o) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.convertSendAndReceive(o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object convertSendAndReceive(String s, Object o) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.convertSendAndReceive(s, o);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object convertSendAndReceive(String s, String s1, Object o) throws AmqpException {
        Object obj = rabbitTemplate.convertSendAndReceive(s, s1, o);
        // 埋点统计数量
        return obj;
    }

    @Override
    public Object convertSendAndReceive(Object o, MessagePostProcessor messagePostProcessor) throws
            AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.convertSendAndReceive(o, messagePostProcessor);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object convertSendAndReceive(String s, Object o, MessagePostProcessor messagePostProcessor) throws
            AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.convertSendAndReceive(s, o, messagePostProcessor);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }

    @Override
    public Object convertSendAndReceive(String s, String s1, Object o, MessagePostProcessor
            messagePostProcessor) throws AmqpException {
        Object obj = null;
        try {
            obj = rabbitTemplate.convertSendAndReceive(s, s1, o, messagePostProcessor);
            // 埋点统计数量
        } catch (AmqpException ae) {
            throw ae;
        }
        return obj;
    }
}
