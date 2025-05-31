// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class RootServlet extends HttpServlet {

  private final Logger logger;
  private final String baseFile;
  private final String path;

  public RootServlet(Logger logger, String baseFile, String path) {
    this.logger = logger;
    this.baseFile = baseFile;
    this.path = path;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    logger.info(req.getHeader("User-Agent") + "@" + req.getRemoteAddr() + ":" + req.getPathInfo());
    var file = new File(baseFile, path);
    var bytes = readFile(file);
    res.setContentType("text/html");
    res.setContentLength(bytes.length);
    res.setHeader("Cache-Control", "no-store");
    res.getOutputStream().write(bytes);
  }

  static byte[] readFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }
}
