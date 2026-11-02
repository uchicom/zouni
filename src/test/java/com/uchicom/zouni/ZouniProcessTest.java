// (C) 2025 uchicom
package com.uchicom.zouni;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.uchicom.http.DefaultRouter;
import com.uchicom.http.StaticHandler;
import com.uchicom.util.Parameter;
import jakarta.servlet.Servlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** {@link ZouniProcess}のテストケース. レスポンスがバッファされず、正しく送信されることを確認する. */
public class ZouniProcessTest extends AbstractTest {

  @TempDir Path pubDir;

  Path htmlDir;

  @BeforeEach
  void createHtmlDir() throws Exception {
    htmlDir = Files.createDirectory(pubDir.resolve("html"));
  }

  /** リクエストを処理し、ソケットへ書き込まれた生のレスポンスを返す. */
  String request(String requestLine, String... headers) throws Exception {
    var sb = new StringBuilder(requestLine).append("\r\n");
    for (var header : headers) {
      sb.append(header).append("\r\n");
    }
    sb.append("\r\n");

    var router = new DefaultRouter();
    router.init(pubDir.toFile(), 0);
    var staticHandler = new StaticHandler(router);
    var parameter = mock(Parameter.class);
    doReturn(pubDir.toFile()).when(parameter).getFile("public");
    doReturn("localhost").when(parameter).get("host");
    var socket = mock(Socket.class);
    var out = new ByteArrayOutputStream();
    doReturn(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.US_ASCII)))
        .when(socket)
        .getInputStream();
    doReturn(out).when(socket).getOutputStream();

    new ZouniProcess(
            parameter,
            socket,
            new HashMap<String, Servlet>(),
            new HashMap<String, Servlet>(),
            Logger.getLogger("test"),
            null,
            staticHandler)
        .execute();
    return out.toString(StandardCharsets.ISO_8859_1);
  }

  @Test
  public void staticFileHasContentLengthAndLastModified() throws Exception {
    var file = htmlDir.resolve("a.txt");
    Files.writeString(file, "hello");
    Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(0));

    var res = request("GET /a.txt HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 200 OK");
    assertThat(res).contains("Content-Length: 5\r\n");
    assertThat(res).contains("Last-Modified: Thu, 1 Jan 1970 00:00:00 GMT\r\n");
    assertThat(res).endsWith("\r\n\r\nhello");
  }

  @Test
  public void largeStaticFileIsSentCompletely() throws Exception {
    var data = new byte[1024 * 1024 + 123];
    new java.util.Random(1).nextBytes(data);
    Files.write(htmlDir.resolve("big.bin"), data);

    var res = request("GET /big.bin HTTP/1.1");

    var headerEnd = res.indexOf("\r\n\r\n") + 4;
    assertThat(res).contains("Content-Length: " + data.length + "\r\n");
    assertThat(res.substring(headerEnd).getBytes(StandardCharsets.ISO_8859_1)).isEqualTo(data);
  }

  @Test
  public void staticFileIsNotGzippedBecauseLengthIsDeclared() throws Exception {
    Files.writeString(htmlDir.resolve("a.txt"), "hello");

    var res = request("GET /a.txt HTTP/1.1", "Accept-Encoding: gzip");

    assertThat(res).doesNotContain("Content-Encoding");
    assertThat(res).endsWith("\r\n\r\nhello");
  }

  @Test
  public void directoryRequestServesIndexHtm() throws Exception {
    Files.writeString(htmlDir.resolve("index.htm"), "top");

    var res = request("GET / HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 200 OK");
    assertThat(res).endsWith("\r\n\r\ntop");
  }

  @Test
  public void percentEncodedPathIsDecoded() throws Exception {
    Files.writeString(htmlDir.resolve("日本 語.txt"), "jp");
    Files.writeString(htmlDir.resolve("a+b.txt"), "plus");

    assertThat(request("GET /%E6%97%A5%E6%9C%AC%20%E8%AA%9E.txt HTTP/1.1")).endsWith("\r\n\r\njp");
    assertThat(request("GET /a+b.txt HTTP/1.1")).endsWith("\r\n\r\nplus");
  }

  @Test
  public void invalidPercentEncodingIsBadRequest() throws Exception {
    var res = request("GET /%ZZ.txt HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 400");
  }

  @Test
  public void notModifiedWhenIfModifiedSinceIsNewer() throws Exception {
    var file = htmlDir.resolve("a.txt");
    Files.writeString(file, "hello");
    Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(0));

    // ヘッダー名は小文字でも解釈される
    var res = request("GET /a.txt HTTP/1.1", "if-modified-since: Fri, 2 Jan 2026 00:00:00 GMT");

    assertThat(res).startsWith("HTTP/1.1 304");
    assertThat(res).doesNotContain("hello");
  }

  @Test
  public void dotFileIsNotServed() throws Exception {
    Files.writeString(htmlDir.resolve(".secret"), "secret");

    var res = request("GET /.secret HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 404");
    assertThat(res).doesNotContain("secret");
  }

  @Test
  public void notFound() throws Exception {
    var res = request("GET /none.txt HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 404");
  }

  @Test
  public void methodNotAllowed() throws Exception {
    var res = request("DELETE /a.txt HTTP/1.1");

    assertThat(res).startsWith("HTTP/1.1 405");
  }

  @Test
  public void pathTraversalIsRejected() throws Exception {
    var secret = new File(pubDir.toFile().getParentFile(), "secret.txt");
    Files.writeString(secret.toPath(), "secret");
    try {
      var res = request("GET /../secret.txt HTTP/1.1");

      assertThat(res).doesNotContain("secret");
    } finally {
      secret.delete();
    }
  }
}
