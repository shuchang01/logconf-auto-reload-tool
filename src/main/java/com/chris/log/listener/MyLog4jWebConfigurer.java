package com.chris.log.listener;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.Log4jConfigListener;
import org.springframework.web.util.ServletContextPropertyUtils;
import org.springframework.web.util.WebUtils;

import com.alibaba.fastjson.JSON;

/**
 * Convenience class that performs custom log4j initialization for web environments,
 * allowing for log file paths within the web application, with the option to
 * perform automatic refresh checks (for runtime changes in logging configuration).
 *
 * <p><b>WARNING: Assumes an expanded WAR file</b>, both for loading the configuration
 * file and for writing the log files. If you want to keep your WAR unexpanded or
 * don't need application-specific log files within the WAR directory, don't use
 * log4j setup within the application (thus, don't use Log4jConfigListener or
 * Log4jConfigServlet). Instead, use a global, VM-wide log4j setup (for example,
 * in JBoss) or JDK 1.4's {@code java.util.logging} (which is global too).
 *
 * <p>Supports three init parameters at the servlet context level (that is,
 * context-param entries in web.xml):
 *
 * <ul>
 * <li><i>"log4jConfigLocation":</i><br>
 * Location of the log4j config file; either a "classpath:" location (e.g.
 * "classpath:myLog4j.properties"), an absolute file URL (e.g. "file:C:/log4j.properties),
 * or a plain path relative to the web application root directory (e.g.
 * "/WEB-INF/log4j.properties"). If not specified, default log4j initialization
 * will apply ("log4j.properties" or "log4j.xml" in the class path; see the
 * log4j documentation for details).
 * <li><i>"log4jRefreshInterval":</i><br>
 * Interval between config file refresh checks, in milliseconds. If not specified,
 * no refresh checks will happen, which avoids starting log4j's watchdog thread.
 * <li><i>"log4jExposeWebAppRoot":</i><br>
 * Whether the web app root system property should be exposed, allowing for log
 * file paths relative to the web application root directory. Default is "true";
 * specify "false" to suppress expose of the web app root system property. See
 * below for details on how to use this system property in log file locations.
 * </ul>
 *
 * <p>Note: {@code initLogging} should be called before any other Spring activity
 * (when using log4j), for proper initialization before any Spring logging attempts.
 *
 * <p>Log4j's watchdog thread will asynchronously check whether the timestamp
 * of the config file has changed, using the given interval between checks.
 * A refresh interval of 1000 milliseconds (one second), which allows to
 * do on-demand log level changes with immediate effect, is not unfeasible.

 * <p><b>WARNING:</b> Log4j's watchdog thread does not terminate until VM shutdown;
 * in particular, it does not terminate on LogManager shutdown. Therefore, it is
 * recommended to <i>not</i> use config file refreshing in a production J2EE
 * environment; the watchdog thread would not stop on application shutdown there.
 *
 * <p>By default, this configurer automatically sets the web app root system property,
 * for "${key}" substitutions within log file locations in the log4j config file,
 * allowing for log file paths relative to the web application root directory.
 * The default system property key is "webapp.root", to be used in a log4j config
 * file like as follows:
 *
 * <p>{@code log4j.appender.myfile.File=${webapp.root}/WEB-INF/demo.log}
 *
 * <p>Alternatively, specify a unique context-param "webAppRootKey" per web application.
 * For example, with "webAppRootKey = "demo.root":
 *
 * <p>{@code log4j.appender.myfile.File=${demo.root}/WEB-INF/demo.log}
 *
 * <p><b>WARNING:</b> Some containers (like Tomcat) do <i>not</i> keep system properties
 * separate per web app. You have to use unique "webAppRootKey" context-params per web
 * app then, to avoid clashes. Other containers like Resin do isolate each web app's
 * system properties: Here you can use the default key (i.e. no "webAppRootKey"
 * context-param at all) without worrying.
 *
 * @author Juergen Hoeller
 * @author Marten Deinum
 * 
 * @author Chris
 * @since July 12, 2017
 * 
 * @since 12.08.2003
 * @see org.springframework.util.Log4jConfigurer
 * @see Log4jConfigListener
 * @deprecated as of Spring 4.2.1, in favor of Apache Log4j 2
 * (following Apache's EOL declaration for log4j 1.x)
 */
@Deprecated
public abstract class MyLog4jWebConfigurer {
	/** Parameter specifying the location of the log4j config file */
	public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";

	/** Parameter specifying the refresh interval for checking the log4j config file */
	public static final String REFRESH_INTERVAL_PARAM = "log4jRefreshInterval";

	/** Parameter specifying whether to expose the web app root system property */
	public static final String EXPOSE_WEB_APP_ROOT_PARAM = "log4jExposeWebAppRoot";
	
	/** The absolute path of log4j conf file */
	public static String absLocation = null;

