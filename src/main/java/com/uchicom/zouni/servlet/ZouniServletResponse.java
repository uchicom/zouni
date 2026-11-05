// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ZouniServletResponse implements HttpServletResponse {

  /** この大きさを超えるまでは本文をメモリに保持し、Content-Lengthを確定して送信する. */
  static final int BUFFER_SIZE = 8 * 1024;

  private static final DateTimeFormatter HTTP_DATE =
      DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

  private final OutputStream os;
  private final boolean gzip;
  private final HeaderWriter headerWriter;
  private final Body body = new Body();
  private ServletOutputStream servletOutputStream;
  private PrintWriter writer;
  private long contentLength = -1;
  private boolean committed;
  private String type;
  private List<Cookie> cookieList = new ArrayList<>();
  private Map<String, List<String>> headerListMap = new HashMap<String, List<String>>();
  private int statuscode = 200;

  /**
   * @param os 送信先. ヘッダと本文はここへ直接書き込まれる
   * @param gzip 本文をgzip圧縮してよい場合true. Content-Lengthが宣言された場合は圧縮しない
   * @param headerWriter コミット時にヘッダを出力する
   */
  public ZouniServletResponse(OutputStream os, boolean gzip, HeaderWriter headerWriter) {
    this.os = os;
    this.gzip = gzip;
    this.headerWriter = headerWriter;
  }

  /** 本文の出力を完了する. 未コミットの場合はここでContent-Length付きでコミットする. */
  public void finish() throws IOException {
    if (writer != null) {
      writer.flush();
    }
    body.finish();
  }

  public List<Cookie> getCookieList() {
    return cookieList;
  }

  @Override
  public String getCharacterEncoding() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (servletOutputStream == null) {
      servletOutputStream = new ZouniServletOutputStream(body);
    }
    return servletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (writer == null) {
      writer = new PrintWriter(new OutputStreamWriter(body, StandardCharsets.UTF_8));
    }
    return writer;
  }

  @Override
  public void setContentLength(int length) {
    contentLength = length;
  }

  @Override
  public void setContentLengthLong(long len) {
    contentLength = len;
  }

  @Override
  public void setContentType(String type) {
    this.type = type;
  }

  @Override
  public String getContentType() {
    return type;
  }

  @Override
  public void setBufferSize(int size) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getBufferSize() {
    return BUFFER_SIZE;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  /** 本文の長さが確定していなくてもヘッダを出力し、以降の本文を順次送信する. */
  @Override
  public void flushBuffer() throws IOException {
    if (writer != null) {
      writer.flush();
    }
    body.commitStreaming();
    body.sinkFlush();
  }

  @Override
  public void setLocale(Locale locale) {
    // TODO Auto-generated method stub

  }

  @Override
  public Locale getLocale() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addCookie(Cookie cookie) {
    cookieList.add(cookie);
  }

  @Override
  public String encodeURL(String url) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String encodeRedirectURL(String url) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendError(int status) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendError(int status, String message) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendRedirect(String location) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setDateHeader(String headername, long date) {
    setHeader(headername, HTTP_DATE.format(Instant.ofEpochMilli(date)));
  }

  @Override
  public void setHeader(String headername, String value) {
    var headerList = new ArrayList<String>();
    headerList.add(value);
    headerListMap.put(headername, headerList);
  }

  @Override
  public void addHeader(String headername, String value) {
    var headerList = headerListMap.get(headername);
    if (headerList == null) {
      headerList = new ArrayList<String>();
      headerListMap.put(headername, headerList);
    }
    headerList.add(value);
  }

  @Override
  public void addDateHeader(String headername, long date) {
    addHeader(headername, HTTP_DATE.format(Instant.ofEpochMilli(date)));
  }

  @Override
  public void addIntHeader(String headername, int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setIntHeader(String headername, int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setStatus(int statuscode) {
    this.statuscode = statuscode;
  }

  @Override
  public boolean isCommitted() {
    return committed;
  }

  @Override
  public void resetBuffer() {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  public void setCharacterEncoding(String arg0) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  public boolean containsHeader(String arg0) {
    return headerListMap.containsKey(arg0);
  }

  @Override
  public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sendRedirect'");
  }

  @Override
  public int getStatus() {
    return statuscode;
  }

  @Override
  public String getHeader(String name) {
    var headerList = headerListMap.get(name);
    if (headerList == null) {
      return null;
    }
    return headerList.get(0);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return headerListMap.get(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return headerListMap.keySet();
  }

  /** 本文の出力先. 閾値までは保持し、超えた時点でヘッダを出力して以降は直接送信する. */
  private class Body extends OutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private OutputStream sink;
    private GZIPOutputStream gzipOs;
    private BufferedOutputStream chunkBuffer;
    private ChunkedOutputStream chunked;

    @Override
    public void write(int b) throws IOException {
      if (sink == null && buffer.size() < BUFFER_SIZE) {
        buffer.write(b);
        return;
      }
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      if (sink == null) {
        if (buffer.size() + len <= BUFFER_SIZE) {
          buffer.write(b, off, len);
          return;
        }
        commitStreaming();
      }
      sink.write(b, off, len);
    }

    /** 明示的なflushでは確定させない. 確定させるのは{@link ZouniServletResponse#flushBuffer()}. */
    @Override
    public void flush() {}

    @Override
    public void close() {}

    void sinkFlush() throws IOException {
      if (sink != null) {
        sink.flush();
      }
    }

    /** 本文の長さが確定していない状態でヘッダを出力する. Content-Lengthが宣言済みなら直接送信する. */
    void commitStreaming() throws IOException {
      if (committed) {
        return;
      }
      committed = true;
      var useGzip = gzip && contentLength < 0;
      if (contentLength >= 0) {
        headerWriter.write(ZouniServletResponse.this, os, contentLength, false);
        sink = os;
      } else {
        headerWriter.write(ZouniServletResponse.this, os, -1, useGzip);
        chunked = new ChunkedOutputStream(os);
        chunkBuffer = new BufferedOutputStream(chunked, BUFFER_SIZE);
        sink = chunkBuffer;
        if (useGzip) {
          gzipOs = new GZIPOutputStream(chunkBuffer);
          sink = gzipOs;
        }
      }
      buffer.writeTo(sink);
      buffer.reset();
    }

    void finish() throws IOException {
      if (!committed) {
        commitComplete();
      } else if (chunked != null) {
        if (gzipOs != null) {
          gzipOs.finish();
        }
        chunkBuffer.flush();
        chunked.finish();
      }
      os.flush();
    }

    /** 全本文がメモリ上にある状態でContent-Length付きでヘッダを出力する. */
    private void commitComplete() throws IOException {
      committed = true;
      var bytes = buffer.toByteArray();
      var useGzip = gzip && contentLength < 0 && bytes.length > 0;
      if (useGzip) {
        var baos = new ByteArrayOutputStream();
        try (var gz = new GZIPOutputStream(baos)) {
          gz.write(bytes);
        }
        bytes = baos.toByteArray();
      }
      headerWriter.write(ZouniServletResponse.this, os, bytes.length, useGzip);
      os.write(bytes);
    }
  }
}
