package com.uchicom.zouni.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class ZouniServletContext implements ServletContext {

	private static ZouniServletContext context;
	private Map<String, Servlet> servletMap = new ConcurrentHashMap<String, Servlet>();
	private Map<String, RequestDispatcher> rdMap = new ConcurrentHashMap<String, RequestDispatcher>();
	private Map<String, ZouniSession> sessionMap =  new ConcurrentHashMap<String, ZouniSession>();
	private File baseFile;
	private File pubFile;
	private ZouniServletContext(File baseFile, File pubFile) {
		this.baseFile = baseFile;
		this.pubFile = pubFile;
	}

	public static void init(File baseFile, File pubFile) {
		context = new ZouniServletContext(baseFile, pubFile);
	}
	public static ZouniServletContext getInstance() {
		return context;
	}
	public Map<String, Servlet> getServletMap() {
		return servletMap;
	}
	@Override
	public String getMimeType(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String name) {
		RequestDispatcher rd = null;
		if (rdMap.containsKey(name)) {
			rd = rdMap.get(name);
		} else {
			rd = new ZouniRequestDispatcher(name, servletMap, baseFile);
		}
		return rd;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String path) {
		try {
			return new File(pubFile, path).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ServletContext getContext(String uripath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<?> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<?> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String name, Object attribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void log(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String message, Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public Servlet getServlet(String name) throws ServletException {

		return null;
	}

	@Override
	public Enumeration<?> getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<?> getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(Exception exception, String message) {
		// TODO Auto-generated method stub

	}
	public ZouniSession getSession(String key) {
		System.out.println(sessionMap);
		return sessionMap.get(key);
	}
	public ZouniSession createSession() {
		ZouniSession session = new ZouniSession(String.valueOf(System.currentTimeMillis()));
		sessionMap.put(session.getId(), session);
		return session;
	}
	public void removeSession(ZouniSession session) {
		sessionMap.remove(session.getId());
	}

}
