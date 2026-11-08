// (C) 2026 uchicom
package com.uchicom.zouni;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class ActivityStreamsTest {

  @Test
  public void inputNotifiesOnRead() throws Exception {
    var count = new AtomicInteger();
    var is =
        ActivityStreams.wrap(
            new ByteArrayInputStream(new byte[] {1, 2, 3}), count::incrementAndGet);

    assertThat(is.read()).isEqualTo(1);
    assertThat(is.read(new byte[2], 0, 2)).isEqualTo(2);
    assertThat(count.get()).isEqualTo(2);

    // EOFでは通知しない
    assertThat(is.read()).isEqualTo(-1);
    assertThat(count.get()).isEqualTo(2);
  }

  @Test
  public void outputNotifiesOnWrite() throws Exception {
    var count = new AtomicInteger();
    var out = new ByteArrayOutputStream();
    var os = ActivityStreams.wrap(out, count::incrementAndGet);

    os.write(1);
    os.write(new byte[] {2, 3}, 0, 2);

    assertThat(count.get()).isEqualTo(2);
    assertThat(out.toByteArray()).containsExactly(1, 2, 3);
  }
}
