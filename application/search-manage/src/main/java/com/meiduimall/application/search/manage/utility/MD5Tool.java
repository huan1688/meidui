package com.meiduimall.application.search.manage.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meiduimall.application.search.manage.constant.SysConstant;

public class MD5Tool {
	private static final Log log = LogFactory.getLog(MD5Tool.class);

	// 该方法将你输入的字符串，通过md5加密，返回一个加密后的字符串
	public static String MD5Encrypt16(String inStr) {
		MessageDigest md = null;
		String outStr = null;
		try {
			md = MessageDigest.getInstance("MD5"); // 可以选中其他的算法如SHA
			byte[] digest = md.digest(inStr.getBytes());
			// 返回的是byet[]，要转化为String存储比较方便
			outStr = bytetoString(digest);
		} catch (NoSuchAlgorithmException e) {
			log.error("MD5Encrypt16异常:", e);
		}
		return outStr;
	}

	public static String bytetoString(byte[] digest) {

		String str = "";
		String tempStr = "";
		for (int i = 1; i < digest.length; i++) {
			tempStr = (Integer.toHexString(digest[i] & 0xff));
			if (tempStr.length() == 1) {
				str = str + SysConstant.ZERO + tempStr;
			} else {
				str = str + tempStr;
			}
		}
		return str.toLowerCase();

	}

	// 32位加密
	public static String MD5Encrypt(String values) {
		StringBuilder buf = new StringBuilder("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(values.getBytes());
			byte b[] = md.digest();

			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append(SysConstant.ZERO);
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			log.error("MD5Encrypt异常:", e);
		}
		return buf.toString();
	}

	// 随机生成5位随机数
	public final static String get5Radom() {
		String newString = null;

		// 得到0.0到1.0之间的数字,并扩大100000倍
		double doubleP = Math.random() * 100000;

		// 如果数据等于100000,则减少1
		if (doubleP >= 100000) {
			doubleP = 99999;
		}

		// 然后把这个数字转化为不包含小数点的整数
		int tempString = (int) Math.ceil(doubleP);

		// 转化为字符串
		newString = "" + tempString;

		// 把得到的数增加为固定长度,为5位
		while (newString.length() < 5) {
			newString = SysConstant.ZERO + newString;
		}

		return newString;
	}

	// 主要把传递过来的字符串参数转化为经过MD5算法加密的字符串
	public final static String encrypeString(String neededEncrypedString)
			throws Exception {
		// 初始化加密之后的字符串
		String encrypeString = null;

		// 16进制的数组
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		// 字符串的加密过程
		try {
			// 把需要加密的字符串转化为字节数组
			byte[] neededEncrypedByteTemp = neededEncrypedString.getBytes();

			// 得到MD5的加密算法对象
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 更新算法使用的摘要
			md.update(neededEncrypedByteTemp);

			// 完成算法加密过程
			byte[] middleResult = md.digest();

			// 把加密后的字节数组转化为字符串
			int length = middleResult.length;
			char[] neededEncrypedByte = new char[length * 2];
			int k = 0;
			for (int i = 0; i < length; i++) {
				byte byte0 = middleResult[i];
				neededEncrypedByte[k++] = hexDigits[byte0 >>> 4 & 0xf];
				neededEncrypedByte[k++] = hexDigits[byte0 & 0xf];
			}
			encrypeString = new String(neededEncrypedByte);
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex);
		}

		// 返回加密之后的字符串
		return encrypeString;
	}

	public final static String getMD5String() {
		String md5 = "";
		try {
			md5 = encrypeString(get5Radom());
		} catch (Exception e) {
			log.error("getMD5String异常:", e);
		}
		return md5;
	}

	// 固定密钥加密
	public static String HexEncode(String str) {
		String hexString = null;
		if (str != null && str.length() > 0) {
			char[] digital = "0123456789ABCDEF".toCharArray();
			StringBuilder sb = new StringBuilder("");
			try {
				byte[] bs = str.getBytes("utf-8");
				int bit;
				for (int i = 0; i < bs.length; i++) {
					bit = (bs[i] & 0x0f0) >> 4;
					sb.append(digital[bit]);
					bit = bs[i] & 0x0f;
					sb.append(digital[bit]);
				}
			} catch (Exception e) {
				log.error("HexEncode异常:", e);
			}
			hexString = sb.toString();
		}

		return hexString;
	}

	// 固定密钥解密
	public static String HexDecode(String hexString) {
		String str = null;
		if (hexString != null && hexString.length() > 0) {
			String digital = "0123456789ABCDEF";
			char[] hex2char = hexString.toCharArray();
			byte[] bytes = new byte[hexString.length() / 2];
			int temp;
			for (int i = 0; i < bytes.length; i++) {
				temp = digital.indexOf(hex2char[2 * i]) * 16;
				temp += digital.indexOf(hex2char[2 * i + 1]);
				bytes[i] = (byte) (temp & 0xff);
			}
			try {
				str = new String(bytes, "utf-8");
			} catch (Exception e) {
				log.error("HexDecode异常:", e);
			}
		}
		return str;
	}

}
