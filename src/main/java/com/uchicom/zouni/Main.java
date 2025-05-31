// (C) 2025 uchicom
package com.uchicom.zouni;

import com.uchicom.zouni.factory.di.DIFactory;
import com.uchicom.zouni.servlet.RootServlet;
import jakarta.servlet.Servlet;
import java.util.HashMap;

/**
 * 起動クラス.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class Main {

  public static void main(String[] args) {
    var parameter = new ZouniParameter(args);
    if (parameter.init(System.err)) {
      var servlet = new RootServlet(DIFactory.logger(), parameter.get("public"), "user.html");
      var map = new HashMap<String, Servlet>();
      map.put("pub./user/", servlet);
      var startWithMap = new HashMap<String, Servlet>();
      startWithMap.put("pub./user/", servlet);
      parameter.createServer((a, b) -> new ZouniProcess(a, b, map, startWithMap)).execute();
    }
  }
}
