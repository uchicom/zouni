// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

  /* (非 Javadoc)
   * @see jakarta.servlet.http.HttpSession#getServletContext()
   */
  @Override
  public ServletContext getServletContext() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }
}
