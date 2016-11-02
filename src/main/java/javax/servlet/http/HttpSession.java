package javax.servlet.http;

import java.util.Enumeration;

public interface HttpSession {
	public long getCreationTime();
	public String getId();
	public long getLastAccessedTime();
	public boolean isNew();
	public int getMaxInactiveInterval();
	public void setMaxInactiveInterval(int interval);
	public Object getAttribute(String name);
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames();
	public void setAttribute(String name, Object attribute);
	public void removeAttribute(String name);
	public  void invalidate();
	 
	// deprecated methods
	@Deprecated
	public Object getValue(String name);
	@Deprecated
	public String[] getValueNames();
	@Deprecated
	public void putValue(String name, Object value);
	@Deprecated
	public void removeValue(String name);
	@Deprecated
	public HttpSessionContext getSessionContext();
}
