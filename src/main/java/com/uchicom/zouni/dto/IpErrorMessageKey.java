// (C) 2025 uchicom
package com.uchicom.zouni.dto;

public class IpErrorMessageKey {
  String ip;
  String errorMessage;

  public IpErrorMessageKey(String ip, String errorMessage) {
    this.ip = ip;
    this.errorMessage = errorMessage;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ip == null) ? 0 : ip.hashCode());
    result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (obj instanceof IpErrorMessageKey other) {
      if (ip == null) {
        if (other.ip != null) return false;
      } else if (!ip.equals(other.ip)) return false;
      if (errorMessage == null) {
        if (other.errorMessage != null) return false;
      } else if (!errorMessage.equals(other.errorMessage)) return false;
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return ip + ":" + errorMessage;
  }
}
