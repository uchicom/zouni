// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class ZouniRequestDispatcher implements RequestDispatcher {

  private String name;
  private Map<String, Servlet> servletMap;

  public ZouniRequestDispatcher(String name, Map<String, Servlet> servletMap) {
    this.name = name;
    this.servletMap = servletMap;
  }

  ZipInputStream zis;

  @Override
  public void forward(ServletRequest req, ServletResponse res)
      throws ServletException, IOException {
    if (name.endsWith(".htm")) { // TODO 静的ファイルを保持しておく必要がある
      // 静的ファイル呼び出し
      try {
        file2Stream(new File(name), res.getOutputStream());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      // サーブレット呼び出し.
      Servlet servlet = getServlet(name, "pub.");
      servlet.service(req, res);
    }
  }

  @Override
  public void include(ServletRequest req, ServletResponse res)
      throws ServletException, IOException {}

  private Servlet getServlet(String name, String keyPrefix)
      throws FileNotFoundException, IOException {
    String key = null;
    if (name.charAt(0) == '/') {
      key = keyPrefix + name;
    } else {
      key = keyPrefix + "/" + name;
    }
    var servlet = servletMap.get(key);
    return servlet;
  }

  private void file2Stream(File file, ServletOutputStream sos) {
    try (FileInputStream fis = new FileInputStream(file); ) {
      byte[] bytes = new byte[1024 * 4];
      int length = 0;

      while ((length = fis.read(bytes)) > 0) {
        sos.write(bytes, 0, length);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
