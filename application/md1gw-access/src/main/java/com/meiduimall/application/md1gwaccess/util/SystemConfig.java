package com.meiduimall.application.md1gwaccess.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 系统配置文件读取类
 * @author chencong
 *
 */
public class SystemConfig {

	private static final SystemConfig systemConfig = new SystemConfig();
	public static Map<String, String> configMap;
	@SuppressWarnings("static-access")
	private SystemConfig() {
		if (configMap == null) {
			try {
				this.configMap = loadProperty("config.properties");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	public static SystemConfig getInstance() { 
		return systemConfig;
	}
	@SuppressWarnings("static-access")
	public static String get(String key) { 
			return systemConfig.configMap.get(key);
	}
	
	/**
	 * Description : 加载配置文件 Created By : Kaixuan.Feng Creation Time :
	 * 2016年12月14日 下午3:02:49
	 * 
	 * @param config
	 * @return
	 */
	public static Map<String, String> loadProperty(String config) {
		InputStream is = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
			Properties pro = new Properties();
			pro.load(is);
			Iterator<Object> localIterator = pro.keySet().iterator();
			while (localIterator.hasNext()) {
				Object key = localIterator.next();
				map.put(key.toString(), pro.get(key).toString());
			}
		} catch (Exception ex) {
			System.out.println("配置文件:" + config + "加载出错!");
			ex.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}