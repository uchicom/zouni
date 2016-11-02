package javax.servlet.http;

import java.security.Principal;
import java.util.Enumeration;

import javax.servlet.ServletRequest;

public interface HttpServletRequest extends ServletRequest {
	public String getAuthType();
	public Cookie[] getCookies();
	public long getDateHeader(String name);
	public String getHeader(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames();
	public int getIntHeader(String name);
	public String getMethod();
	public String getContextPath();
	public String getPathInfo();
	public String getPathTranslated();
	public String getQueryString();
	public String getRemoteUser();
	public Boolean isUserInRole(String role);
	public Principal getUserPrincipal();
	public String getRequestedSessionId();
	public boolean isRequestedSessionIdValid();
	public Boolean isRequestedSessionIdFromCookie();
	public Boolean isRequestedSessionIdFromURL();
	public String getRequestURI();
	public String getServletPath();
	public HttpSession getSession();
	public HttpSession getSession(Boolean create);
	 
	// deprecated methods
	@Deprecated
	public Boolean isRequestSessionIdFromUrl();
}
