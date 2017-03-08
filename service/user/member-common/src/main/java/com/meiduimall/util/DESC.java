package com.meiduimall.util;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.SpringApplication;

/**
 * 数据库加密解密类
 * @author chencong
 *
 */
public class DESC {
	
	/**
	 * 字符串加密 系统默认方式
	 * @param str 需要加密的字符串
	 * @return
	 */
	public static String encryption(String str) {
		try {
			if (StringUtil.isEmptyByString(str) || "null".equals(str)) {
				return null;
			} else {
				if (IS_OPEN) {
					//加密key在config文件中,config文件不在member_common工程中
					return encrypt(str, SystemConfig.configMap.get("API_KEY_NAME"));
				} else {
					return str;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 字符串解密 系统默认方式
	 * @param str 需要解密的字符串
	 * @return
	 */
	public static String deyption(String str) {
		try {
			if (StringUtil.isEmptyByString(str) || "null".equals(str)) {
				return null;
			} else {
				if (IS_OPEN) {
					return decrypt(str, SystemConfig.configMap.get("API_KEY_NAME"));
				} else {
					return str;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 字符串加密
	 * @param str 需要加密的字符串
	 * @param memberId 会员编号
	 * @return
	 */
	public static String encryption(String str, String memberId) {
		try {
			if (StringUtil.isEmptyByString(memberId) || StringUtil.isEmptyByString(str) || "null".equals(str)) {
				return null;
			} else {
				if (IS_OPEN) {
					return encrypt(str, MD5Util.encrypeString(memberId));
				} else {
					return str;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 字符串解密
	 * @param str 需要解密的字符串
	 * @param memberId 会员编号
	 * @return
	 */
	public static String deyption(String str, String memberId) {
		try {
			if (StringUtil.isEmptyByString(memberId) || StringUtil.isEmptyByString(str) || "null".equals(str)) {
				return null;
			} else {
				if (IS_OPEN) {
					return decrypt(str, MD5Util.encrypeString(memberId));
				} else {
					return str;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/******上面加密解密需要调用的字段和方法 start******/
	private static String hexString = "0123456789ABCDEF";

	/*
	 * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	 */
	public static String encode(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	/*
	 * 将16进制数字解码成字符串,适用于所有字符（包括中文）
	 */
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}
	
	
	
	
	private static boolean IS_OPEN = true;

	private static SecretKey keyGenerator(String keyStr) throws Exception {
		byte input[] = HexString2Bytes(keyStr);
		DESKeySpec desKey = new DESKeySpec(input);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(desKey);
		return securekey;
	}

	private static int parse(char c) {
		if (c >= 'a')
			return (c - 'a' + 10) & 0x0f;
		if (c >= 'A')
			return (c - 'A' + 10) & 0x0f;
		return (c - '0') & 0x0f;
	}

	private static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
	}

	private static String encrypt(String data, String key) throws Exception {
		Key deskey = keyGenerator(key);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.ENCRYPT_MODE, deskey, random);
		return Base64.encodeBase64String(cipher.doFinal(data.getBytes("GBK")));
	}

	private static String decrypt(String data, String key) throws Exception {
		Key deskey = keyGenerator(key);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, deskey);
		return new String(cipher.doFinal(Base64.decodeBase64(data)), "GBK");
	}
	/******end******/
	

}