package com.dk3k.framework.server.netty.servlet.exception;

/**
 * 
 * @description <code>NettyServletRuntimeException</code>
 *
 */
public class NettyServletRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NettyServletRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NettyServletRuntimeException(String message) {
		super(message);
	}

}
