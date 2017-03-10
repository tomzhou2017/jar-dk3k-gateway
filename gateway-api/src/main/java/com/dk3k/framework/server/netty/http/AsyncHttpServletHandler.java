package com.dk3k.framework.server.netty.http;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaderValues;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.server.netty.servlet.async.ThreadLocalAsyncExecutor;

public class AsyncHttpServletHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(AsyncHttpServletHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object e) throws Exception {
		if (e instanceof ServletResponse) {
			logger.info("Handler async task...");
			HttpServletResponse response = (HttpServletResponse) e;
			Runnable task = ThreadLocalAsyncExecutor.pollTask(response);
			task.run();

			// write response...
			ChannelFuture future = ctx.channel().writeAndFlush(response);

			String keepAlive = response.getHeader(CONNECTION.toString());
			if (null != keepAlive && HttpHeaderValues.KEEP_ALIVE.toString().equalsIgnoreCase(keepAlive)) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else {
			ctx.fireChannelRead(e);
		}
	}
}
