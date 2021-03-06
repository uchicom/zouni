package com.uchicom.zouni.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ZouniRequestDispatcher implements RequestDispatcher {

	private String name;
	private Map<String, Servlet> servletMap;
	private File baseFile;
	public ZouniRequestDispatcher(String name, Map<String, Servlet> servletMap, File baseFile) {
		this.name = name;
		this.servletMap = servletMap;
		this.baseFile = baseFile;
	}
	ZipInputStream zis;
	@Override
	public void forward(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		if (name.endsWith(".jsp")) {
			//解析した結果を保持する
			Servlet servlet = getServlet(name, "dir.");
			servlet.service(req, res);
		} else if (name.endsWith(".htm")) {
			//静的ファイル呼び出し
			try {
				file2Stream(new File(name), res.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//サーブレット呼び出し.
			Servlet servlet = getServlet(name, "pub.");
			servlet.service(req, res);
		}
	}

	@Override
	public void include(ServletRequest req, ServletResponse res) throws ServletException, IOException {

	}
	private Servlet getServlet(String name, String keyPrefix) throws FileNotFoundException, IOException {
		String key = null;
		if (name.charAt(0) == '/') {
			key = keyPrefix + name;
		} else {
			key = keyPrefix + "/" + name;
		}
		Servlet servlet = null;
		if (servletMap.containsKey(key)) {
			servlet = servletMap.get(key);
		} else {
			servlet = new ViewServlet(new File(baseFile, name));
			servletMap.put(key, servlet);
		}
		return servlet;
	}
	private void file2Stream(File file, ServletOutputStream sos) {
		try (FileInputStream fis = new FileInputStream(file);) {
			byte[] bytes = new byte[1024 * 4];
			int length = 0;

			while ((length = fis.read(bytes)) > 0) {
				sos.write(bytes, 0, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
