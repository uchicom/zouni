package javax.servlet.http;

import java.util.Hashtable;

import javax.servlet.ServletInputStream;

public class HttpUtils {
	public HttpUtils() {
		
	}
	 
	public static Hashtable<String, String> parseQueryString(String queryString) {
		return new Hashtable<String, String>();
	}
	public static Hashtable<String, String> parsePostData(int length, ServletInputStream in) {
		return new Hashtable<String, String>();
	}
	public static StringBuffer getRequestURL(HttpServletRequest req) {
		return new StringBuffer();
	}
}
