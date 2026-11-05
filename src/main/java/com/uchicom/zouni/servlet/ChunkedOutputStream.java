// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import com.uchicom.zouni.Constants;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Transfer-Encoding: chunked 形式で書き込む出力ストリーム.
 *
 * <p>{@link #close()}では下位ストリームを閉じない. 終端チャンクは{@link #finish()}で書き込む.
 */
public class ChunkedOutputStream extends OutputStream {

  private static final byte[] LAST_CHUNK = "0\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

  private final OutputStream os;

  public ChunkedOutputStream(OutputStream os) {
    this.os = os;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] {(byte) b}, 0, 1);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (len == 0) {
      return;
    }
    os.write(Integer.toHexString(len).getBytes(StandardCharsets.US_ASCII));
    os.write(Constants.RES_LINE_END);
    os.write(b, off, len);
    os.write(Constants.RES_LINE_END);
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public void close() throws IOException {
    flush();
  }

  /** 終端チャンクを書き込む. */
  public void finish() throws IOException {
    os.write(LAST_CHUNK);
  }
}
