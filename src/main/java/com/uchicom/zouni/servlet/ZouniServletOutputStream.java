package com.uchicom.zouni.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class ZouniServletOutputStream extends ServletOutputStream {

	private OutputStream os;
	public ZouniServletOutputStream(OutputStream os) {
		this.os = os;
	}

	/* (非 Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	
}
