// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
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
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZouniServletContext implements ServletContext {

  private static ZouniServletContext context;
  private Map<String, Servlet> servletMap = new ConcurrentHashMap<String, Servlet>();
  private Map<String, RequestDispatcher> rdMap = new ConcurrentHashMap<String, RequestDispatcher>();
  private Map<String, ZouniSession> sessionMap = new ConcurrentHashMap<String, ZouniSession>();
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
  public Enumeration<String> getInitParameterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<String> getAttributeNames() {
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
   * @param baseFile
   */
  private void watch(File baseFile, String keyPrefix) {
    Thread thread =
        new Thread(
            () -> {
              try (WatchService service = FileSystems.getDefault().newWatchService()) {
                regist(service, baseFile);
                WatchKey key = null;
                while ((key = service.take()) != null) {
                  // スレッドの割り込み = 終了要求を判定する. 必要なのか不明
                  if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                  }
                  if (!key.isValid()) continue;
                  Path basePath = pathMap.get(key);
                  if (basePath == null) continue;
                  for (WatchEvent<?> event : key.pollEvents()) {
                    if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) continue;
                    // eventではファイル名しかとれない
                    Path file = (Path) event.context();
                    // 監視対象のフォルダを取得する必要がある
                    Path real = basePath.resolve(file);
                    String cpath = real.toFile().getCanonicalPath();
                    String bpath = baseFile.getCanonicalPath();
                    String absPath = cpath.substring(bpath.length()).replace('\\', '/');
                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                      regist(service, real.toFile());
                    } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())
                        || StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
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
    thread.setDaemon(false); // mainスレッドと運命を共に
    thread.start();
  }

  /**
   * 監視サービスにフォルダを再起呼び出しして登録する
   *
   * @param service
   * @param file
   * @throws IOException
   */
  public void regist(WatchService service, File file) throws IOException {
    if (file.isDirectory()) {
      Path path = file.toPath();
      pathMap.put(
          path.register(
              service,
              new Kind[] {
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
              },
              new Modifier[] {}),
          path);
      for (File child : file.listFiles()) {
        regist(service, child);
      }
    }
  }

  /* (非 Javadoc)
   * @see jakarta.servlet.ServletContext#getContextPath()
   */
  @Override
  public String getContextPath() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  /* (非 Javadoc)
   * @see jakarta.servlet.ServletContext#getResourcePaths(java.lang.String)
   */
  @Override
  public Set<String> getResourcePaths(String arg0) {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  /* (非 Javadoc)
   * @see jakarta.servlet.ServletContext#getServletContextName()
   */
  @Override
  public String getServletContextName() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public int getEffectiveMajorVersion() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getEffectiveMajorVersion'");
  }

  @Override
  public int getEffectiveMinorVersion() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getEffectiveMinorVersion'");
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setInitParameter'");
  }

  @Override
  public Dynamic addServlet(String servletName, String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addServlet'");
  }

  @Override
  public Dynamic addServlet(String servletName, Servlet servlet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addServlet'");
  }

  @Override
  public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addServlet'");
  }

  @Override
  public Dynamic addJspFile(String servletName, String jspFile) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addJspFile'");
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createServlet'");
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getServletRegistration'");
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getServletRegistrations'");
  }

  @Override
  public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addFilter'");
  }

  @Override
  public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addFilter'");
  }

  @Override
  public jakarta.servlet.FilterRegistration.Dynamic addFilter(
      String filterName, Class<? extends Filter> filterClass) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addFilter'");
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createFilter'");
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getFilterRegistration'");
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getFilterRegistrations'");
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getSessionCookieConfig'");
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setSessionTrackingModes'");
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'getDefaultSessionTrackingModes'");
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'getEffectiveSessionTrackingModes'");
  }

  @Override
  public void addListener(String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addListener'");
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addListener'");
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addListener'");
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createListener'");
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getJspConfigDescriptor'");
  }

  @Override
  public ClassLoader getClassLoader() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getClassLoader'");
  }

  @Override
  public void declareRoles(String... roleNames) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'declareRoles'");
  }

  @Override
  public String getVirtualServerName() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getVirtualServerName'");
  }

  @Override
  public int getSessionTimeout() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getSessionTimeout'");
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setSessionTimeout'");
  }

  @Override
  public String getRequestCharacterEncoding() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getRequestCharacterEncoding'");
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setRequestCharacterEncoding'");
  }

  @Override
  public String getResponseCharacterEncoding() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getResponseCharacterEncoding'");
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setResponseCharacterEncoding'");
  }
}
