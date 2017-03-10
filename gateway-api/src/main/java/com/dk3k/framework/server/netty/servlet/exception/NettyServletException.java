package com.dk3k.framework.server.netty.servlet.exception;

/**
 * 
 * @description <code>NettyServletException</code>
 *
 */
public class NettyServletException extends Exception {

	private static final long serialVersionUID = 1L;

	public NettyServletException(String message, Throwable cause) {
		super(message, cause);
	}

	public NettyServletException(String message) {
		super(message);
	}

}
