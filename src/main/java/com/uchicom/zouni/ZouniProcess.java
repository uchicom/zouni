package com.uchicom.zouni;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.uchicom.server.Parameter;
import com.uchicom.server.ServerProcess;
import com.uchicom.zouni.servlet.FileServlet;
import com.uchicom.zouni.servlet.ViewServlet;
import com.uchicom.zouni.servlet.ZouniServletContext;
import com.uchicom.zouni.servlet.ZouniServletRequest;
import com.uchicom.zouni.servlet.ZouniServletResponse;

public class ZouniProcess implements ServerProcess {
	private static DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("Z"));
	private Socket socket;
	private String servletPackage;
	private File pubDir;
	private Map<String, Servlet> map;
	public ZouniProcess(Parameter parameter, Socket socket) {
		this.socket = socket;
		this.servletPackage = parameter.get("package");
		this.pubDir = parameter.getFile("public");
		this.map = ZouniServletContext.getInstance().getServletMap();
	}

	@Override
	public void execute() {
		try {
			ZouniServletRequest req = new ZouniServletRequest(socket);
			if ("GET".equals(req.getMethod()) || "POST".equals(req.getMethod())) {
				Servlet servlet = null;
				if (map.containsKey(req.getRequestURI())) {
					servlet = map.get(req.getRequestURI());
				} else {
					try {
						String className = null;
						File file = null;
						if (req.getRequestURI().endsWith("/")) {
							File dir = null;
							if ("/".equals(req.getRequestURI())) {
								dir = pubDir;
							} else {
								dir = new File(pubDir, req.getRequestURI());
							}
							file = new File(dir, "index.htm");
							if (!file.exists()) {
								file = new File(dir, "index.html");
								if (!file.exists()) {
									file = new File(dir, "index.jsp");
								}
							}
						} else {
							file = new File(pubDir, req.getRequestURI().substring(1));
						}
						System.out.println(file.getCanonicalPath());

						if (file.exists()) {
							if (file.getCanonicalPath().startsWith(pubDir.getCanonicalPath())) {
								if (file.getCanonicalPath().endsWith(".jsp")) {
									servlet = new ViewServlet(file);
								} else {
									servlet = new FileServlet(file);
								}
							}
						} else {
							if ("/".equals(req.getRequestURI())) {
								className = servletPackage + ".IndexServlet";
							} else if (req.getRequestURI().endsWith("/")) {
								className = servletPackage + req.getRequestURI().replaceAll("/", ".") + "IndexServlet";
								//下の階層に対応していない
//							} else if (req.getRequestURI().split("/").length > 2) {
//								className = servletPackage + req.getRequestURI().replace("/", ".") (1, 2).toUpperCase() + req.getRequestURI().substring(2, req.getRequestURI().length()) + "Servlet";
							} else {
								int lastIndex = req.getRequestURI().lastIndexOf("/");
								className = servletPackage + req.getRequestURI().substring(0, lastIndex + 1).replace('/', '.') + req.getRequestURI().substring(lastIndex + 1, lastIndex + 2).toUpperCase() + req.getRequestURI().substring(lastIndex + 2, req.getRequestURI().length()) + "Servlet";
							}

							Class<?> clazz = Class.forName(className);
	//						System.out.println(className);
							servlet = (HttpServlet)clazz.newInstance();
						}
						if (servlet != null) {
							map.put(req.getRequestURI(), servlet);
						} else {
							map.put(req.getRequestURI(), null);
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					};
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZouniServletResponse res = null;
				GZIPOutputStream gzos = null;
				if (req.isGzip()) {
					gzos = new GZIPOutputStream(baos);
					res = new ZouniServletResponse(gzos);
				} else {
					res = new ZouniServletResponse(baos);
				}
				if (servlet != null) {
					//レスポンスヘッダ出力
					try {
						servlet.service(req, res);
					} catch (ServletException e) {
						try (OutputStream os = socket.getOutputStream();) {
							error500(os);
						}
						throw e;
					}
					try (OutputStream os = socket.getOutputStream();) {
						os.write(Constants.RES_200);
						String contentType = res.getContentType();
						if (contentType != null) {
							os.write(contentType.getBytes());
							os.write(Constants.RES_LINE_END);
						} else {
							os.write(Constants.RES_CONTENT_TYPE);
						}
						os.write(Constants.RES_EXPIRES);
						if (req.isGzip()) {
							os.write(Constants.RES_CONTENT_ENCODING_GZIP);
							gzos.finish();
						}
						os.write(Constants.RES_CONTENT_LENGTH);
						os.write(String.valueOf(baos.size()).getBytes());
						os.write(Constants.RES_LINE_END);
						os.write(Constants.RES_LAST_MODIFIED);
						os.write(formatter.format(OffsetDateTime.now()).getBytes());
						os.write(Constants.RES_LINE_END);
						os.write(Constants.RES_SERVER);
						if (req.getSession() != null && req.getSession().getId() != null) {
							System.out.println("クッキーあり");
							os.write(Constants.SET_COOKIE);
							os.write(Constants.JSESSIONID);
							os.write(req.getSession().getId().getBytes());
							os.write(Constants.RES_LINE_END);
						}
						os.write(Constants.RES_LINE_END);
						System.out.println("baos:" + baos.size());
						os.write(baos.toByteArray());
						os.flush();
					}

				} else {
					try (OutputStream os = socket.getOutputStream();) {
						error404(os);
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (socket != null && socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/* (非 Javadoc)
	 * @see com.uchicom.server.ServerProcess#getLastTime()
	 */
	@Override
	public long getLastTime() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	/* (非 Javadoc)
	 * @see com.uchicom.server.ServerProcess#forceClose()
	 */
	@Override
	public void forceClose() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public static void error404(OutputStream os) throws IOException {
		os.write(Constants.RES_404);
		os.write(Constants.RES_CONTENT_TYPE);
		os.write(Constants.RES_CONTENT_LENGTH);
		os.write(Constants.RES_440_HTML_LENGTH);
		os.write(Constants.RES_LINE_END);
		os.write(Constants.RES_SERVER);
		os.write(Constants.RES_LINE_END);
		os.write(Constants.RES_404_HTML);
		os.flush();
	}
	public static void error500(OutputStream os) throws IOException {
		os.write(Constants.RES_500);
		os.write(Constants.RES_CONTENT_TYPE);
		os.write(Constants.RES_CONTENT_LENGTH);
		os.write(Constants.RES_500_HTML_LENGTH);
		os.write(Constants.RES_LINE_END);
		os.write(Constants.RES_SERVER);
		os.write(Constants.RES_LINE_END);
		os.write(Constants.RES_500_HTML);
		os.flush();
	}
}
