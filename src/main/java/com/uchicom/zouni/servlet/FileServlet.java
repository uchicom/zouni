// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import com.uchicom.zouni.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * mime対応表の充実が必要.
 *
 * @author Shigeki Uchiyama
 */
public class FileServlet extends HttpServlet {

  /** */
  private static final long serialVersionUID = 1L;

  private File file;
  private String type;

  public FileServlet(File file) {
    this.file = file;
    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    type = Constants.mimeProperties.getProperty(ext);
    if (type == null) {
      type = Constants.DEFAULT_MIME;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType(type);
    // 解析した情報を元にデータを埋め込んで
    ServletOutputStream sos = res.getOutputStream();
    try (FileInputStream fis = new FileInputStream(file); ) {
      int length = 0;
      byte[] bytes = new byte[1024 * 4];
      while ((length = fis.read(bytes)) > 0) {
        sos.write(bytes, 0, length);
      }
    }
  }
}
