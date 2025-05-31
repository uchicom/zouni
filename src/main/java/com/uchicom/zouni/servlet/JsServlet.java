// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class JsServlet extends HttpServlet {
  private final Supplier<String> supplier;

  public JsServlet(Supplier<String> supplier) {
    this.supplier = supplier;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    var body = supplier.get();
    var bytes = body.getBytes(StandardCharsets.UTF_8);
    res.setContentType("application/javascript; charset=UTF-8");
    res.setContentLength(bytes.length);
    res.setHeader("Cache-Control", "no-store");
    res.getOutputStream().write(bytes);
  }
}
