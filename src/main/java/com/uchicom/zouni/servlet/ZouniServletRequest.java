package com.uchicom.zouni.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		InputStream is = null;
		if (socket.isClosed()) return;
		try {
			is = socket.getInputStream();

			byte[] bytes = new byte[1024 * 4];
			int length = is.read(bytes);

			if (length != -1) {
				String str = new String(bytes, 0, length);
				int contentLengthIndex = str.indexOf("Content-Length:");
				if (contentLengthIndex >= 0) {
					int contentLength = Integer.parseInt(str.substring(contentLengthIndex + 16, str.indexOf("\r\n", contentLengthIndex + 16)));
					int endIndex = str.indexOf("\r\n\r\n");
					if (endIndex >= 0) {
						String[] heads = str.substring(0, endIndex).split("\r\n");
						for (int i = 1; i < heads.length; i++) {
							String[] headValue = heads[i].split(": ");
							Value value = new Value();
							value.setParameter(headValue[1]);
							valueMap.put("header." + headValue[0], value);
						}
						if (length < endIndex + 4 + contentLength) {
							length = is.read(bytes);
							str = str + new String(bytes, 0, length);
						}
					}
				}
				if (str.startsWith("GET")) {
					this.method = "GET";
					this.requestUri = str.substring(4, str.indexOf(' ', 4));
					int uriSeparatorIndex = requestUri.indexOf("?");
					if (uriSeparatorIndex >= 0) {
						setParameters(this.requestUri.substring(uriSeparatorIndex + 1));
						this.requestUri = this.requestUri.substring(0, uriSeparatorIndex);
					}
				} else if (str.startsWith("POST")) {
					this.method = "POST";
					this.requestUri = str.substring(5, str.indexOf(' ', 5));
					int startIndex = str.indexOf("\r\n\r\n") + 4;
					int lastIndex = str.indexOf("\r\n", startIndex);
					//multipart未対応
					if (lastIndex>=0) {
						setParameters(str.substring(startIndex, lastIndex));
					} else {
						setParameters(str.substring(startIndex));
					}
					//Reader作成
					bais = new ByteArrayInputStream(str.substring(startIndex).getBytes());
				}
				int index = str.indexOf("Accept-Encoding:");
				if (index >= 0) {
					int lastIndex = str.indexOf("\r\n", index);
					System.out.println("[" + str.substring(index + 16, lastIndex) + "]");
					for (String enc : str.substring(index + 16, lastIndex).trim().split("[ ,]+")) {
						if (!gzip && "gzip".equals(enc)) {
							gzip = true;
						} else if (!deflate && "deflate".equals(enc)){
							deflate = true;
						}
					}
				}
				index = str.indexOf("Cookie:");
				if (index >= 0) {
					int lastIndex = str.indexOf("\r\n", index);
					System.out.println("[" + str.substring(index + 7, lastIndex) + "]");
					for (String cookie : str.substring(index + 7, lastIndex).trim().split(";")) {
						System.out.println("Cookie:" + cookie);
						String[] keyValue = cookie.split("=");
						if (keyValue[0].equals("JSESSIONID")) {
							System.out.println("JSESSIONID:" + cookie);
							this.session = ZouniServletContext.getInstance().getSession(keyValue[1]);
							System.out.println("session:" +  session);
						}
					}
				}
				System.out.println(str);
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
				String key = split.substring(0, index);
				if (index == split.length() - 1) {
					valueMap.put("param." + key, null);
				} else {
					if (valueMap.containsKey("param." + key)) {
						Value value = valueMap.get("param." + key);
						if (value != null) {
							if (value.getParameters() == null) {
								List<String> list = new ArrayList<String>();
								list.add(value.getParameter());
								list.add(split.substring(index + 1));
							} else {
								value.getParameters().add(split.substring(index + 1));
							}
						}
					} else {
						Value value = new Value();
						value.setParameter(split.substring(index + 1));
						valueMap.put("param." + key, value);
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
	public String getParameterNames() {
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
					return new String[]{URLDecoder.decode(value.getParameter(), "utf-8")};
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
	public String gerServerPort() {
		return String.valueOf(socket.getLocalPort());
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
	public Boolean isSecure() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRaelPath() {
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
		return valueMap.get("header." + name).getParameter();
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
	public Boolean isUserInRole(String role) {
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
	public Boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return null;
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
		System.out.println(session);
		return session;
	}

	@Override
	public HttpSession getSession(Boolean create) {
		session = ZouniServletContext.getInstance().createSession();
		return session;
	}

	@Override
	public Boolean isRequestSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return null;
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

}
