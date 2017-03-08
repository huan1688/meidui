package com.meiduimall.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Title : Base64Util 
 * Description : Base64Tools
 * Created By : Kaixuan.Feng 
 * Creation Time : 2016年12月16日 下午2:48:25 
 * -------------------------
 * Modified By : 
 * Modification Time : 
 * Modify Content : 
 * -------------------------
 */
public class Base64Util {

	public Base64Util() {
		
	}

	/**
	 * 功能：编码字符串
	 */
	public static String encode(String data) {
		return new String(encode(data.getBytes()));
	}

	/**
	 * 功能：解码字符串
	 * 
	 * @throws Exception
	 */
	public static String decode(String data) {
		return new String(decode(data.toCharArray()));
	}

	/**
	 * 功能：编码byte[]
	 */
	public static char[] encode(byte[] data) {
		char[] out = new char[((data.length + 2) / 3) * 4];
		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;

			int val = (0xFF & (int) data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & (int) data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & (int) data[i + 2]);
				quad = true;
			}
			out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = alphabet[val & 0x3F];
			val >>= 6;
			out[index + 0] = alphabet[val & 0x3F];
		}
		return out;
	}

	/**
	 * 功能：解码
	 */
	public static byte[] decode(char[] data) {

		int tempLen = data.length;
		for (int ix = 0; ix < data.length; ix++) {
			if ((data[ix] > 255) || codes[data[ix]] < 0) {
				--tempLen; // ignore non-valid chars and padding
			}
		}

		int len = (tempLen / 4) * 3;
		if ((tempLen % 4) == 3) {
			len += 2;
		}
		if ((tempLen % 4) == 2) {
			len += 1;

		}
		byte[] out = new byte[len];

		int shift = 0;
		int accum = 0;
		int index = 0;

		for (int ix = 0; ix < data.length; ix++) {
			int value = (data[ix] > 255) ? -1 : codes[data[ix]];

			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}

		if (index != out.length) {
			throw new Error("Miscalculated data length (wrote " + index + " instead of " + out.length + ")");
		}

		return out;
	}

	/**
	 * 功能：编码文件
	 */
	public static void encode(File file) throws IOException {
		if (!file.exists()) {
			System.exit(0);
		} else {
			byte[] decoded = readBytes(file);
			char[] encoded = encode(decoded);
			writeChars(file, encoded);
		}
		file = null;
	}

	/**
	 * 功能：解码文件。
	 */
	public static void decode(File file) throws IOException {
		if (!file.exists()) {
			System.exit(0);
		} else {
			char[] encoded = readChars(file);
			byte[] decoded = decode(encoded);
			writeBytes(file, decoded);
		}
		file = null;
	}

	private static char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

	private static byte[] codes = new byte[256];

	static {
		for (int i = 0; i < 256; i++) {
			codes[i] = -1;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			codes[i] = (byte) (i - 'A');
		}

		for (int i = 'a'; i <= 'z'; i++) {
			codes[i] = (byte) (26 + i - 'a');
		}
		for (int i = '0'; i <= '9'; i++) {
			codes[i] = (byte) (52 + i - '0');
		}
		codes['+'] = 62;
		codes['/'] = 63;
	}

	private static byte[] readBytes(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = null;
		InputStream fis = null;
		InputStream is = null;
		try {
			fis = new FileInputStream(file);
			is = new BufferedInputStream(fis);
			int count = 0;
			byte[] buf = new byte[16384];
			while ((count = is.read(buf)) != -1) {
				if (count > 0) {
					baos.write(buf, 0, count);
				}
			}
			b = baos.toByteArray();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (is != null)
					is.close();
				if (baos != null)
					baos.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return b;
	}

	private static char[] readChars(File file) throws IOException {
		CharArrayWriter caw = new CharArrayWriter();
		Reader fr = null;
		Reader in = null;
		try {
			fr = new FileReader(file);
			in = new BufferedReader(fr);
			int count = 0;
			char[] buf = new char[16384];
			while ((count = in.read(buf)) != -1) {
				if (count > 0) {
					caw.write(buf, 0, count);
				}
			}
		} finally {
			try {
				if (caw != null)
					caw.close();
				if (in != null)
					in.close();
				if (fr != null)
					fr.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return caw.toCharArray();
	}

	private static void writeBytes(File file, byte[] data) throws IOException {
		OutputStream fos = null;
		OutputStream os = null;
		try {
			fos = new FileOutputStream(file);
			os = new BufferedOutputStream(fos);
			os.write(data);
		} finally {
			try {
				if (os != null)
					os.close();
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private static void writeChars(File file, char[] data) throws IOException {
		Writer fos = null;
		Writer os = null;
		try {
			fos = new FileWriter(file);
			os = new BufferedWriter(fos);
			os.write(data);
		} finally {
			try {
				if (os != null)
					os.close();
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}