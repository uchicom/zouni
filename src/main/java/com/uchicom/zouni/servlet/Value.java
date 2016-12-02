// (c) 2016 uchicom
package com.uchicom.zouni.servlet;

import java.util.List;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Value {

	private String parameter;
	private List<String> parameters;

	private Object attribute;

	/**
	 * parameterを取得します.
	 *
	 * @return parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * parameterを設定します.
	 *
	 * @param parameter parameter
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/**
	 * parametersを取得します.
	 *
	 * @return parameters
	 */
	public List<String> getParameters() {
		return parameters;
	}

	/**
	 * parametersを設定します.
	 *
	 * @param parameters parameters
	 */
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * attributeを取得します.
	 *
	 * @return attribute
	 */
	public Object getAttribute() {
		return attribute;
	}

	/**
	 * attributeを設定します.
	 *
	 * @param attribute attribute
	 */
	public void setAttribute(Object attribute) {
		this.attribute = attribute;
	}
}
