// (C) 2025 uchicom
package com.uchicom.zouni;

import com.uchicom.http.DefaultRouter;
import com.uchicom.http.Router;
import com.uchicom.http.StaticHandler;
import com.uchicom.server.ServerProcess;
import com.uchicom.util.Parameter;
import com.uchicom.zouni.dto.IpErrorMessageKey;
import com.uchicom.zouni.exception.HttpException;
import com.uchicom.zouni.servlet.ZouniServletRequest;
import com.uchicom.zouni.servlet.ZouniServletResponse;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ZouniProcess implements ServerProcess {
  private String host;
  private Socket socket;
  private StaticHandler staticHandler;
  public Map<String, Servlet> startWithMap;
  private Map<String, Servlet> map;
  private ConcurrentMap<IpErrorMessageKey, AtomicInteger> ipErrorMessageCountMap;
  private Logger logger;
  private boolean session;

  /** エラー応答の生成にのみ使用する(公開ディレクトリは持たない) */
  private final Router router = new DefaultRouter();

  /** 最終アクセス時刻(ミリ秒). 読み書きのたびに更新する */
  private volatile long lastTime = System.currentTimeMillis();

  public ZouniProcess(
      Parameter parameter,
      Socket socket,
      Map<String, Servlet> map,
      Map<String, Servlet> startWithMap,
      Logger logger,
      ConcurrentMap<IpErrorMessageKey, AtomicInteger> ipErrorMessageCountMap,
      StaticHandler staticHandler) {
    this.socket = socket;
    this.map = map;
    this.startWithMap = startWithMap;
    this.logger = logger;
    this.ipErrorMessageCountMap = ipErrorMessageCountMap;
    this.staticHandler = staticHandler;
    this.host = parameter.get("host");
    this.session = parameter.is("session");
  }

  @Override
  public void execute() {
    try (BufferedInputStream bis =
        new BufferedInputStream(ActivityStreams.wrap(socket.getInputStream(), this::touch))) {
      var req = createServletRequest(socket, bis);
      if (!"GET".equals(req.getMethod()) && !"POST".equals(req.getMethod())) {
        error(405);
        return;
      }
      var servlet = getServlet(req);
      if (servlet == null) {
        // サーブレットに割り当てがないリクエストは静的ファイルとして配信する(応答はhttp側が直接出力する)
        handleStatic(req);
        return;
      }
      // 本文はバッファせず、ヘッダは本文の出力開始時(または完了時)にソケットへ直接出力する
      var res =
          new ZouniServletResponse(
              new BufferedOutputStream(socketOutputStream()),
              req.isGzip(),
              (r, os, contentLength, gzip) -> writeHeaders(req, r, os, contentLength, gzip));
      try {
        servlet.service(req, res);
      } catch (ServletException e) {
        if (!res.isCommitted()) {
          error(500);
        }
        throw e;
      }
      if (!res.isCommitted() && res.getStatus() != 200) {
        error(res.getStatus());
        return;
      }
      res.finish();

    } catch (Throwable e) {
      filterLog(socket.getInetAddress(), e);
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

  void filterLog(InetAddress inetAddress, Throwable e) {
    var message = e.getMessage();
    if (ipErrorMessageCountMap == null) {
      warningLog(inetAddress, message);
      return;
    }
    ipErrorMessageCountMap.compute(
        new IpErrorMessageKey(inetAddress.toString(), message),
        (key, old) -> {
          if (old == null) {
            warningLog(inetAddress, message);
            return new AtomicInteger(1);
          }
          old.getAndIncrement();
          return old;
        });
  }

  void warningLog(InetAddress inetAddress, String message) {
    logger.warning("Error ip:" + inetAddress + ", " + message);
  }

  /**
   * レスポンスヘッダを出力する.
   *
   * @param contentLength 本文の長さ. 負の場合はchunkedで送信する
   * @param gzip 本文がgzip圧縮されている場合true
   */
  void writeHeaders(
      ZouniServletRequest req,
      ZouniServletResponse res,
      OutputStream os,
      long contentLength,
      boolean gzip)
      throws IOException {
    if (res.getStatus() == 200) {
      os.write(Constants.RES_200);
    } else {
      os.write(("HTTP/1.1 " + res.getStatus()).getBytes(StandardCharsets.US_ASCII));
      os.write(Constants.RES_LINE_END);
    }
    // Content-TYpe
    writeContentType(os, res);
    os.write(Constants.RES_EXPIRES);
    if (gzip) {
      os.write(Constants.RES_CONTENT_ENCODING_GZIP);
    }
    if (contentLength >= 0) {
      os.write(Constants.RES_CONTENT_LENGTH);
      os.write(String.valueOf(contentLength).getBytes(StandardCharsets.US_ASCII));
      os.write(Constants.RES_LINE_END);
    } else {
      os.write(Constants.RES_TRANSFER_ENCODING_CHUNKED);
    }
    os.write(Constants.RES_SERVER);

    // SESSION
    writeSession(os, req);

    // クッキー
    writeCookie(os, res);

    // ヘッダー
    writeHeader(os, res);

    os.write(Constants.RES_LINE_END);
  }

  void writeContentType(OutputStream os, ZouniServletResponse res) throws IOException {
    var contentType = res.getContentType();
    if (contentType != null) {
      os.write(Constants.RES_CONTENT_TYPE);
      os.write(contentType.getBytes(StandardCharsets.US_ASCII));
      os.write(Constants.RES_LINE_END);
    } else {
      os.write(Constants.RES_CONTENT_TYPE_TEXT_HtML);
    }
  }

  void writeSession(OutputStream os, ZouniServletRequest req) throws IOException {
    if (!session) {
      return;
    }
    os.write(Constants.SET_COOKIE);
    os.write(Constants.JSESSIONID);
    if (req.getSession() != null && req.getSession().getId() != null) {
      os.write(req.getSession().getId().getBytes(StandardCharsets.US_ASCII));
      os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
      os.write(
          Constants.formatter
              .format(OffsetDateTime.now(ZoneOffset.systemDefault()).plusDays(1))
              .getBytes(StandardCharsets.US_ASCII));
    } else {
      os.write("; Expires=".getBytes(StandardCharsets.US_ASCII));
      os.write(
          Constants.formatter
              .format(OffsetDateTime.now(ZoneOffset.systemDefault()).minusDays(1))
              .getBytes(StandardCharsets.US_ASCII));
    }
    os.write("; Domain=".getBytes(StandardCharsets.US_ASCII));
    os.write(this.host.getBytes(StandardCharsets.US_ASCII));
    os.write(Constants.RES_LINE_END);
  }

  void writeCookie(OutputStream os, ZouniServletResponse res) throws IOException {
    var cookieList = res.getCookieList();
    if (cookieList.isEmpty()) {
      return;
    }
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

  void writeHeader(OutputStream os, ZouniServletResponse res) throws IOException {
    for (var headerName : res.getHeaderNames()) {
      for (var headerValue : res.getHeaders(headerName)) {
        os.write(headerName.getBytes(StandardCharsets.US_ASCII));
        os.write(": ".getBytes(StandardCharsets.US_ASCII));
        os.write(headerValue.getBytes(StandardCharsets.US_ASCII));
        os.write(Constants.RES_LINE_END);
      }
    }
  }

  void handleStatic(ZouniServletRequest req) throws IOException {
    String path;
    try {
      path = decodePath(req.getRequestURI());
    } catch (IllegalArgumentException e) {
      error(400);
      return;
    }
    staticHandler.handle(path, null, req.getHeaderMap(), socketOutputStream());
  }

  /** パスをパーセントデコードする. クエリ文字列と異なり、+は空白にしない. */
  static String decodePath(String path) {
    return URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
  }

  Servlet getServlet(ZouniServletRequest req) {
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
    return servlet;
  }

  ZouniServletRequest createServletRequest(Socket socket, InputStream is)
      throws IOException, HttpException {
    try {
      return new ZouniServletRequest(socket, is);
    } catch (HttpException e) {
      error(e.statusCode);
      throw e;
    }
  }

  @Override
  public long getLastTime() {
    return lastTime;
  }

  private void touch() {
    lastTime = System.currentTimeMillis();
  }

  /** 読み書きのたびに最終アクセス時刻を更新するソケットの出力ストリームを返す. */
  private OutputStream socketOutputStream() throws IOException {
    return ActivityStreams.wrap(socket.getOutputStream(), this::touch);
  }

  @Override
  public void forceClose() {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        logger.warning("Socket close error: " + e.getMessage());
      }
    }
  }

  /** エラー応答を返す. 応答の組み立てと送信後のクローズはhttpライブラリに任せる. */
  void error(int statuscode) throws IOException {
    router.error(String.valueOf(statuscode), socketOutputStream());
  }
}
