package com.uchicom.zouni.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ZouniServletRequest implements HttpServletRequest {
	private String method;
	private Socket socket;
	private String requestUri;
	private ZouniSession session;
	private Map<String, Value> valueMap = new HashMap<String, Value>();
	private boolean gzip;
	private boolean deflate;
	private ByteArrayInputStream bais;

	public ZouniServletRequest(Socket socket) {
		this.socket = socket;
		if (socket.isClosed())
			return;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String head = br.readLine();
			String line = br.readLine();
			StringBuffer sb = new StringBuffer(4 * 1024);
			sb.append(head);
			while (line != null && !"".equals(line)) {
				String[] headValue = line.split(": ");
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
					int contentLength = Integer.parseInt(cl.getParameter());
					char[] chars = new char[contentLength];
					int length = 0;
					int index = 0;
					while ((length = br.read(chars, index, chars.length - index)) > 0 && index < chars.length) {
						index += length;
					}
					sb.append(chars);
				}
				String str = sb.toString();
				String[] heads = head.split(" ");
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
					bais = new ByteArrayInputStream(str.substring(startIndex).getBytes());
				}
				Value ae = valueMap.get("header.Accept-Encoding");
				if (ae != null) {
					for (String enc : ae.getParameter().trim().split("[ ,]+")) {
						if (!gzip && "gzip".equals(enc)) {
							gzip = true;
						} else if (!deflate && "deflate".equals(enc)) {
							deflate = true;
						}
					}
				}
				Value cv = valueMap.get("header.Cookie");
				if (cv != null) {
					for (String cookie : cv.getParameter().split(";")) {
						String[] keyValue = cookie.trim().split("=");
						if (keyValue[0].equals("JSESSIONID")) {
							if (session == null) {
								this.session = ZouniServletContext.getInstance().getSession(keyValue[1]);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setParameters(String parameters) {
		for (String split : parameters.split("&")) {
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
	public Enumeration<?> getAttributeNames() {
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
	public Enumeration<?> getLocales() {
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String name) {
		Value value = valueMap.get("param." + name);
		if (value != null && value.getParameter() != null) {
			try {
				return URLDecoder.decode(value.getParameter(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
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
					e.printStackTrace();
				}
			} else if (value.getParameter() != null) {
				try {
					return new String[] { URLDecoder.decode(value.getParameter(), "utf-8") };
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
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
		return new BufferedReader(new InputStreamReader(bais));
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
		// TODO Auto-generated method stub
		return null;
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
	public Enumeration<?> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<?> getHeaderNames() {
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

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	@Override
	public String getLocalAddr() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	@Override
	public String getLocalName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	@Override
	public int getLocalPort() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	@Override
	public Map<String, String> getParameterMap() {
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	@Override
	public Enumeration<?> getParameterNames() {
		Vector<String> parameterNameList = new Vector<>(valueMap.size());
		for (String key : valueMap.keySet()) {
			if (key.startsWith("param.")) {
				try {
					parameterNameList.add(URLDecoder.decode(key.substring(6), "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return parameterNameList.elements();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	@Override
	public String getRealPath(String arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	@Override
	public int getRemotePort() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	@Override
	public int getServerPort() {
		return socket.getLocalPort();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	@Override
	public boolean isSecure() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		// TODO 自動生成されたメソッド・スタブ

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	@Override
	public StringBuffer getRequestURL() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	@Override
	public HttpSession getSession(boolean arg0) {
		if (arg0 && session == null) {
			session = ZouniServletContext.getInstance().createSession();
		}
		return session;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	@Override
	public boolean isUserInRole(String arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
