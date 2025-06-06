// (C) 2025 uchicom
package com.uchicom.zouni.servlet;

import com.uchicom.zouni.Constants;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.util.Enumeration;

public class ZouniServletConfig implements ServletConfig {

  private static ZouniServletConfig zouniServletConfig;

  private ZouniServletConfig(File pubFile) {
    ZouniServletContext.init(pubFile);
  }

  public static void init(File pubFile) {
    zouniServletConfig = new ZouniServletConfig(pubFile);
  }

  public static ZouniServletConfig getInstance() {
    return zouniServletConfig;
  }

  @Override
  public ServletContext getServletContext() {
    return ZouniServletContext.getInstance();
  }

  @Override
  public String getInitParameter(String name) {
    return null;
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return null;
  }

  @Override
  public String getServletName() {
    return Constants.SERVER_NAME;
  }
}
