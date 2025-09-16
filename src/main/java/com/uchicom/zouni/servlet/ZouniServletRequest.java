// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ZouniServletRequest implements HttpServletRequest {
  private String method;
  private Socket socket;
  private String requestUri;
  private ZouniSession session;
  private Map<String, Value> valueMap = new HashMap<String, Value>();
  private boolean gzip;
  private boolean deflate;
  private ByteArrayInputStream bais;
  private Integer contentLength;
  private Cookie[] cookies;

  public ZouniServletRequest(Socket socket) {
    this.socket = socket;
    if (socket.isClosed()) return;
    try {
      BufferedReader br =
          new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      String head = br.readLine();
      String line = br.readLine();
      var sb = new StringBuilder(4 * 1024);
      sb.append(head);
      while (line != null && !"".equals(line)) {
        String[] headValue = line.split(": ", 2);
        Value value = new Value();
        value.setParameter(headValue[1]);
        valueMap.put("header." + headValue[0], value);
        sb.append(line);
        sb.append("\r\n");
        line = br.readLine();
      }
      if (sb.length() > 0) {
        sb.append("\r\n");
        Value cl = valueMap.get("header.Content-Length");
        if (cl != null) {
          contentLength = Integer.parseInt(cl.getParameter());
          char[] chars = new char[contentLength];
          int length = 0;
          int index = 0;
          while ((length = br.read(chars, index, chars.length - index)) > 0
              && index < chars.length) {
            index += length;
          }
          sb.append(chars);
        }
        String str = sb.toString();
        String[] heads = head.split(" ", 0);
        if (heads[0].equals("GET")) {
          this.method = "GET";
          this.requestUri = heads[1];
          int uriSeparatorIndex = requestUri.indexOf("?");
          if (uriSeparatorIndex >= 0) {
            setParameters(this.requestUri.substring(uriSeparatorIndex + 1));
            this.requestUri = this.requestUri.substring(0, uriSeparatorIndex);
          }
        } else if (heads[0].equals("POST")) {
          this.method = "POST";
          this.requestUri = heads[1];
          int startIndex = str.indexOf("\r\n\r\n") + 4;
          int lastIndex = str.indexOf("\r\n", startIndex);
          // multipart未対応
          if (lastIndex >= 0) {
            setParameters(str.substring(startIndex, lastIndex));
          } else {
            setParameters(str.substring(startIndex));
          }
          // Reader作成
          bais =
              new ByteArrayInputStream(str.substring(startIndex).getBytes(StandardCharsets.UTF_8));
        }
        Value ae = valueMap.get("header.Accept-Encoding");
        if (ae != null) {
          for (String enc : ae.getParameter().trim().split("[ ,]+", 0)) {
            if (!gzip && "gzip".equals(enc)) {
              gzip = true;
            } else if (!deflate && "deflate".equals(enc)) {
              deflate = true;
            }
          }
        }
        Value cv = valueMap.get("header.Cookie");
        if (cv != null) {
          var splitedCookies = cv.getParameter().split(";", 0);
          this.cookies = new Cookie[splitedCookies.length];
          for (var i = 0; i < splitedCookies.length; i++) {
            String cookie = splitedCookies[i];
            String[] keyValue = cookie.trim().split("=", 0);
            String value = keyValue.length == 1 ? null : keyValue[1];
            this.cookies[i] = new Cookie(keyValue[0], value);
            if (keyValue[0].equals("JSESSIONID")) {
              if (session == null) {
                this.session = ZouniServletContext.getInstance().getSession(value);
              }
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setParameters(String parameters) {
    for (String split : parameters.split("&", 0)) {
      int index = split.indexOf("=");
      if (index > 0) {
        String key = "param." + split.substring(0, index);
        if (index == split.length() - 1) {
          valueMap.put(key, null);
        } else {
          String val = split.substring(index + 1);
          if (valueMap.containsKey(key)) {
            Value value = valueMap.get(key);
            if (value != null) {
              if (value.getParameters() == null) {
                List<String> list = new ArrayList<>();
                list.add(value.getParameter());
                list.add(val);
                value.setParameters(list);
              } else {
                value.getParameters().add(val);
              }
            }
          } else {
            Value value = new Value();
            value.setParameter(val);
            valueMap.put(key, value);
          }
        }
      }
    }
  }

  @Override
  public Object getAttribute(String name) {
    if (valueMap.containsKey("attribute." + name)) {
      return valueMap.get("attribute." + name).getAttribute();
    }
    return null;
  }

  @Override
  public void setAttribute(String name, Object attribute) {
    Value value = new Value();
    value.setAttribute(attribute);
    valueMap.put("attribute." + name, value);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeAttribute(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Locale getLocale() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getContentLength() {
    return contentLength;
  }

  @Override
  public String getContentType() {
    var value = valueMap.get("header.Content-Type");
    if (value == null) {
      return null;
    }
    return value.getParameter();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ZouniServletInputStream(bais);
  }

  @Override
  public String getParameter(String name) {
    Value value = valueMap.get("param." + name);
    if (value != null && value.getParameter() != null) {
      try {
        return URLDecoder.decode(value.getParameter(), "utf-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Override
  public String[] getParameterValues(String key) {

    Value value = valueMap.get("param." + key);
    if (value != null) {
      if (value.getParameters() != null) {
        try {
          String[] values = new String[value.getParameters().size()];
          for (int i = 0; i < values.length; i++) {
            values[i] = URLDecoder.decode(value.getParameters().get(i), "utf-8");
          }
          return values;
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      } else if (value.getParameter() != null) {
        try {
          return new String[] {URLDecoder.decode(value.getParameter(), "utf-8")};
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

  @Override
  public String getProtocol() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getScheme() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getServerName() {
    return null;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8));
  }

  @Override
  public String getRemoteAddr() {
    return socket.getInetAddress().getHostAddress();
  }

  @Override
  public String getRemoteHost() {
    return socket.getInetAddress().getHostName();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getAuthType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Cookie[] getCookies() {
    return cookies;
  }

  @Override
  public long getDateHeader(String name) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getHeader(String name) {
    if (valueMap.containsKey("header." + name)) {
      return valueMap.get("header." + name).getParameter();
    }
    return null;
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getIntHeader(String name) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public String getContextPath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPathInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPathTranslated() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getQueryString() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteUser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Principal getUserPrincipal() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRequestURI() {
    return requestUri;
  }

  @Override
  public String getServletPath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpSession getSession() {
    return session;
  }

  public boolean isGzip() {
    return gzip;
  }

  public void setGzip(boolean gzip) {
    this.gzip = gzip;
  }

  public boolean isDeflate() {
    return deflate;
  }

  public void setDeflate(boolean deflate) {
    this.deflate = deflate;
  }

  @Override
  public String getLocalAddr() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public String getLocalName() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public int getLocalPort() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return null;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return null;
  }

  @Override
  public int getRemotePort() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  @Override
  public int getServerPort() {
    return socket.getLocalPort();
  }

  @Override
  public boolean isSecure() {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  public StringBuffer getRequestURL() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public HttpSession getSession(boolean arg0) {
    if (arg0 && session == null) {
      session = ZouniServletContext.getInstance().createSession();
    }
    return session;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public boolean isUserInRole(String arg0) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public long getContentLengthLong() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getContentLengthLong'");
  }

  @Override
  public ServletContext getServletContext() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getServletContext'");
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
  }

  @Override
  public boolean isAsyncStarted() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isAsyncStarted'");
  }

  @Override
  public boolean isAsyncSupported() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isAsyncSupported'");
  }

  @Override
  public AsyncContext getAsyncContext() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAsyncContext'");
  }

  @Override
  public DispatcherType getDispatcherType() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getDispatcherType'");
  }

  @Override
  public String getRequestId() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getRequestId'");
  }

  @Override
  public String getProtocolRequestId() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getProtocolRequestId'");
  }

  @Override
  public ServletConnection getServletConnection() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getServletConnection'");
  }

  @Override
  public String changeSessionId() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'changeSessionId'");
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
  }

  @Override
  public void login(String username, String password) throws ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'login'");
  }

  @Override
  public void logout() throws ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'logout'");
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getParts'");
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPart'");
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
      throws IOException, ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'upgrade'");
  }
}
