// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import java.io.IOException;
import java.io.OutputStream;

/** レスポンスヘッダを出力する. 本文の出力開始時(コミット時)に一度だけ呼び出される. */
@FunctionalInterface
public interface HeaderWriter {

  /**
   * @param res レスポンス
   * @param os 出力先
   * @param contentLength 本文の長さ. 負の場合はchunkedで送信する
   * @param gzip 本文をgzip圧縮して送信する場合true
   */
  void write(ZouniServletResponse res, OutputStream os, long contentLength, boolean gzip)
      throws IOException;
}
