// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ZouniServletResponse implements HttpServletResponse {

  private OutputStream os;
  private String type;
  private List<Cookie> cookieList = new ArrayList<>();

  public ZouniServletResponse(OutputStream os) {
    this.os = os;
  }

  public List<Cookie> getCookieList() {
    return cookieList;
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
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(new OutputStreamWriter(os, Charset.availableCharsets().get("utf-8")));
  }

  @Override
  public void setContentLength(int length) {}

  @Override
  public void setContentType(String type) {
    this.type = type;
  }

  @Override
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
    cookieList.add(cookie);
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
  public boolean isCommitted() {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public void resetBuffer() {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  public void setCharacterEncoding(String arg0) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  public boolean containsHeader(String arg0) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  public void setContentLengthLong(long len) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setContentLengthLong'");
  }

  @Override
  public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sendRedirect'");
  }

  @Override
  public int getStatus() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
  }

  @Override
  public String getHeader(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getHeader'");
  }

  @Override
  public Collection<String> getHeaders(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
  }

  @Override
  public Collection<String> getHeaderNames() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
  }
}
