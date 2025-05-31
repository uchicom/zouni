// (C) 2016 uchicom
package com.uchicom.zouni.servlet;

import java.util.List;

public class Value {

  private String parameter;
  private List<String> parameters;

  private Object attribute;

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public Object getAttribute() {
    return attribute;
  }

  public void setAttribute(Object attribute) {
    this.attribute = attribute;
  }
}
