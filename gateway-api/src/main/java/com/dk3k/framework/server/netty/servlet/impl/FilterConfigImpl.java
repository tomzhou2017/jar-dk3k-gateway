package com.dk3k.framework.server.netty.servlet.impl;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class FilterConfigImpl extends ConfigAdapter implements FilterConfig {

	public FilterConfigImpl(String filterName) {
		super(filterName);
	}

	@Override
	public String getFilterName() {
		return super.getOwnerName();
	}

	@Override
	public ServletContext getServletContext() {
		return ServletContextImpl.get();
	}

}
