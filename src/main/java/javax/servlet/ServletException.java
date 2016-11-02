package javax.servlet;

public class ServletException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServletException() {
		
	}
	public ServletException(String message) {
		super(message);
	}
	public ServletException(String message, Throwable cause) {
		super(message, cause);
	}
	public ServletException(Throwable cause) {
		super(cause);
	}
	 
	public Throwable getRootCause() {
		Throwable cause = this;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}
}
