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
}
