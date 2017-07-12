package com.chris.log.listener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.util.Assert;
import org.springframework.web.util.Log4jConfigListener;

/**
 * <p>
 * 该类用于替代/WEB-INF/web.xml中的listener节点listener-class配置参数值org.springframework.web.util.Log4jConfigListener
 * <p>
 * 该类本质上覆盖重写了 {@link Log4jConfigListener}
 * 
 * @author Chris
 * @since July 11, 2017
 */
@SuppressWarnings("deprecation")
public class MyLog4jConfigListener implements Runnable, ServletContextListener {
	/** 文件名 */ 
	private String configFileName = null; 
	/** 文件完全绝对路径 */ 
	private String fullFilePath = null;
	
	public MyLog4jConfigListener() {
	}
	
	public MyLog4jConfigListener(final String filePath) {
		this();
		this.fullFilePath = filePath;
	}
	
	@Override
	public void run() {
		// Auto-generated method stub
		register(this.fullFilePath);
	}
	
	private void register(final String file) {
		// Auto-generated method stub
		Assert.notNull(file, "conf file cannot be blank");
		final int lastIndex = file.lastIndexOf("/");
		String dirPath = file.substring(0, lastIndex + 1);
		String fileName = file.substring(lastIndex + 1, file.length());
		this.configFileName = fileName;

		configurationChanged(file);
		try {
			startWatcher(dirPath, fileName);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startWatcher(String dirPath, String file) throws IOException {
		// Auto-generated method stub
		final WatchService watcher = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(dirPath);
		path.register(watcher, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Auto-generated method stub
				try {
					watcher.close();
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		WatchKey key = null;
		while (true) {
			try {
				key = watcher.take();
				for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(configFileName)) {
                        configurationChanged(dirPath + file);
                    }
                }
				boolean isReset = key.reset();
				if (!isReset) {
					System.out.println("======MyLog4jConfigListener======>Could not reset the watch key.");
                    break;
				}
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
	}

	private void configurationChanged(final String file) {
		// Auto-generated method stub
		System.out.println(String.format("======MyLog4jConfigListener======>Refreshing the configuration file == %s", file));
		ApplicationConfiguration.getInstance().initilize(file);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Auto-generated method stub
		MyLog4jWebConfigurer.initLogging(sce.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Auto-generated method stub
		MyLog4jWebConfigurer.shutdownLogging(sce.getServletContext());
	}

}
