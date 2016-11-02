package javax.servlet;

import java.io.IOException;
import java.io.OutputStream;

public abstract class ServletOutputStream extends OutputStream {

	private OutputStream os;
	public ServletOutputStream(OutputStream os) {
		this.os = os;
	}
	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}
	 
	public void print(String s) throws IOException {
		os.write(s.getBytes());
	}
	public void print(Boolean b) throws IOException {
		os.write(b.toString().getBytes());
	}
	public void print(char c) throws IOException {
		os.write((int)c);
	}
	public void print(int i) throws IOException {
		os.write(String.valueOf(i).getBytes());
	}
	public void print(long l) throws IOException {
		os.write(String.valueOf(l).getBytes());
	}
	public void print(float f) throws IOException {
		os.write(String.valueOf(f).getBytes());
	}
	public void print(double d) throws IOException {
		os.write(String.valueOf(d).getBytes());
	}
	
	public void println() throws IOException {
		os.write("\r\n".getBytes());
	}
	public void println(String s) throws IOException {
		print(s);
		println();
	}
	public void println(boolean b) throws IOException {
		print(b);
		println();
	}
	public void println(char c) throws IOException {
		print(c);
		println();
	}
	public void println(int i) throws IOException {
		print(i);
		println();
	}
	public void println(long l) throws IOException {
		print(l);
		println();
	}
	public void println(float f) throws IOException {
		print(f);
		println();
	}
	public void println(double d) throws IOException {
		print(d);
		println();
	}
}
