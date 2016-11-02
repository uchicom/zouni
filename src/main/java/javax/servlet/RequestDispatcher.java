package javax.servlet;


public interface RequestDispatcher {
	public void forward(ServletRequest req, ServletResponse res);
	public void include(ServletRequest req, ServletResponse res);
}
