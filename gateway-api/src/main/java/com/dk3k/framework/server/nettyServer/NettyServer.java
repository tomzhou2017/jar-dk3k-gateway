package com.dk3k.framework.server.nettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    // 定义spring 全局配置文件
    private static final String SPRING_APPLICATION_CONFIG = "classpath:/applicationContext.xml";
    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
    // 默认端口8080
    private int port = 8080;
    // 默认配置文件（基于spring，ContextLoadListener的contextParams）
    private String servletContext = SPRING_APPLICATION_CONFIG;
    //Http请求内容的最大值（单位字节），默认64K
    private int maxContentLength = 65536;

    public NettyServer() {

    }

    public NettyServer(int port) {
        if (0 != port) {
            this.port = port;
        }

    }

    public NettyServer(int port, int maxContentLength) {
        if (0 != port) {
            this.port = port;
        }
        if (maxContentLength > 1024) {
            this.maxContentLength = maxContentLength;
        }

    }

    public NettyServer(int port, String servletContext) {
        this(port);
        if (null != servletContext) {
            this.servletContext = servletContext;
        }
    }

    public NettyServer(int port, int maxContentLength, String servletContext) {
        this(port, maxContentLength);
        if (null != servletContext) {
            this.servletContext = servletContext;
        }
    }

    public void start() throws Exception {
        boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
        ServerBootstrap server = new ServerBootstrap();

        EventLoopGroup bossGroup = linux ? new EpollEventLoopGroup(ioWorkerCount) : new NioEventLoopGroup(ioWorkerCount);
        EventLoopGroup workerGroup = linux ? new EpollEventLoopGroup(ioWorkerCount) : new NioEventLoopGroup(ioWorkerCount);

        try {
            server.group(bossGroup, workerGroup);
            server.channel(linux ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

            //在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，TCP总是希望尽可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
            server.option(ChannelOption.TCP_NODELAY, true);

            //超时时间 ，这个设置保证当达到60000(1分钟)毫秒时，就释放channel,这样内存就不会需要等待很长时间释放
            server.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000*60);

            //childOption()是提供给由父管道ServerChannel接收到的连接，也就是worker线程
            server.childOption(ChannelOption.SO_KEEPALIVE, true);

            server.childHandler(new DispatcherServletChannelInitializer(servletContext, maxContentLength));
            ChannelFuture future = server.bind(port).sync();
            logger.info("Started NettyServer@0.0.0.0:" + port);

            future.channel().closeFuture().sync();

        } finally {
            logger.error("Started NettyServer Failed@0.0.0.0:" + port);

            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    protected boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");

        if (os == null) {
            return false;
        }
        return os.startsWith(osNamePrefix);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServletContext() {
        return servletContext;
    }

    public void setServletContext(String servletContext) {
        this.servletContext = servletContext;
    }

}
