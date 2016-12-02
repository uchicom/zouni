package com.uchicom.zouni.servlet;

import java.io.File;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.uchicom.zouni.Constants;

public class ZouniServletConfig implements ServletConfig {

	private static ZouniServletConfig zouniServletConfig;
	private ZouniServletConfig(File baseFile, File pubFile) {
		ZouniServletContext.init(baseFile, pubFile);
	}
	public static void init(File baseFile, File pubFile) {
		zouniServletConfig = new ZouniServletConfig(baseFile, pubFile);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<?> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletName() {
		return Constants.SERVER_NAME;
	}

}
