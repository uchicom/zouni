package javax.servlet.http;

import java.util.EventObject;

public class HttpSessionBindingEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HttpSession session;

	private String name;

	public HttpSessionBindingEvent(HttpSession session, String name) {
		super(session);
	}

	public String getName() {
		return this.name;
	}

	public HttpSession getSession() {
		return this.session;
	}
}
