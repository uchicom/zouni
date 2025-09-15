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
    var zouniParameter = new ZouniParameter(args);
    if (zouniParameter.init(System.err)) {
      var servlet = new RootServlet(DIFactory.logger(), zouniParameter.get("public"), "user.html");
      var map = new HashMap<String, Servlet>();
      map.put("pub./user/", servlet);
      var startWithMap = new HashMap<String, Servlet>();
      startWithMap.put("pub./user/", servlet);
      zouniParameter
          .createServer(
              (parameter, socket) -> new ZouniProcess(parameter, socket, map, startWithMap))
          .execute();
    }
  }
}
