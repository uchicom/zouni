// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZouniServletInputStream extends ServletInputStream {

  private final InputStream is;

  public ZouniServletInputStream(InputStream is) {
    this.is = is;
  }

  @Override
  public boolean isFinished() {
    throw new UnsupportedOperationException("Unimplemented method 'isFinished'");
  }

  @Override
  public boolean isReady() {
    throw new UnsupportedOperationException("Unimplemented method 'isReady'");
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    throw new UnsupportedOperationException("Unimplemented method 'setReadListener'");
  }

  @Override
  public int read() throws IOException {
    return is.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return is.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return is.read(b, off, len);
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    return is.readAllBytes();
  }

  @Override
  public void close() throws IOException {
    is.close();
  }
}
