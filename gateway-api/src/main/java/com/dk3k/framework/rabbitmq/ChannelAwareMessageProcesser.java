package com.dk3k.framework.rabbitmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

/**
 * Created by Lait on 10/17/2016.
 */
public interface ChannelAwareMessageProcesser {
    void processMessage(Message message, Channel channel);
}
