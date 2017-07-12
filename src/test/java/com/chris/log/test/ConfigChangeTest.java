package com.chris.log.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.alibaba.fastjson.JSON;

/**
 * <p>
 * 线上正式环境动态(日志升降级)修改日志记录级别实现方案 
 * <p>
 * based on jdk1.7+, spring web mvc4.x, log4j, slf4j(log4j.properties) log-framework
 * <p>
 * 单元测试类
 * 
 * @author Chris
 * @since July 11, 2017
 */
public class ConfigChangeTest {
//	private static final String FILE_PATH = "/Users/shangpan/Pro/tmp/hello.properties";
//	/** Parameter specifying the location of the log4j config file */
//	public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";
//
//	public static void main(String[] args) {
//		// Only perform custom log4j initialization in case of a config file.
////		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
//		String location = ContextUtils.getRequest().getServletContext().getInitParameter(CONFIG_LOCATION_PARAM);
//		org.springframework.util.Assert.notNull(location, "The param of log4jConfigLocation for web.xml must be configured and is not null");
//		ConfigurationChangelistener listener = new ConfigurationChangelistener(location);
//		try {
//			new Thread(listener).start();
//			while (true) {
//				Thread.sleep(2000L);
//				System.out.println(ApplicationConfiguration.getInstance().getConfVal("TEST_KEY"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		String location = "classpath:properties/log4j.properties";
		String log4j = org.apache.commons.lang3.StringUtils.substringAfter(location, ":");
		System.out.println("===111===" + log4j); // properties/log4j.properties
		
		URL url = Thread.currentThread().getContextClassLoader().getResource(log4j);
		String urlStr = JSON.toJSONString(url);
		System.out.println("===222===" + urlStr);
		String log4jstr = org.apache.commons.lang3.StringUtils.substringAfter(urlStr.replaceAll("\"", ""), ":");
		System.out.println("===333===" + log4jstr);
		
		InputStream in = null;
		File f = null;
		try {
			f = new File(log4jstr);
			in = new FileInputStream(f);
			System.out.println("========>" + in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
//	public static void main(String[] args) {
////		File f = new File("/Users/shangpan/Pro/tmp/proxy.txt");
//		File f = new File("/Users/shangpan/Workspaces/Projects/byph/byph-web-framework/byph-web-sample/target/classes/properties/log4j.properties");
//		System.out.println(f);
////		FileInputStream fis = null;
//		InputStream fis = null;
//		try {
//			fis = new FileInputStream(f);
//			System.out.println("======" + fis);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			if (fis != null) {
//				try {
//					fis.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

}
