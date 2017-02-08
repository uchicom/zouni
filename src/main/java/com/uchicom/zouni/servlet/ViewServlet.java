package com.uchicom.zouni.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

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

	private String script;
	public ViewServlet(File templateFile) throws FileNotFoundException, IOException {
		//ここで解析して保持
		try (FileInputStream fis = new FileInputStream(templateFile);) {
			byte[] bytes = new byte[1024 * 4];
			int length = 0;
			boolean program = false;
			byte prev = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4 * 1024);
			StringBuffer scriptBuff = new StringBuffer(4 * 1024);

			int startIndex = 0;
			while ((length = fis.read(bytes)) > 0) {
				if (baos.size() > 0) {
					baos.write(bytes, 0, length);
					bytes = baos.toByteArray();
					length = baos.size();
					baos.reset();
				}
				startIndex = 0;
				int index = -1;
				while ((index = indexOf(bytes, startIndex, length - 1, (byte)'%')) >= 0) {
					if (program) {
						program = false;
						if (index != length - 1) {
							prev = bytes[index + 1];
						}
						if (bytes[startIndex] == '=') {
							scriptBuff.append("out.print(");
							scriptBuff.append(new String(bytes, startIndex + 1, index - (startIndex+1), Charset.availableCharsets().get("utf-8")));
							scriptBuff.append(");\n");
						} else {
							scriptBuff.append(new String(bytes, startIndex, index - startIndex, Charset.availableCharsets().get("utf-8")));
						}
						scriptBuff.append("\n");
						startIndex = index + 2;
					} else {
						if (index != 0) {
							prev = bytes[index - 1];
							if (prev == '<') {
								program = true;
								if (index != 1) {
									scriptBuff.append("out.print(\"");
									scriptBuff.append(new String(bytes, startIndex, index - 1 - startIndex, Charset.availableCharsets().get("utf-8")).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
									scriptBuff.append("\");\n");
								}
								startIndex = index + 1;
							} else {
								scriptBuff.append("out.print(\"");
								scriptBuff.append(new String(bytes, startIndex, index - startIndex + 1, Charset.availableCharsets().get("utf-8")).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
								scriptBuff.append("\");\n");
								startIndex = index + 1;
							}
						}
					}
				}
				//最後の文字列を保持
				if (startIndex < length - 1) {
					baos.write(bytes, startIndex, length - startIndex);
				}
			}
			if (baos.size() > 0) {
				startIndex = 0;
				length = baos.size();
				bytes = baos.toByteArray();
				if (program) {if (bytes[startIndex] == '=') {
						scriptBuff.append("out.print(");
						scriptBuff.append(new String(bytes, startIndex + 1, length - (startIndex + 1), Charset.availableCharsets().get("utf-8")));
						scriptBuff.append(");\n");
					} else {
						scriptBuff.append(new String(bytes, startIndex, length - startIndex, Charset.availableCharsets().get("utf-8")));
					}
					scriptBuff.append("\n");
					//これはエラーだな。
				} else {
					scriptBuff.append("out.print(\"");
					scriptBuff.append(new String(bytes, startIndex, length - startIndex, Charset.availableCharsets().get("utf-8")).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
					scriptBuff.append("\");\n");
				}
			}
			scriptBuff.append("out.flush();\n");
			script = scriptBuff.toString();
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
			se.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
