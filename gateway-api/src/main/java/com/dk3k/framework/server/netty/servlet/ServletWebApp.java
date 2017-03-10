package com.dk3k.framework.server.netty.servlet;

import java.io.File;
import java.util.Map;

import com.dk3k.framework.server.netty.servlet.config.FilterConfiguration;
import com.dk3k.framework.server.netty.servlet.config.ServletConfiguration;
import com.dk3k.framework.server.netty.servlet.config.ServletContextListenerConfiguration;
import com.dk3k.framework.server.netty.servlet.config.WebAppConfiguration;
import com.dk3k.framework.server.netty.servlet.impl.FilterChainImpl;
import com.dk3k.framework.server.netty.servlet.impl.ServletContextImpl;

import io.netty.channel.group.ChannelGroup;

/**
 * 
 * @description <code>ChannelInitializerWrapper</code>是Channel Handler与业务容器的包装类
 *
 */
public class ServletWebApp {

	private static ServletWebApp instance;

	private WebAppConfiguration webAppConfig;

	private ChannelGroup sharedChannelGroup;

	public static ServletWebApp get() {

		if (instance == null)
			instance = new ServletWebApp();

		return instance;
	}

	private ServletWebApp() {
	}

	public void init(WebAppConfiguration webapp, ChannelGroup sharedChannelGroup) {
		this.webAppConfig = webapp;
		this.sharedChannelGroup = sharedChannelGroup;
		this.initServletContext();
		this.initContextListeners();
		this.initFilters();
		this.initServlets();
	}

	public void destroy() {
		this.destroyServlets();
		this.destroyFilters();
		this.destroyContextListeners();
	}

	private void initContextListeners() {
		if (webAppConfig.getServletContextListenerConfigurations() != null) {
			for (ServletContextListenerConfiguration ctx : webAppConfig.getServletContextListenerConfigurations()) {
				ctx.init();
			}
		}
	}

	private void destroyContextListeners() {
		if (webAppConfig.getServletContextListenerConfigurations() != null) {
			for (ServletContextListenerConfiguration ctx : webAppConfig.getServletContextListenerConfigurations()) {
				ctx.destroy();
			}
		}
	}

	private void destroyServlets() {
		if (webAppConfig.getServletConfigurations() != null) {
			for (ServletConfiguration servlet : webAppConfig.getServletConfigurations()) {
				servlet.destroy();
			}
		}
	}

	private void destroyFilters() {
		if (webAppConfig.getFilterConfigurations() != null) {
			for (FilterConfiguration filter : webAppConfig.getFilterConfigurations()) {
				filter.destroy();
			}
		}
	}

	protected void initServletContext() {
		ServletContextImpl ctx = ServletContextImpl.get();
		ctx.setServletContextName(this.webAppConfig.getName());

		if (webAppConfig.getContextParameters() != null) {
			for (Map.Entry<String, String> entry : webAppConfig.getContextParameters().entrySet()) {
				ctx.addInitParameter(entry.getKey(), entry.getValue());
			}
		}
	}

	protected void initFilters() {
		if (webAppConfig.getFilterConfigurations() != null) {
			for (FilterConfiguration filter : webAppConfig.getFilterConfigurations()) {
				filter.init();
			}
		}
	}

	protected void initServlets() {
		if (webAppConfig.hasServletConfigurations()) {
			for (ServletConfiguration servlet : webAppConfig.getServletConfigurations()) {
				servlet.init();
			}
		}
	}

	public FilterChainImpl initializeChain(String uri) {
		ServletConfiguration servletConfiguration = this.findServlet(uri);
		FilterChainImpl chain = new FilterChainImpl(servletConfiguration);

		if (this.webAppConfig.hasFilterConfigurations()) {
			for (FilterConfiguration s : this.webAppConfig.getFilterConfigurations()) {
				if (s.matchesUrlPattern(uri))
					chain.addFilterConfiguration(s);
			}
		}

		return chain;
	}

	private ServletConfiguration findServlet(String uri) {

		if (!this.webAppConfig.hasServletConfigurations()) {
			return null;
		}

		for (ServletConfiguration s : this.webAppConfig.getServletConfigurations()) {
			if (s.matchesUrlPattern(uri))
				return s;
		}

		return null;
	}

	public File getStaticResourcesFolder() {
		return this.webAppConfig.getStaticResourcesFolder();
	}

	public WebAppConfiguration getWebappConfig() {
		return webAppConfig;
	}

	public ChannelGroup getSharedChannelGroup() {
		return sharedChannelGroup;
	}
}
