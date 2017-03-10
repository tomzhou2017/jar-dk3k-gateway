package com.dk3k.framework.server.netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 
 * @description <code>ChannelInitializerWrapper</code>是Channel Handler与业务容器的包装类
 *
 */
public abstract class ChannelInitializerWrapper extends ChannelInitializer<SocketChannel> {

	public abstract void shutdown();
}
