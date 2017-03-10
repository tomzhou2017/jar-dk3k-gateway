package com.dk3k.framework.server.netty.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.server.WebContext;

public class NettyServletContext implements ServletContext {
	final Logger logger = LoggerFactory.getLogger(NettyServletContext.class);

	private WebContext webContext;
	private Map<String, ServletRegistration> servletRegistrations;
	private Map<String, String> initParameters;
	private Map<String, Object> attributes;

	public NettyServletContext(WebContext webContext) {
		this.webContext = webContext;
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public ServletContext getContext(String uripath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(String file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<String> getServletNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void log(String msg) {
		logger.debug(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		log(msg, exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		logger.debug("Message: '{}', Throwable: '{}'", message, throwable);
	}

	@Override
	public String getRealPath(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getInitParameter(String name) {
		return getInitParameters().get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new Vector<>(getInitParameters().keySet()).elements();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		if (getInitParameters().containsKey(name)) {
			return false;
		}
		getInitParameters().put(name, value);
		return true;
	}

	private Map<String, String> getInitParameters() {
		if (initParameters == null) {
			initParameters = new HashMap<>();
		}
		return initParameters;
	}

	@Override
	public Object getAttribute(String name) {
		return getAttributes().get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Vector<>(getAttributes().keySet()).elements();
	}

	private Map<String, Object> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		return attributes;
	}

	@Override
	public void setAttribute(String name, Object object) {
		if (object == null) {
			removeAttribute(name);
		} else {
			getAttributes().put(name, object);
		}
	}

	@Override
	public void removeAttribute(String name) {
		getAttributes().remove(name);
	}

	@Override
	public String getServletContextName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return getServletRegistrationsInternal().get(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return new HashMap<>(getServletRegistrationsInternal());
	}

	private Map<String, ServletRegistration> getServletRegistrationsInternal() {
		if (servletRegistrations == null) {
			servletRegistrations = new HashMap<>();
		}
		return servletRegistrations;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void declareRoles(String... roleNames) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVirtualServerName() {
		// TODO Auto-generated method stub
		return null;
	}

}
