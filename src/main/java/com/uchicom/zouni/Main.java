// (C) 2025 uchicom
package com.uchicom.zouni;

import com.uchicom.zouni.dto.IpErrorMessageKey;
import com.uchicom.zouni.factory.di.DIFactory;
import com.uchicom.zouni.servlet.RootServlet;
import jakarta.servlet.Servlet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 起動クラス.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class Main {

  public static void main(String[] args) {
    var logger = DIFactory.logger();
    var zouniParameter = new ZouniParameter(args);
    var servlet = new RootServlet(logger, zouniParameter.get("public"), "user.html");
    var map = new HashMap<String, Servlet>();
    map.put("pub./user/", servlet);
    var startWithMap = new HashMap<String, Servlet>();
    startWithMap.put("pub./user/", servlet);
    var ipErrorMessageCountMap = new ConcurrentHashMap<IpErrorMessageKey, AtomicInteger>();
    zouniParameter
        .createServer(
            (parameter, socket) ->
                new ZouniProcess(
                    parameter, socket, map, startWithMap, logger, ipErrorMessageCountMap))
        .execute();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  if (ipErrorMessageCountMap != null) {
                    DIFactory.logger().info("IpErrorMessageCount:" + ipErrorMessageCountMap);
                  }
                }));
  }
}
