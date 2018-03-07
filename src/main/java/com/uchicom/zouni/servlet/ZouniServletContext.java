package com.uchicom.zouni.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
		watch(baseFile, "dir.");
		watch(pubFile, "pub.");

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
			rdMap.put(name, rd);
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


	Map<WatchKey, Path> pathMap = new HashMap<>();

	/**
	 *
	 * @param baseFile
	 */
	private void watch(File baseFile, String keyPrefix) {
		Thread thread = new Thread(() -> {
			try (WatchService service = FileSystems.getDefault().newWatchService()) {
				regist(service, baseFile);
				WatchKey key = null;
				while ((key = service.take()) != null) {
					// スレッドの割り込み = 終了要求を判定する. 必要なのか不明
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					if (!key.isValid())
						continue;
					Path basePath = pathMap.get(key);
					if (basePath == null) continue;
					for (WatchEvent<?> event : key.pollEvents()) {
						if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) continue;
						//eventではファイル名しかとれない
						Path file = (Path) event.context();
						//監視対象のフォルダを取得する必要がある
						Path real = basePath.resolve(file);
						String cpath = real.toFile().getCanonicalPath();
						String bpath = baseFile.getCanonicalPath();
						String absPath = cpath.substring(bpath.length()).replace('\\', '/');
						if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
							regist(service, real.toFile());
						} else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind()) ||
								StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
							// 削除時はcancel不要の認識
							if (servletMap.containsKey(keyPrefix + absPath)) {
								servletMap.remove(keyPrefix + absPath);
							}
						}
					}
					key.reset();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(false); //mainスレッドと運命を共に
		thread.start();
	}

	/**
	 *  監視サービスにフォルダを再起呼び出しして登録する
	 * @param service
	 * @param file
	 * @throws IOException
	 */
	public void regist(WatchService service, File file) throws IOException {
		if (file.isDirectory()) {
			Path path = file.toPath();
			pathMap.put(
					path.register(
							service, new Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
									StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE },
							new Modifier[] {}),
					path);
			for (File child : file.listFiles()) {
				regist(service, child);
			}
		}
	}

	/* (非 Javadoc)
	 * @see javax.servlet.ServletContext#getContextPath()
	 */
	@Override
	public String getContextPath() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/* (非 Javadoc)
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	@Override
	public Set getResourcePaths(String arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/* (非 Javadoc)
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	@Override
	public String getServletContextName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
