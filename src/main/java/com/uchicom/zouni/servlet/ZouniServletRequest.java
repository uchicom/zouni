package com.uchicom.zouni.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
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
	private ZouniSession session = new ZouniSession();
	private Map<String, String> parameterMap = new HashMap<String, String>();
	private boolean gzip;
	private boolean deflate;
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
				if (index == split.length() - 1) {
					parameterMap.put(split.substring(0, index), null);
				} else {
					parameterMap.put(split.substring(0, index), split.substring(index + 1));
				}
			}
		}
		System.out.println(parameterMap);

	}
	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String name, Object attribute) {
		// TODO Auto-generated method stub
		return null;
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
		String value = parameterMap.get(name);
		if (value != null) {
			try {
				return URLDecoder.decode(parameterMap.get(name), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO 自動生成された catch ブロック
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
	public String getParameterValues() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
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
		return session;
	}

	@Override
	public HttpSession getSession(Boolean create) {
		// TODO Auto-generated method stub
		return null;
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
