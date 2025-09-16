// (C) 2025 uchicom
package com.uchicom.zouni;

import com.uchicom.server.ServerProcess;
import com.uchicom.util.Parameter;
import com.uchicom.zouni.factory.di.DIFactory;
import com.uchicom.zouni.servlet.FileServlet;
import com.uchicom.zouni.servlet.ZouniServletRequest;
import com.uchicom.zouni.servlet.ZouniServletResponse;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class ZouniProcess implements ServerProcess {
  private static DateTimeFormatter formatter =
      DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);
  private String host;
  private Socket socket;
  private File pubDir;
  public Map<String, Servlet> startWithMap;
  private Map<String, Servlet> map;
  private Logger logger = DIFactory.logger();

  public ZouniProcess(
      Parameter parameter,
      Socket socket,
      Map<String, Servlet> map,
      Map<String, Servlet> startWithMap) {
    this.socket = socket;
    this.map = map;
    this.startWithMap = startWithMap;
    this.pubDir = parameter.getFile("public");
    this.host = parameter.get("host");
  }

  @Override
  public void execute() {
    try {
      ZouniServletRequest req = new ZouniServletRequest(socket);
      if ("GET".equals(req.getMethod()) || "POST".equals(req.getMethod())) {
        String key = "pub." + req.getRequestURI();
        var servlet = map.get(key);
        if (servlet == null) {
          servlet =
              startWithMap.entrySet().stream()
                  .filter(entry -> key.startsWith(entry.getKey()))
                  .map(Entry::getValue)
                  .findFirst()
                  .orElse(null);
        }
        if (servlet == null) {
          // リクエスト対象のファイルを探す
          File file = null;
          if (req.getRequestURI().endsWith("/")) {
            File dir = null;
            if ("/".equals(req.getRequestURI())) {
              dir = pubDir;
            } else {
              dir = new File(pubDir, req.getRequestURI());
            }
            file = new File(dir, "index.htm");
            if (!file.exists()) {
              file = new File(dir, "index.html");
            }
          } else {
            file = new File(pubDir, req.getRequestURI().substring(1));
          }

          if (file.exists()) {
            // ファイルが存在する場合
            if (file.getCanonicalPath().startsWith(pubDir.getCanonicalPath())) {
              // ファイル出力サーブレット
              servlet = new FileServlet(file);
            }
          }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZouniServletResponse res = null;
        GZIPOutputStream gzos = null;
        if (req.isGzip()) {
          gzos = new GZIPOutputStream(baos);
          res = new ZouniServletResponse(gzos);
        } else {
          res = new ZouniServletResponse(baos);
        }
        if (servlet != null) {
          // レスポンスヘッダ出力
          try {
            servlet.service(req, res);
          } catch (ServletException e) {
            try (OutputStream os = socket.getOutputStream(); ) {
              error500(os);
            }
            throw e;
          }
          // chunkの実装がない
          try (OutputStream os = socket.getOutputStream(); ) {
            os.write(Constants.RES_200);
            String contentType = res.getContentType();
            if (contentType != null) {
              os.write("Content-Type: ".getBytes(StandardCharsets.US_ASCII));
              os.write(contentType.getBytes(StandardCharsets.US_ASCII));
              os.write(Constants.RES_LINE_END);
            } else {
              os.write(Constants.RES_CONTENT_TYPE);
            }
            os.write(Constants.RES_EXPIRES);
            if (req.isGzip()) {
              os.write(Constants.RES_CONTENT_ENCODING_GZIP);
              gzos.finish();
            }
            os.write(Constants.RES_CONTENT_LENGTH);
            os.write(String.valueOf(baos.size()).getBytes(StandardCharsets.US_ASCII));
            os.write(Constants.RES_LINE_END);
            os.write(Constants.RES_LAST_MODIFIED);
            os.write(
                formatter
                    .format(OffsetDateTime.now(ZoneOffset.systemDefault()))
                    .getBytes(StandardCharsets.US_ASCII));
            os.write(Constants.RES_LINE_END);
            os.write(Constants.RES_SERVER);
            if (req.getSession() != null && req.getSession().getId() != null) {
              os.write(Constants.SET_COOKIE);
              os.write(Constants.JSESSIONID);
              os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
              os.write(
                  Constants.formatter
                      .format(OffsetDateTime.now(ZoneOffset.systemDefault()).minusDays(1))
                      .getBytes(StandardCharsets.US_ASCII));
              os.write(Constants.RES_LINE_END);

              os.write(Constants.SET_COOKIE);
              os.write(Constants.JSESSIONID);
              os.write(req.getSession().getId().getBytes(StandardCharsets.US_ASCII));
              os.write("; Domain=".getBytes(StandardCharsets.US_ASCII));
              os.write(this.host.getBytes(StandardCharsets.US_ASCII));
              os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
              os.write(
                  Constants.formatter
                      .format(OffsetDateTime.now(ZoneOffset.systemDefault()).plusDays(1))
                      .getBytes(StandardCharsets.US_ASCII));
              os.write(Constants.RES_LINE_END);
            } else {
              os.write(Constants.SET_COOKIE);
              os.write(Constants.JSESSIONID);
              os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
              os.write(
                  Constants.formatter
                      .format(OffsetDateTime.now(ZoneOffset.systemDefault()).minusDays(1))
                      .getBytes(StandardCharsets.US_ASCII));
              os.write(Constants.RES_LINE_END);
              os.write(Constants.SET_COOKIE);
              os.write(Constants.JSESSIONID);
              os.write("; Domain=".getBytes(StandardCharsets.US_ASCII));
              os.write(this.host.getBytes(StandardCharsets.US_ASCII));
              os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
              os.write(
                  Constants.formatter
                      .format(OffsetDateTime.now(ZoneOffset.systemDefault()).minusDays(1))
                      .getBytes(StandardCharsets.US_ASCII));
              os.write(Constants.RES_LINE_END);
            }
            var cookieList = res.getCookieList();
            if (!cookieList.isEmpty()) {
              for (var cookie : cookieList) {
                os.write(Constants.SET_COOKIE);
                os.write(cookie.getName().getBytes(StandardCharsets.UTF_8));
                os.write("=".getBytes(StandardCharsets.US_ASCII));
                if (cookie.getValue() != null) {
                  os.write(cookie.getValue().getBytes(StandardCharsets.UTF_8));
                }
                if (cookie.getDomain() != null) {
                  os.write("; ".getBytes(StandardCharsets.US_ASCII));
                  os.write(cookie.getDomain().getBytes(StandardCharsets.UTF_8));
                }
                if (cookie.isHttpOnly()) {
                  os.write("; HttpOnly".getBytes(StandardCharsets.US_ASCII));
                }
                if (cookie.getSecure()) {
                  os.write("; Secure".getBytes(StandardCharsets.US_ASCII));
                }
                if (cookie.getPath() != null) {
                  os.write("; Path=".getBytes(StandardCharsets.US_ASCII));
                  os.write(String.valueOf(cookie.getPath()).getBytes(StandardCharsets.US_ASCII));
                }
                if (cookie.getMaxAge() > 0) {
                  os.write("; Max-Age=".getBytes(StandardCharsets.US_ASCII));
                  os.write(String.valueOf(cookie.getMaxAge()).getBytes(StandardCharsets.US_ASCII));
                }
                os.write(Constants.RES_LINE_END);
              }
            }
            os.write(Constants.RES_LINE_END);
            os.write(baos.toByteArray());
            os.flush();
          }

        } else {
          try (OutputStream os = socket.getOutputStream(); ) {
            error404(os);
          }
        }
      }
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "Error processing request", e);
    } finally {
      if (socket != null && socket.isConnected()) {
        try {
          socket.close();
        } catch (IOException e) {
          logger.warning("Socket close error: " + e.getMessage());
        }
      }
    }
  }

  @Override
  public long getLastTime() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  @Override
  public void forceClose() {
    // TODO 自動生成されたメソッド・スタブ

  }

  public static void error404(OutputStream os) throws IOException {
    os.write(Constants.RES_404);
    os.write(Constants.RES_CONTENT_TYPE);
    os.write(Constants.RES_CONTENT_LENGTH);
    os.write(Constants.RES_440_HTML_LENGTH);
    os.write(Constants.RES_LINE_END);
    os.write(Constants.RES_SERVER);
    os.write(Constants.RES_LINE_END);
    os.write(Constants.RES_404_HTML);
    os.flush();
  }

  public static void error500(OutputStream os) throws IOException {
    os.write(Constants.RES_500);
    os.write(Constants.RES_CONTENT_TYPE);
    os.write(Constants.RES_CONTENT_LENGTH);
    os.write(Constants.RES_500_HTML_LENGTH);
    os.write(Constants.RES_LINE_END);
    os.write(Constants.RES_SERVER);
    os.write(Constants.RES_LINE_END);
    os.write(Constants.RES_500_HTML);
    os.flush();
  }
}
