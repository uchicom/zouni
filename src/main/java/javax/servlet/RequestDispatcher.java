package javax.servlet;

import java.io.IOException;

public interface RequestDispatcher {
	public void forward(ServletRequest req, ServletResponse res) throws ServletException, IOException;
	public void include(ServletRequest req, ServletResponse res) throws ServletException, IOException ;
}
