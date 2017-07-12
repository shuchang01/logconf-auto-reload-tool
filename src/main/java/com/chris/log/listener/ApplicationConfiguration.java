package com.chris.log.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * 参考博文
 * http://howtodoinjava.com/java-7/auto-reload-of-configuration-when-any-change-happen/
 * <p>
 * 基于jdk1.7+的修改properties配置文件热部署实现方案
 * 
 * @author anonymous author
 * @author Chris
 * @since July 11, 2017
 */
public class ApplicationConfiguration {
	private static final ApplicationConfiguration ac = new ApplicationConfiguration();

	private ApplicationConfiguration() {
	}

	public static ApplicationConfiguration getInstance() {
		return ac;
	}
	
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
