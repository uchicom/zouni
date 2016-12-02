package javax.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

public interface ServletRequest {
	public Object getAttribute(String name);
	public void setAttribute(String name, Object attribute);
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames();
	public void removeAttribute(String name);
	public Locale getLocale();
	@SuppressWarnings("rawtypes")
	public Enumeration getLocales();
	public String getCharacterEncoding();
	public int getContentLength();
	public String getContentType();
	public ServletInputStream getInputStream() throws IOException;
	public String getParameter(String name);
	public String getParameterNames();
	public String[] getParameterValues(String name);
	public String getProtocol();
	public String getScheme();
	public String getServerName();
	public String gerServerPort();
	public BufferedReader getReader() throws IOException;
	public String getRemoteAddr();
	public String getRemoteHost();
	public Boolean isSecure();
	public RequestDispatcher getRequestDispatcher(String path);

	// deprecated methods
	public String getRaelPath();

}
