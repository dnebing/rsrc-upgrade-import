package com.liferay.exportimport.resources.importer.custom.context;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

/**
 * class ServletContextWrapper: A class which wraps a ServletContext that basically passes
 * all method calls to the original wrapped instance.  This will allow subclasses to override
 * specific methods to do specific things but leave the other methods alone.  Should make for
 * easier to read code.
 *
 * @author dnebinger
 */
public class ServletContextWrapper implements ServletContext {
	protected final ServletContext _wrappedServletContext;

	public ServletContextWrapper(final ServletContext servletContext) {
		super();

		_wrappedServletContext = servletContext;
	}

	@Override
	public String getContextPath() {
		return _wrappedServletContext.getContextPath();
	}

	@Override
	public ServletContext getContext(String uripath) {
		return _wrappedServletContext.getContext(uripath);
	}

	@Override
	public int getMajorVersion() {
		return _wrappedServletContext.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return _wrappedServletContext.getMinorVersion();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return _wrappedServletContext.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return _wrappedServletContext.getEffectiveMinorVersion();
	}

	@Override
	public String getMimeType(String file) {
		return _wrappedServletContext.getMimeType(file);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return _wrappedServletContext.getResourcePaths(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return _wrappedServletContext.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return _wrappedServletContext.getResourceAsStream(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return _wrappedServletContext.getRequestDispatcher(path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return _wrappedServletContext.getNamedDispatcher(name);
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return _wrappedServletContext.getServlet(name);
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return _wrappedServletContext.getServlets();
	}

	@Override
	public Enumeration<String> getServletNames() {
		return _wrappedServletContext.getServletNames();
	}

	@Override
	public void log(String msg) {
		_wrappedServletContext.log(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		_wrappedServletContext.log(exception, msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		_wrappedServletContext.log(message, throwable);
	}

	@Override
	public String getRealPath(String path) {
		return _wrappedServletContext.getRealPath(path);
	}

	@Override
	public String getServerInfo() {
		return _wrappedServletContext.getServerInfo();
	}

	@Override
	public String getInitParameter(String name) {
		return _wrappedServletContext.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return _wrappedServletContext.getInitParameterNames();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return _wrappedServletContext.setInitParameter(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return _wrappedServletContext.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return _wrappedServletContext.getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object object) {
		_wrappedServletContext.setAttribute(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		_wrappedServletContext.removeAttribute(name);
	}

	@Override
	public String getServletContextName() {
		return _wrappedServletContext.getServletContextName();
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, String className) {
		return _wrappedServletContext.addServlet(servletName, className);
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
		return _wrappedServletContext.addServlet(servletName, servlet);
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		return _wrappedServletContext.addServlet(servletName, servletClass);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		return _wrappedServletContext.createServlet(clazz);
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return _wrappedServletContext.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return _wrappedServletContext.getServletRegistrations();
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, String className) {
		return _wrappedServletContext.addFilter(filterName, className);
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		return _wrappedServletContext.addFilter(filterName, filter);
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		return _wrappedServletContext.addFilter(filterName, filterClass);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		return _wrappedServletContext.createFilter(clazz);
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return _wrappedServletContext.getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return _wrappedServletContext.getFilterRegistrations();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return _wrappedServletContext.getSessionCookieConfig();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		_wrappedServletContext.setSessionTrackingModes(sessionTrackingModes);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return _wrappedServletContext.getDefaultSessionTrackingModes();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return _wrappedServletContext.getEffectiveSessionTrackingModes();
	}

	@Override
	public void addListener(String className) {
		_wrappedServletContext.addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		_wrappedServletContext.addListener(t);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		_wrappedServletContext.addListener(listenerClass);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		return _wrappedServletContext.createListener(clazz);
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return _wrappedServletContext.getJspConfigDescriptor();
	}

	@Override
	public ClassLoader getClassLoader() {
		return _wrappedServletContext.getClassLoader();
	}

	@Override
	public void declareRoles(String... roleNames) {
		_wrappedServletContext.declareRoles(roleNames);
	}
}
