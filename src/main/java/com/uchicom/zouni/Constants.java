package com.uchicom.zouni;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Constants {

	public static DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

	//初期設定
	/** デフォルト公開ディレクトリ */
    public static String DEFAULT_PUBLIC = "www";
	/** デフォルト内部ディレクトリ */
    public static String DEFAULT_DIR = "page";
    /** デフォルト待ち受けポート番号 */
    public static String DEFAULT_PORT = "8080";
    /** デフォルト接続待ち数 */
	public static String DEFAULT_BACK = "10";
	/** デフォルトスレッドプール数 */
	public static String DEFAULT_POOL = "10";

	//正常
	public static final byte[] RES_LINE_END = "\r\n".getBytes();
	public static final byte[] RES_200 = "HTTP/1.1 200 OK\r\n".getBytes();
	public static final byte[] RES_CONTENT_TYPE = "Content-Type: text/html; charset=utf-8\r\n".getBytes();
	public static final byte[] RES_CONTENT_LENGTH = "Content-Length: ".getBytes();
	public static final byte[] RES_EXPIRES = "Expires: 43200\r\n".getBytes();
	public static final byte[] RES_LAST_MODIFIED = "Last-Modified: ".getBytes();
	public static final byte[] RES_CONTENT_ENCODING_GZIP = "Content-Encoding: gzip\r\n".getBytes();
	public static final String VERSION = "0.0.1";
	public static final byte[] RES_SERVER = ("Server: Zouni(" + VERSION + ")\r\n").getBytes();
	public static final byte[] SET_COOKIE = "Set-Cookie: ".getBytes();
	public static final byte[] JSESSIONID = "JSESSIONID=".getBytes();

	//エラー
	public static final byte[] RES_404 = "HTTP/1.1 404 Not Found\r\n".getBytes();
	public static final byte[] RES_404_HTML = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1></body></html>".getBytes();
	public static final byte[] RES_440_HTML_LENGTH = String.valueOf(RES_404_HTML.length).getBytes();

	public static final byte[] RES_500 = "HTTP/1.1 500 Internal Server Error\r\n".getBytes();
	public static final byte[] RES_500_HTML = "<html><head><title>500 Internal Server Error</title></head><body><h1>500 Internal Server Error</h1></body></html>".getBytes();
	public static final byte[] RES_500_HTML_LENGTH = String.valueOf(RES_500_HTML.length).getBytes();

	public static String SERVER_NAME = "Zouni";

	public static Properties mimeProperties = new Properties();
	static {
		try {
			mimeProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/uchicom/zouni/mime.properties"));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static String DEFAULT_MIME = "Content-Type: text/plain";
}
