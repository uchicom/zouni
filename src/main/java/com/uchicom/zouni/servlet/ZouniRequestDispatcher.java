package com.uchicom.zouni.servlet;

import java.io.File;
import java.io.FileInputStream;
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
	public void forward(ServletRequest req, ServletResponse res) {
//		long start = System.currentTimeMillis();
//	      System.out.println("memory used :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//	      System.out.println("memory total:" + Runtime.getRuntime().totalMemory());
//	      System.out.println("memory max  :" + Runtime.getRuntime().maxMemory());
		if (name.endsWith(".jsp")) {
			Servlet servlet = getServlet(name);
			//解析した結果を保持する
			try {
				servlet.service(req, res);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		}
//		System.out.println(name + ":" + (System.currentTimeMillis() - start));
//
//		System.out.println("memory used :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//		System.out.println("memory total:" + Runtime.getRuntime().totalMemory());
//		System.out.println("memory max  :" + Runtime.getRuntime().maxMemory());
	}

	@Override
	public void include(ServletRequest req, ServletResponse res) {

	}
	private Servlet getServlet(String name) {
		Servlet servlet = null;
		if (servletMap.containsKey(name)) {
			servlet = servletMap.get(name);
		} else {
			servlet = new ViewServlet(new File(baseFile, name));
			servletMap.put(name, servlet);
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
