package com.uchicom.zouni.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVCモデルのView.
 * @author Shigeki Uchiyama
 *
 */
public class ViewServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private StringBuffer script = new StringBuffer();
	public ViewServlet(File templateFile) throws FileNotFoundException, IOException {
		//ここで解析して保持
		try (FileInputStream fis = new FileInputStream(templateFile);) {
			byte[] bytes = new byte[1024 * 4];
			int length = 0;
			boolean program = false;
			byte prev = 0;
			while ((length = fis.read(bytes)) > 0) {
				int startIndex = 0;
				int index = -1;
				while ((index = indexOf(bytes, startIndex, length - 1, (byte)'%')) >= 0) {
					if (program) {
						program = false;
						if (index != length - 1) {
							prev = bytes[index + 1];
						}
						System.out.println(startIndex + ":a" + (index - startIndex));
						System.out.println(new String(bytes, startIndex, index - startIndex));
						if (bytes[startIndex] == '=') {
							script.append("out.print(");
							script.append(new String(bytes, startIndex + 1, index - (startIndex+1)));
							script.append(");\n");
						} else {
							script.append(new String(bytes, startIndex, index - startIndex));
						}
						script.append("\n");
						startIndex = index + 2;
					} else {
						if (index != 0) {
							prev = bytes[index - 1];
							if (prev == '<') {
								program = true;
								if (index != 1) {
									script.append("out.print(\"");
									System.out.println(startIndex + ":b" + (index - 1 - startIndex));
									System.out.println(new String(bytes, startIndex, index - 1 - startIndex).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
									script.append(new String(bytes, startIndex, index - 1 - startIndex).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
									script.append("\");\n");
								}
								startIndex = index + 1;
							} else {
								script.append("out.print(\"");
								System.out.println(startIndex + ":c" + (index - startIndex));
								System.out.println(new String(bytes, startIndex, index - startIndex + 1).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
								script.append(new String(bytes, startIndex, index - startIndex + 1).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
								script.append("\");\n");
								startIndex = index + 1;
							}
						}
					}
				}
				//最後の文字列改修
				if (startIndex < length - 1) {
					if (program) {
						System.out.println(startIndex + ":d" + (length - startIndex));
						System.out.println(new String(bytes, startIndex, length - startIndex));

						if (bytes[startIndex] == '=') {
							script.append("out.print(");
							script.append(new String(bytes, startIndex + 1, index - (startIndex+1)));
							script.append(");\n");
						} else {
							script.append(new String(bytes, startIndex, index - startIndex));
						}
						script.append("\n");
						//これはエラーだな。
					} else {
						script.append("out.print(\"");
						System.out.println(startIndex + ":e" + (length - startIndex));
						System.out.println(new String(bytes, startIndex, length - startIndex).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
						script.append(new String(bytes, startIndex, length - startIndex).replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
						script.append("\");\n");
					}
				}
			}
			script.append("out.flush();\n");
			System.out.println(script);

		}
	}
	private int indexOf(byte[] bytes, int fromIndex, int toIndex, byte value) {
		for (int i = fromIndex; i <= toIndex; i++) {
			if (bytes[i] == value) return i;
		}
		return -1;
	}
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine se = sem.getEngineByName("JavaScript");
			se.put("out", res.getPrintWriter());
			se.put("request", req);
			se.put("session", req.getSession());
			se.put("response", res);
			se.eval(script.toString());
		} catch (ScriptException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
