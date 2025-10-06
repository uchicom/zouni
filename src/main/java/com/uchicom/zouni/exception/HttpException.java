// (C) 2025 uchicom
package com.uchicom.zouni.exception;

public class HttpException extends Exception {
  public final int statusCode;

  public HttpException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }
}
