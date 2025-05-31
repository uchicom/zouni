// (C) 2025 uchicom
package com.uchicom.zouni;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.uchicom.server.Server;
import com.uchicom.server.ServerProcessFactory;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

/**
 * {@link Main}のテストケース.
 *
 * @author uchicom
 */
public class MainTest extends AbstractTest {

  @Captor ArgumentCaptor<PrintStream> printStreamCaptor;
  @Captor ArgumentCaptor<ServerProcessFactory> serverProcessArgumentCaptor;

  @Test
  public void main() throws Exception {
    // mock
    var args = new String[] {"test"};
    var server = mock(Server.class);
    try (var mocked =
        mockConstruction(
            ZouniParameter.class,
            (mock, context) -> {
              doReturn(true).when(mock).init(printStreamCaptor.capture());
              doReturn(server).when(mock).createServer(serverProcessArgumentCaptor.capture());
              doNothing().when(server).execute();
              assertThat(context.arguments().get(0)).isEqualTo(args);
            })) {
      Main.main(args);

      // assert
      verify(server, times(1)).execute();
      assertThat(printStreamCaptor.getValue()).isEqualTo(System.err);
      assertThat(serverProcessArgumentCaptor.getValue()).isNotNull();
    }
  }
}
