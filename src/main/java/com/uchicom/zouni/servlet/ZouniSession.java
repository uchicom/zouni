package com.uchicom.zouni.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class ZouniSession implements HttpSession {

	private String id;

	private Map<String, Object> attributeMap = new HashMap<String, Object>();

	private long creationTime;

	private int maxInactiveInterval;

	private long lastAccessedTime;

	private boolean isNew;

	public ZouniSession(String id) {
		this.id = id;
		creationTime = System.currentTimeMillis();
	}
	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		maxInactiveInterval = interval;
	}

	@Override
	public Object getAttribute(String name) {
		return attributeMap.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Enumeration<String>() {
			private Iterator<String> iterator = attributeMap.keySet().iterator();
			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}

		};
	}

	@Override
	public void setAttribute(String name, Object attribute) {
		attributeMap.put(name, attribute);
	}

	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}

	@Override
	public void invalidate() {
		attributeMap.clear();
		ZouniServletContext.getInstance().removeSession(this);
		id = null;
	}

	@Override
	@Deprecated
	public Object getValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void putValue(String name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public void removeValue(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
