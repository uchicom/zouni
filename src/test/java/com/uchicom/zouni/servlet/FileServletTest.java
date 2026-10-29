// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.uchicom.zouni.AbstractTest;
import com.uchicom.zouni.Constants;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link FileServlet}のテストケース.
 *
 * @author uchicom
 */
public class FileServletTest extends AbstractTest {

  @TempDir File tempDir;

  @Test
  public void defaultMime() {
    assertThat(Constants.DEFAULT_MIME).isEqualTo("application/octet-stream");
  }

  @Test
  public void doGet_unknownExtension() throws Exception {
    // mock
    var file = new File(tempDir, "test.unknownext");
    Files.writeString(file.toPath(), "abc");
    var req = mock(HttpServletRequest.class);
    var res = mock(HttpServletResponse.class);
    var sos = mock(ServletOutputStream.class);
    doReturn(sos).when(res).getOutputStream();

    // test
    new FileServlet(file).doGet(req, res);

    // assert
    verify(res).setContentType("application/octet-stream");
  }

  @Test
  public void doGet_noExtension() throws Exception {
    var file = new File(tempDir, "README");
    Files.writeString(file.toPath(), "abc");
    var res = mock(HttpServletResponse.class);
    doReturn(mock(ServletOutputStream.class)).when(res).getOutputStream();

    new FileServlet(file).doGet(mock(HttpServletRequest.class), res);

    verify(res).setContentType("application/octet-stream");
  }

  @Test
  public void doGet_upperCaseExtension() throws Exception {
    var file = new File(tempDir, "FOO.PNG");
    Files.writeString(file.toPath(), "abc");
    var res = mock(HttpServletResponse.class);
    doReturn(mock(ServletOutputStream.class)).when(res).getOutputStream();

    new FileServlet(file).doGet(mock(HttpServletRequest.class), res);

    verify(res).setContentType(Constants.mimeProperties.getProperty("png"));
  }

  @Test
  public void doGet_notFound() throws Exception {
    var res = mock(HttpServletResponse.class);

    new FileServlet(new File(tempDir, "none.txt")).doGet(mock(HttpServletRequest.class), res);

    verify(res).sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void doGet_directory() throws Exception {
    var res = mock(HttpServletResponse.class);

    new FileServlet(tempDir).doGet(mock(HttpServletRequest.class), res);

    verify(res).sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}
