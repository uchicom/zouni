package javax.servlet;

import java.io.IOException;
import java.io.InputStream;

public abstract class ServletInputStream extends InputStream {

	public ServletInputStream() {
		
	}
	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	 
	public int readLine(byte[] buffer, int offset, int length) throws IOException {
		return 0;
	}
}
