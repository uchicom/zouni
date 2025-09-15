// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

public class ZouniServletOutputStream extends ServletOutputStream {

  private final OutputStream os;

  public ZouniServletOutputStream(OutputStream os) {
    this.os = os;
  }

  @Override
  public void write(int b) throws IOException {
    os.write(b);
  }

  @Override
  public boolean isReady() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isReady'");
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setWriteListener'");
  }
}
