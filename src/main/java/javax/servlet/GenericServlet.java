package javax.servlet;

import java.io.IOException;
import java.util.Enumeration;

public abstract class GenericServlet implements Servlet {

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
	public GenericServlet() {
		
	}
	 
	public String getInitParameter() {
		return null;
	}
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		return null;
	}
	public ServletContext gerServletContext() {
		return null;
	}
	public void init() {
		
	}
	public void init(ServletConfig config) throws ServletException {
		
	}
	public void log(String message) {
		
	}
	public void log(String message, Throwable cause) {
		
	}
	public abstract void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;


}
