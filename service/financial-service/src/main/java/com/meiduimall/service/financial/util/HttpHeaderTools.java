package com.meiduimall.service.financial.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHeaderTools {

	private static Logger logger = LoggerFactory.getLogger(HttpHeaderTools.class);

	private static final String LOCAL_LOOPBACK_ADDRESS = "127.0.0.1";

	private static final String ERROR_ADDRESS = "0:0:0:0:0:0:0:1";

	/**
	 * 获取当前请求的IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = null;
		try {
			ipAddress = request.getHeader("x-forwarded-for");

			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
				if (LOCAL_LOOPBACK_ADDRESS.equals(ipAddress) || ERROR_ADDRESS.equals(ipAddress)) {
					// 根据网卡取本机配置的IP
					InetAddress inet = null;
					try {
						inet = InetAddress.getLocalHost();
						ipAddress = inet.getHostAddress();
					} catch (UnknownHostException e) {
						logger.error("获取IP失败：" + e);
					}
				}
			}
			// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
			if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
				logger.info("当前请求ip地址未过滤=" + ipAddress); // = 15
				if (ipAddress.indexOf(",") > 0) {
					ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return ipAddress;
	}
}
