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
import java.util.Locale;

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
    var name = file.getName();
    var dot = name.lastIndexOf('.');
    if (dot < 0) {
      type = Constants.DEFAULT_MIME;
    } else {
      type =
          Constants.mimeProperties.getProperty(
              name.substring(dot + 1).toLowerCase(Locale.ROOT), Constants.DEFAULT_MIME);
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
    if (!file.isFile() || !file.canRead()) {
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    res.setContentType(type);
    // 長さを先に宣言し、ファイルの内容をバッファせずに順次送信する
    res.setContentLengthLong(file.length());
    res.setDateHeader("Last-Modified", file.lastModified());
    ServletOutputStream sos = res.getOutputStream();
    try (FileInputStream fis = new FileInputStream(file); ) {
      int length = 0;
      byte[] bytes = new byte[1024 * 16];
      while ((length = fis.read(bytes)) > 0) {
        sos.write(bytes, 0, length);
      }
    }
  }
}
