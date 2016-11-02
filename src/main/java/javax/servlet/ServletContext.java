package javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public interface ServletContext {
	
	public String getMimeType(String filename);
	public URL getResource(String path) throws MalformedURLException;
	public InputStream getResourceAsStream(String path);
	public RequestDispatcher getRequestDispatcher(String name);
	public RequestDispatcher getNamedDispatcher(String name);
	public String getRealPath(String path);
	public ServletContext getContext(String uripath);
	public String getServerInfo();
	public String getInitParameter(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames();
	public Object getAttribute(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames();
	public void setAttribute(String name, Object attribute);
	public void removeAttribute(String name);
	public int getMajorVersion();
	public int getMinorVersion();
	public void log(String message);
	public void log(String message, Throwable cause);
	 
	//deprecated methods
	@Deprecated
	public Servlet getServlet(String name) throws ServletException;
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Enumeration getServlets();
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Enumeration getServletNames();
	@Deprecated
	public void log(Exception exception, String message);
}
