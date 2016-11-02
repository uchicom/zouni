package javax.servlet;

import java.util.Enumeration;

public interface ServletConfig {
	 
	public ServletContext getServletContext();
	public String getInitParameter(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames();
	public String getServletName();
}
