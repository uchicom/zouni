// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

/** {@link ZouniServletResponse}のテストケース. */
public class ZouniServletResponseTest {

  /** ヘッダ出力時の引数と、その時点で下位ストリームに書かれていたバイト数を記録する. */
  static class Recorder implements HeaderWriter {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final List<Long> lengths = new ArrayList<>();
    final List<Boolean> gzips = new ArrayList<>();
    int sizeAtHeader = -1;

    @Override
    public void write(ZouniServletResponse res, OutputStream os, long contentLength, boolean gzip)
        throws IOException {
      lengths.add(contentLength);
      gzips.add(gzip);
      sizeAtHeader = out.size();
      os.write(("HEADER\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
    }

    String body() {
      var s = out.toString(StandardCharsets.ISO_8859_1);
      return s.substring(s.indexOf("\r\n\r\n") + 4);
    }
  }

  @Test
  public void smallBodyIsSentWithContentLength() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, false, rec);

    res.getOutputStream().write("hello".getBytes(StandardCharsets.UTF_8));
    assertThat(res.isCommitted()).isFalse();
    res.finish();

    assertThat(res.isCommitted()).isTrue();
    assertThat(rec.lengths).containsExactly(5L);
    assertThat(rec.body()).isEqualTo("hello");
  }

  @Test
  public void writerIsFlushedOnFinish() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, false, rec);

    res.getWriter().print("あいう");
    res.finish();

    assertThat(rec.lengths).containsExactly(9L);
    assertThat(rec.out.toString(StandardCharsets.UTF_8)).endsWith("あいう");
  }

  @Test
  public void largeBodyWithoutLengthIsChunked() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, false, rec);
    var data = new byte[ZouniServletResponse.BUFFER_SIZE * 3 + 10];
    java.util.Arrays.fill(data, (byte) 'a');

    res.getOutputStream().write(data);
    // 閾値を超えた時点でヘッダが出力され、本文はバッファされず送信されている
    assertThat(res.isCommitted()).isTrue();
    assertThat(rec.lengths).containsExactly(-1L);
    res.finish();

    assertThat(decodeChunked(rec.body())).isEqualTo(new String(data, StandardCharsets.US_ASCII));
  }

  @Test
  public void declaredContentLengthIsStreamedWithoutBuffering() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, true, rec);
    var data = new byte[ZouniServletResponse.BUFFER_SIZE * 2];
    java.util.Arrays.fill(data, (byte) 'b');

    res.setContentLengthLong(data.length);
    res.getOutputStream().write(data);

    assertThat(res.isCommitted()).isTrue();
    // 長さ宣言済みの場合は圧縮せず、そのまま送る
    assertThat(rec.lengths).containsExactly((long) data.length);
    assertThat(rec.gzips).containsExactly(false);
    res.finish();

    assertThat(rec.body()).isEqualTo(new String(data, StandardCharsets.US_ASCII));
  }

  @Test
  public void gzipSmallBody() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, true, rec);

    res.getOutputStream().write("hello hello hello".getBytes(StandardCharsets.UTF_8));
    res.finish();

    assertThat(rec.gzips).containsExactly(true);
    var bytes = rec.out.toByteArray();
    var bodyStart = new String(bytes, StandardCharsets.ISO_8859_1).indexOf("\r\n\r\n") + 4;
    assertThat(rec.lengths).containsExactly((long) (bytes.length - bodyStart));
    assertThat(gunzip(java.util.Arrays.copyOfRange(bytes, bodyStart, bytes.length)))
        .isEqualTo("hello hello hello");
  }

  @Test
  public void gzipLargeBodyIsChunked() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, true, rec);
    var data = new byte[ZouniServletResponse.BUFFER_SIZE * 2 + 1];
    java.util.Arrays.fill(data, (byte) 'c');

    res.getOutputStream().write(data);
    res.finish();

    assertThat(rec.lengths).containsExactly(-1L);
    assertThat(rec.gzips).containsExactly(true);
    var chunked = decodeChunkedBytes(rec.body().getBytes(StandardCharsets.ISO_8859_1));
    assertThat(gunzip(chunked)).isEqualTo(new String(data, StandardCharsets.US_ASCII));
  }

  @Test
  public void flushBufferCommitsHeaderBeforeBodyIsComplete() throws Exception {
    var rec = new Recorder();
    var res = new ZouniServletResponse(rec.out, false, rec);

    res.getOutputStream().write("part1".getBytes(StandardCharsets.UTF_8));
    res.flushBuffer();

    assertThat(res.isCommitted()).isTrue();
    assertThat(rec.lengths).containsExactly(-1L);
    assertThat(rec.out.toString(StandardCharsets.UTF_8)).contains("part1");

    res.getOutputStream().write("part2".getBytes(StandardCharsets.UTF_8));
    res.finish();
    assertThat(decodeChunked(rec.body())).isEqualTo("part1part2");
  }

  @Test
  public void setDateHeaderFormatsAsHttpDate() {
    var res = new ZouniServletResponse(new ByteArrayOutputStream(), false, (r, o, l, g) -> {});

    res.setDateHeader("Last-Modified", 0L);

    assertThat(res.getHeader("Last-Modified")).isEqualTo("Thu, 1 Jan 1970 00:00:00 GMT");
    assertThat(res.containsHeader("Last-Modified")).isTrue();
  }

  static String decodeChunked(String body) throws IOException {
    return new String(
        decodeChunkedBytes(body.getBytes(StandardCharsets.ISO_8859_1)), StandardCharsets.US_ASCII);
  }

  static byte[] decodeChunkedBytes(byte[] raw) throws IOException {
    var in = new ByteArrayInputStream(raw);
    var out = new ByteArrayOutputStream();
    while (true) {
      var line = new StringBuilder();
      int c;
      while ((c = in.read()) != '\r') {
        line.append((char) c);
      }
      in.read(); // \n
      var size = Integer.parseInt(line.toString(), 16);
      if (size == 0) {
        return out.toByteArray();
      }
      out.write(in.readNBytes(size));
      in.read();
      in.read(); // \r\n
    }
  }

  static String gunzip(byte[] bytes) throws IOException {
    try (var gz = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
