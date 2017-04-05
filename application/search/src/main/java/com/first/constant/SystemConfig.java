package com.first.constant;

import java.util.Map;

import com.first.utility.LoadPropertyUtil;

/**
 * 配置文件参数读取类
 * 
 * @author 刘庆
 *
 */
public class SystemConfig {

	private static final SystemConfig systemConfig = new SystemConfig();
	public static Map<String, String> configMap;
	@SuppressWarnings("static-access")
	private SystemConfig() {
		if (configMap == null) {
			try {
				this.configMap = new LoadPropertyUtil().getPropertyValues("config.properties");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	public static SystemConfig getInstance() { 
			return systemConfig;
	}
}
