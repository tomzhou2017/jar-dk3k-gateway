package com.dk3k.framework.server;

import com.dk3k.framework.server.netty.NettyServerBootstrap;
import com.dk3k.framework.server.netty.annotation.NettyBootstrap;
import com.dk3k.framework.server.netty.utils.ResourcesUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author liat.zhang@gmail.com
 * @date 2016年9月15日-下午9:19:23
 * @description <code>NettyServerBootstrap</code>服务器引导程序类，用于调度服务器启动接口
 *
 */
@NettyBootstrap
public class Dk3kApplication {
	/**
	 *
	 * @description <code>入口程序</code>
	 * @param args
	 */
	public static void main(String[] args) {
		run(null,args);
	}

	public static void run(Class<?> clazz,String[] args){
		//需要手动扫描加载
		try {
			File file = ResourcesUtil.getFile("classpath*:/*.xml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		NettyServerBootstrap  nettyServerBootstrap = NettyServerBootstrap.createServer();
		if(null != clazz){
			NettyBootstrap nb = clazz.getAnnotation(NettyBootstrap.class);
			String springApplicationContext = nb.springApplicationContext();
			String springServletContext = nb.springServletContext();
			String serverProperties = nb.serverProperties();
			nettyServerBootstrap.setSpringApplicationContext(springApplicationContext)
			.setSpringServletContext(springServletContext)
			.setServerProperties(serverProperties);
		}
		nettyServerBootstrap.parseCommondArguments(args).start();
	}

	public void run(){
		// 创建->设置application父容器->设置springmvc子容器->设置server参数->启动
		//NettyServerBootstrap.createServer().setSpringApplicationContext(springApplicationContext).setSpringServletContext(springServletContext).parseCommondArguments(args).start();
	}
}
