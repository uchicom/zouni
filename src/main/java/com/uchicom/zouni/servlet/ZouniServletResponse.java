package com.uchicom.zouni.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class ZouniServletResponse implements HttpServletResponse {

	private OutputStream os;
	private String type;
	public ZouniServletResponse(OutputStream os) {
		this.os = os;
	}
	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ZouniServletOutputStream(os);
	}

	@Override
	public PrintWriter getPrintWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContentLength(int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String type) {
		this.type = type;
	}
	public String getContentType() {
		return type;
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean isComitted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocale(Locale locale) {
		// TODO Auto-generated method stub

	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int status) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int status, String message) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateHeader(String headername, long date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String headername, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addHeader(String headername, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDateHeader(String headername, long date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addIntHeader(String headername, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntHeader(String headername, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int statuscode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int statuscode, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
