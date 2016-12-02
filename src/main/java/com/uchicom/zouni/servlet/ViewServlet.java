package com.uchicom.zouni.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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

	private List<byte[]> byteList = new ArrayList<byte[]>();
	private List<String> replaceList = new ArrayList<String>();
	public ViewServlet(File templateFile) throws FileNotFoundException, IOException {
		//ここで解析して保持
		try (FileInputStream fis = new FileInputStream(templateFile);) {
			byte[] bytes = new byte[1024 * 4];
			int length = 0;
			boolean program = false;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StringBuffer strBuff = new StringBuffer();
			byte prev = 0;
			while ((length = fis.read(bytes)) > 0) {
				int startIndex = 0;
				int index = -1;
//				System.out.println("length:" + length);
				while ((index = indexOf(bytes, startIndex, length - 1, (byte)'%')) >= 0) {
//					System.out.println("startIndex:" + startIndex);
//					System.out.println("index:" + index);
					if (program) {
						program = false;
						if (index != length - 1) {
							prev = bytes[index + 1];
						}
						strBuff.append(new String(Arrays.copyOfRange(bytes, startIndex, index )));
						replaceList.add(strBuff.toString().trim());
						strBuff.setLength(0);
						startIndex = index + 2;
					} else {
						if (index != 0) {
							prev = bytes[index - 1];
						}
						if (prev == '<') {
							program = true;
							baos.write(bytes, startIndex, index - 1 - startIndex);
							startIndex = index + 1;
							byteList.add(baos.toByteArray());
							baos.reset();
						} else {
							baos.write(bytes, startIndex, index - startIndex + 1);
							startIndex = index + 1;
						}
					}
				}
				//最後の文字列改修
				if (startIndex < length - 1) {
					if (program) {
						strBuff.append(new String(Arrays.copyOfRange(bytes, startIndex, length)));
						//これはエラーだな。
					} else {
						baos.write(bytes, startIndex, length - startIndex);
					}
				}
			}
			if (baos.size() > 0) {
				byteList.add(baos.toByteArray());
				baos.reset();
			}


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
		//解析した情報を元にデータを埋め込んで
//		for (byte[] bytes : byteList) {
//			System.out.println(bytes);
//			System.out.println(new String(bytes));
//		}
//		System.out.println(byteList);
//		System.out.println(replaceList);
		ServletOutputStream sos = res.getOutputStream();
		int iMaxList = byteList.size();
		for (int iList = 0; iList < iMaxList; iList++) {
			byte[] bytes = byteList.get(iList);
			sos.write(bytes, 0, bytes.length);
			System.out.println("bl:" + bytes.length);
			Object obj = null;
			if (iList < replaceList.size()) {
				String key = replaceList.get(iList);
				if (req.getSession() != null) {
					obj = req.getSession().getAttribute(key);
				}

				if (obj != null) {
					writeObject(sos, obj);
				} else {
					obj = req.getAttribute(key);
					if (obj != null) {
						writeObject(sos, obj);
					}
					if (obj == null && key.indexOf(".") > 0) {
						try {
							int lastIndex = key.lastIndexOf(".");
							Class<?> classObject = Class.forName(key.substring(0, lastIndex));
			    	        Method method = classObject.getMethod(key.substring(lastIndex + 1),
			    	                new Class[] {  });
			    	        obj = method.invoke(classObject, new Object[] { });
							writeObject(sos, obj);
			    		} catch (SecurityException e) {
			    			e.printStackTrace();
			    		} catch (NoSuchMethodException e) {
			    			e.printStackTrace();
			    		} catch (IllegalArgumentException e) {
			    			e.printStackTrace();
			    		} catch (IllegalAccessException e) {
			    			e.printStackTrace();
			    		} catch (InvocationTargetException e) {
			    			e.printStackTrace();
			    		} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
			    		}
					}
				}
			}
		}
	}
	public void writeObject(ServletOutputStream sos, Object obj) throws IOException {
		byte[] bytes = null;
		if (obj instanceof Integer) {
			bytes = String.valueOf(((Integer) obj).intValue()).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else if (obj instanceof String) {
			bytes = ((String) obj).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else if (obj instanceof Boolean) {
			bytes = String.valueOf(((Boolean) obj).booleanValue()).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else if (obj instanceof Double) {
			bytes = String.valueOf(((Double) obj).doubleValue()).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else if (obj instanceof Float) {
			bytes = String.valueOf(((Float) obj).floatValue()).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else if (obj instanceof Long) {
			bytes = String.valueOf(((Long) obj).longValue()).getBytes();
			sos.write(bytes, 0, bytes.length);
		} else {
			bytes = obj.toString().getBytes();
			sos.write(bytes, 0, bytes.length);
		}
	}
}
