// (C) 2016 uchicom
package com.uchicom.zouni;

import com.uchicom.server.MultiSocketServer;
import com.uchicom.server.PoolSocketServer;
import com.uchicom.server.Server;
import com.uchicom.server.ServerProcessFactory;
import com.uchicom.server.SingleSocketServer;
import com.uchicom.util.Parameter;
import com.uchicom.zouni.servlet.ZouniServletConfig;
import java.io.PrintStream;

public class ZouniParameter extends Parameter {

  public ZouniParameter(String[] args) {
    super(args);
  }

  /** 初期化 */
  public boolean init(PrintStream ps) {
    // 公開ディレクトリの基準フォルダ
    if (!is("public")) {
      put("public", Constants.DEFAULT_PUBLIC);
    }
    // 実行するサーバのタイプ
    if (!is("type")) {
      put("type", "single");
    }
    // ホスト名
    if (!is("host")) {
      put("host", "localhost");
    }
    // 待ち受けポート
    if (!is("port")) {
      put("port", Constants.DEFAULT_PORT);
    }
    // 受信する接続 (接続要求) のキューの最大長
    if (!is("back")) {
      put("back", Constants.DEFAULT_BACK);
    }
    // プールするスレッド数
    if (!is("pool")) {
      put("pool", Constants.DEFAULT_POOL);
    }
    ZouniServletConfig.init(getFile("public"));
    return true;
  }

  public Server createServer(ServerProcessFactory factory) {
    return switch (get("type")) {
      case "multi" -> new MultiSocketServer(this, factory);
      case "pool" -> new PoolSocketServer(this, factory);
      case "single" -> new SingleSocketServer(this, factory);
      default -> null;
    };
  }
}
