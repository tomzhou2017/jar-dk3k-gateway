package com.dk3k.framework.server.nettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.servlet.ServletException;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class DispatcherServletChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final DispatcherServlet dispatcherServlet;

    private int maxContentLength=65536;

    public DispatcherServletChannelInitializer(String servletContextPath) throws ServletException {
        MockServletContext servletContext = new MockServletContext();
        MockServletConfig servletConfig = new MockServletConfig(servletContext);
        servletConfig.addInitParameter("contextConfigLocation", servletContextPath);
        servletContext.addInitParameter("contextConfigLocation", servletContextPath);

        XmlWebApplicationContext wac = new XmlWebApplicationContext();
        wac.setServletContext(servletContext);
        wac.setServletConfig(servletConfig);
        wac.setConfigLocation(servletContextPath);
        wac.refresh();

        this.dispatcherServlet = new DispatcherServlet(wac);
        this.dispatcherServlet.init(servletConfig);
    }

    public DispatcherServletChannelInitializer(String servletContextPath,int maxContentLength) throws ServletException {
        this(servletContextPath);
        this.maxContentLength=maxContentLength;
    }

    public DispatcherServletChannelInitializer() throws ServletException {
        this("classpath:/applicationContext.xml");
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));//定义缓冲数据量
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());//块写入处理器
        pipeline.addLast("handler", new ServletNettyHandler(this.dispatcherServlet));

    }


    @Configuration
    @EnableWebMvc
    @ComponentScan(basePackages = "org.springframework.sandbox.mvc")
    static class WebConfig extends WebMvcConfigurerAdapter {
    }

}
