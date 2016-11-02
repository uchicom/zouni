package javax.servlet.http;

import java.util.Enumeration;

@Deprecated
public abstract interface HttpSessionContext {
	 
	// deprecated methods
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Enumeration getIds();
	@Deprecated
	public HttpSession getSession(String id);
}
