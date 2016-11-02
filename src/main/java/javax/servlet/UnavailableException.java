package javax.servlet;

public class UnavailableException extends ServletException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UnavailableException(String message) {
		super(message);
	}
	public UnavailableException(String message, int sec) {
		super(message);
	}
	 
	public int getUnavailableException() {
		return 0;
	}
	public Boolean isPermanent() {
		return false;
	}
	 
	// newly deprecated methods
	@Deprecated
	public UnavailableException(Servlet servlet, String message) {
		
	}
	@Deprecated
	public UnavailableException(int sec, Servlet servlet, String msg) {
		
	}
	@Deprecated
	public Servlet getServlet() {
		return null;
	}
}
