package com.lqk.framework.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.graphics.Bitmap;

/**
 * @Title: IoUtils.java
 * @Package com.dilitech.qiyebao.utils
 * @Description:
 * @author longqiankun
 * @date 2013-6-26 下午3:45:39
 * @version V1.0
 * @Email:qiankun.long@dilitech.com
 */
public class CoverByteUtils {
	/**
	 * @description:将字符串转换成流
	 * @param str
	 *            字符串
	 * @return io流
	 */
	public static InputStream String2InputStream(String str) {
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}

	/**
	 * @description:将IO流转换成字符串
	 * @param is
	 *            io流
	 * @return 字符串
	 */
	public static String inputStream2String(InputStream is) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}

	/**
	 * 
	 * @Title: Bitmap2Bytes
	 * @Description: 将bitmap转换成字节数组
	 * @param bm
	 *            bitmap
	 * @return byte[] 转换后的字节数组
	 * @throws
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

		return baos.toByteArray();
	}

	/**
	 * 
	 * @Title: BytesToInStream
	 * @Description:将字节数组转换的输入流
	 * @param bytes
	 *            转换的字节数组
	 * @return InputStream 输入流
	 * @throws
	 */
	public static InputStream BytesToInStream(byte[] bytes) {
		InputStream is = new ByteArrayInputStream(bytes);
		return is;
	}
}
