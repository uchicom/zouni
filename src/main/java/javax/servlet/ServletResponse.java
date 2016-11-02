package javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public interface ServletResponse {
	public String getCharacterEncoding();

	public ServletOutputStream getOutputStream() throws IOException;

	public PrintWriter getPrintWriter() throws IOException;

	public void setContentLength(int length);

	public void setContentType(String type);

	public void setBufferSize(int size);

	public int getBufferSize();

	public void reset();

	public Boolean isComitted();

	public void flushBuffer() throws IOException;

	public void setLocale(Locale locale);

	public Locale getLocale();
}