	/**
	 * Initialize log4j, including setting the web app root system property.
	 * @param servletContext the current ServletContext
	 * @see WebUtils#setWebAppRootSystemProperty
	 */
	public static void initLogging(ServletContext servletContext) {
		// Expose the web app root system property.
		if (exposeWebAppRoot(servletContext)) {
			WebUtils.setWebAppRootSystemProperty(servletContext);
		}

		// Only perform custom log4j initialization in case of a config file.
		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (location != null) {
			// Perform actual log4j initialization; else rely on log4j's default initialization.
			try {
				// Resolve property placeholders before potentially resolving a real path.
				location = ServletContextPropertyUtils.resolvePlaceholders(location, servletContext);
				servletContext.log("======MyLog4jWebConfigurer======>Resolve log4j conf file from [" + location + "]");
				
				// Leave a URL (e.g. "classpath:" or "file:") as-is.
				if (!ResourceUtils.isUrl(location)) {
					// Consider a plain file path as relative to the web application root directory.
					location = WebUtils.getRealPath(servletContext, location);
				}

				// Write log message to server log.
				servletContext.log("Initializing log4j from [" + location + "]");
				// 信息: Initializing log4j from [classpath:properties/log4j.properties]
				// 
				String log4j = org.apache.commons.lang3.StringUtils.substringAfter(location, ":");
				
				URL url = Thread.currentThread().getContextClassLoader().getResource(log4j);
				String urlStr = JSON.toJSONString(url);
				String log4jstr = org.apache.commons.lang3.StringUtils.substringAfter(urlStr.replaceAll("\"", ""), ":");
				// 
				servletContext.log("Initializing log4j from absolute path [" + log4jstr + "]");
				// 
				absLocation = log4jstr;
				
				// Check whether refresh interval was specified.
				String intervalString = servletContext.getInitParameter(REFRESH_INTERVAL_PARAM);
				if (StringUtils.hasText(intervalString)) {
					// Initialize with refresh interval, i.e. with log4j's watchdog thread,
					// checking the file in the background.
					try {
						long refreshInterval = Long.parseLong(intervalString);
						org.springframework.util.Log4jConfigurer.initLogging(location, refreshInterval);
						// 启动一个新线程来监听文件内容变更
						log4jConfChanged(absLocation, refreshInterval, servletContext);
					}
					catch (NumberFormatException ex) {
						throw new IllegalArgumentException("Invalid 'log4jRefreshInterval' parameter: " + ex.getMessage());
					}
				}
				else {
					// Initialize without refresh check, i.e. without log4j's watchdog thread.
					org.springframework.util.Log4jConfigurer.initLogging(location);
				}
			}
			catch (FileNotFoundException ex) {
				throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ex.getMessage());
			}
		}
	}
	
	/**
	 * 调用核心封装方法
	 * 
	 * @param absLocation the location of the config file: either a "classpath:" location (e.g. "classpath:myLog4j.properties"), 
	 * 			an absolute file URL (e.g. "file:D:/xx/log4j.properties), 
	 * 			or a plain absolute path in the file system (e.g. "/Users/chris/workspaces/demo/log4j.properties")
	 * @param refreshInterval interval between config file refresh checks, in milliseconds
	 * @param servletContext servlet context object
	 */
	private static void log4jConfChanged(final String absLocation, final long refreshInterval, ServletContext servletContext) {
		// Auto-generated method stub
		// execute the listener 
		MyLog4jConfigListener myLog4jConfigListener = new MyLog4jConfigListener(absLocation);
		try {
			new Thread(myLog4jConfigListener).start();
			// test the result
//			while (true) {
//				Thread.sleep(refreshInterval);
////				Thread.sleep(2000L);
//				servletContext.log("======>log4j conf file of property 'log4j.rootLogger'==" + ApplicationConfiguration.getInstance().getConfVal("log4j.rootLogger"));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shut down log4j, properly releasing all file locks
	 * and resetting the web app root system property.
	 * @param servletContext the current ServletContext
	 * @see WebUtils#removeWebAppRootSystemProperty
	 */
	public static void shutdownLogging(ServletContext servletContext) {
		servletContext.log("Shutting down log4j");
		try {
			org.springframework.util.Log4jConfigurer.shutdownLogging();
		}
		finally {
			// Remove the web app root system property.
			if (exposeWebAppRoot(servletContext)) {
				WebUtils.removeWebAppRootSystemProperty(servletContext);
			}
		}
	}

	/**
	 * Return whether to expose the web app root system property,
	 * checking the corresponding ServletContext init parameter.
	 * @see #EXPOSE_WEB_APP_ROOT_PARAM
	 */
	private static boolean exposeWebAppRoot(ServletContext servletContext) {
		String exposeWebAppRootParam = servletContext.getInitParameter(EXPOSE_WEB_APP_ROOT_PARAM);
		return (exposeWebAppRootParam == null || Boolean.valueOf(exposeWebAppRootParam));
	}
}
