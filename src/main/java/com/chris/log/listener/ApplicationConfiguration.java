package com.chris.log.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * 基于jdk1.7+的修改properties配置文件热部署实现方案
 * <p>
 * 该类是单例的，《Java并发编程实践》一书建议用静态内部类单例模式来替代DCL (双重检查模式 : Double Check Pattern)。
 * <p>
 * 参考博文
 * http://howtodoinjava.com/java-7/auto-reload-of-configuration-when-any-change-happen/
 * 
 * @author anonymous author
 * @author Chris
 * @since July 11, 2017
 */
public class ApplicationConfiguration {
	// 第一种传统写法，DCL
//	private static final ApplicationConfiguration ac = new ApplicationConfiguration();
//	private ApplicationConfiguration() {
//	}
//	public static ApplicationConfiguration getInstance() {
//		return ac;
//	}
	
	// 第二种支持多线程，线程安全的写法
	private ApplicationConfiguration() {
	}
	public static ApplicationConfiguration getInstance() {
		return SingletonHolder.getInstance2();
	}
	
	/**
	 * 静态内部类，private access
	 * @author shangpan
	 */
	private static class SingletonHolder {
//		private static final Singleton2 sInstance = new Singleton2(); // 传统写法
		private static ApplicationConfiguration sInstance = getInstance2(); 
		
		private static ApplicationConfiguration getInstance2() {
			if (sInstance == null) {
				synchronized (ApplicationConfiguration.class) {
					if (sInstance == null) {
						sInstance = new ApplicationConfiguration();
					}
				}
			}
			return sInstance;
		}
	}
/***************************************************************************************/ 	
	private static Properties configuration = new Properties();

	private static Properties getConfiguration() {
		return configuration;
	}

	public void initilize(final String file) {
		InputStream in = null;
		try {
			in = new FileInputStream(new File(file));
			configuration.load(in);
		} catch (IOException e) {
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

	public String getConfVal(final String key) {
		return (String) getConfiguration().get(key);
	}
	
	public String getConfValDefaultValue(final String key, final String defaultValue) {
		return getConfiguration().getProperty(key, defaultValue);
	}
}
