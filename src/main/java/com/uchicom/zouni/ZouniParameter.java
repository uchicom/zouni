// (C) 2016 uchicom
package com.uchicom.zouni;

import com.uchicom.http.DefaultRouter;
import com.uchicom.http.StaticHandler;
import com.uchicom.server.MultiSocketServer;
import com.uchicom.server.PoolSocketServer;
import com.uchicom.server.Server;
import com.uchicom.server.ServerProcessFactory;
import com.uchicom.server.SingleSocketServer;
import com.uchicom.server.VirtualSocketServer;
import com.uchicom.util.Parameter;
import com.uchicom.zouni.servlet.ZouniServletConfig;

public class ZouniParameter extends Parameter {

  public ZouniParameter(String[] args) {
    super(args);
    init();
  }

  /** 初期化 */
  void init() {
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
  }

  /**
   * 静的ファイル配信のハンドラを生成する. publicパラメータのディレクトリ直下を公開ディレクトリとして使用する.
   *
   * <p>cache_max_kbにキャッシュ対象とする最大ファイルサイズ(KB)を指定できる. 指定なしまたは0はキャッシュしない.
   */
  public StaticHandler createStaticHandler() {
    long cacheSize = 0;
    if (is("cache_max_kb")) {
      cacheSize = Math.multiplyExact(Long.parseLong(get("cache_max_kb")), 1024L);
    }
    var router = new DefaultRouter();
    router.init(getFile("public"), cacheSize);
    return new StaticHandler(router);
  }

  public Server createServer(ServerProcessFactory factory) {
    return switch (get("type")) {
      case "multi" -> new MultiSocketServer(this, factory);
      case "pool" -> new PoolSocketServer(this, factory);
      case "single" -> new SingleSocketServer(this, factory);
      case "virtual" -> new VirtualSocketServer(this, factory);
      default -> null;
    };
  }
}
