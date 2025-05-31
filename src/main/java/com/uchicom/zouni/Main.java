// (C) 2025 uchicom
package com.uchicom.zouni;

/**
 * 起動クラス.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class Main {

  public static void main(String[] args) {
    var parameter = new ZouniParameter(args);
    if (parameter.init(System.err)) {
      parameter.createServer().execute();
    }
  }
}
