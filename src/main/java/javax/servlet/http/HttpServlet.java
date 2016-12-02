package javax.servlet.http;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.uchicom.zouni.servlet.ZouniServletConfig;

public abstract class HttpServlet extends GenericServlet implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public HttpServlet() {

	}
	@Override
	public ServletConfig getServletConfig() {
		return ZouniServletConfig.getInstance();
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws IOException, ServletException {
		try {
			switch (((HttpServletRequest) req).getMethod()) {
			case "GET":
				doGet((HttpServletRequest) req, (HttpServletResponse)res);
				break;
			case "POST":
				doPost((HttpServletRequest) req, (HttpServletResponse)res);
				break;
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}


	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	protected void doTrace(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}
	public long getLastModified(HttpServletRequest req) {
		return System.currentTimeMillis();
	}
}
