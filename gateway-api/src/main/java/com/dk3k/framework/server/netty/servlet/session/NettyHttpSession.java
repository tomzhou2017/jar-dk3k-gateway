package com.dk3k.framework.server.netty.servlet.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NettyHttpSession implements HttpSession {

	private Map<String, Object> attributes;
	private ServletContext servletContext;

	public NettyHttpSession(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return getAttributes().get(name);
	}

	private Map<String, Object> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		return attributes;
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Vector<>(getAttributes().keySet()).elements();
	}

	public Collection<String> getAttributeNamesAsCollection() {
		return new ArrayList<>(getAttributes().keySet());
	}

	@Override
	public String[] getValueNames() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (value == null) {
			removeAttribute(name);
		} else {
			getAttributes().put(name, value);
		}
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		getAttributes().remove(name);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
	}

	@Override
	public boolean isNew() {
		return false;
	}

}
