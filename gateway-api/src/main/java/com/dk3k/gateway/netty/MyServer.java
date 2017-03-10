package com.dk3k.gateway.netty;

import com.dk3k.framework.server.Dk3kApplication;
import com.dk3k.framework.server.netty.annotation.NettyBootstrap;

@NettyBootstrap(springApplicationContext = "classpath:/dk3k-gateway-application.xml", springServletContext = "classpath:/dk3k-gateway-servlet.xml")
public class MyServer {
	public static void main(String[] args) {
		Dk3kApplication.run(MyServer.class, args);
	}
}