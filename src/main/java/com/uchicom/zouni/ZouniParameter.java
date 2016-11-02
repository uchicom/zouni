// (c) 2016 uchicom
package com.uchicom.zouni;

import java.io.PrintStream;

import com.uchicom.server.MultiSocketServer;
import com.uchicom.server.Parameter;
import com.uchicom.server.PoolSocketServer;
import com.uchicom.server.Server;
import com.uchicom.server.SingleSocketServer;
import com.uchicom.zouni.servlet.ZouniServletConfig;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class ZouniParameter extends Parameter {

	/**
	 * @param args
	 */
	public ZouniParameter(String[] args) {
		super(args);
	}


    /**
     * 初期化
     * @param ps
     * @return
     */
    public boolean init(PrintStream ps) {
    	// メールボックスの基準フォルダ
    	if (!is("dir")) {
    		put("dir", Constants.DEFAULT_DIR);
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
    	ZouniServletConfig.init(getFile("dir"));
        return true;
    }

    public Server createServer() {
    	Server server = null;
		switch (get("type")) {
		case "multi":
			server = new MultiSocketServer(this, (a, b)->{
				return new ZouniProcess(a, b);
			});
			break;
		case "pool":
			server = new PoolSocketServer(this, (a, b)->{
				return new ZouniProcess(a, b);
			});
			break;
		case "single":
			server = new SingleSocketServer(this, (a, b)->{
				return new ZouniProcess(a, b);
			});
			break;
		}
    	return server;
    }
}
