package com.lqk.framework.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.lqk.framework.app.Ioc;

/**
 * 获取相关系统信息
 * 
 */
public class Handler_System {

	public static String UA = Build.MODEL;
	private static String mIMEI;
	private static String mSIM;
	private static String mMobileVersion;
	private static String mNetwrokIso;
	private static String mNetType;
	private static String mDeviceID;
	private static List<NeighboringCellInfo> mCellinfos;

	public static final String systemWidth = "width";
	public static final String systemHeight = "height";
	private static HashMap<String, Integer> map;

	public static final int DEVICE_PHONE = 1;
	public static final int DEVICE_PAD = 2;
	static {
		init();
	}

	/**
	 * 获取应用程序名称
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:30:56
	 * @return
	 * @return String
	 */
	public static String getAppName() {
		return getAppName(null);
	}

	/**
	 * 获取应用程序名称
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:30:43
	 * @param packageName
	 * @return
	 * @return String
	 */
	public static String getAppName(String packageName) {
		String applicationName;

		if (packageName == null) {
			packageName = Ioc.getIoc().getApplication().getPackageName();
		}

		try {
			PackageManager packageManager = Ioc.getIoc().getApplication()
					.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					packageName, 0);
			applicationName = Ioc.getIoc().getApplication()
					.getString(packageInfo.applicationInfo.labelRes);
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
			applicationName = "";
		}

		return applicationName;
	}

	/**
	 * 获取版本名称
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:30:37
	 * @return
	 * @return String
	 */
	public static String getAppVersionNumber() {
		return getAppVersionNumber(null);
	}

	/**
	 * 获取版本名称
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:30:27
	 * @param packageName
	 * @return
	 * @return String
	 */
	public static String getAppVersionNumber(String packageName) {
		String versionName;

		if (packageName == null) {
			packageName = Ioc.getIoc().getApplication().getPackageName();
		}

		try {
			PackageManager packageManager = Ioc.getIoc().getApplication()
					.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					packageName, 0);
			versionName = packageInfo.versionName;
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
			versionName = "";
		}

		return versionName;
	}

	/**
	 * 获取应用程序的版本号
	 * 
	 * @return
	 * @return String
	 */
	public static String getAppVersionCode() {
		return getAppVersionCode(null);
	}

	/**
	 * 获取指定应用程序的版本号
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:29:51
	 * @param packageName
	 * @return
	 * @return String
	 */
	public static String getAppVersionCode(String packageName) {
		String versionCode;

		if (packageName == null) {
			packageName = Ioc.getIoc().getApplication().getPackageName();
		}

		try {
			PackageManager packageManager = Ioc.getIoc().getApplication()
					.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					packageName, 0);
			versionCode = Integer.toString(packageInfo.versionCode);
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
			versionCode = "";
		}

		return versionCode;
	}

	/**
	 * 获取SDK版本
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:29:13
	 * @return
	 * @return int
	 */
	public static int getSdkVersion() {
		try {
			return Build.VERSION.class.getField("SDK_INT").getInt(null);
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
			return 3;
		}
	}

	/*
	 * 判断是否是该签名打包
	 */
	public static boolean isRelease(String signatureString) {
		final String releaseSignatureString = signatureString;
		if (releaseSignatureString == null
				|| releaseSignatureString.length() == 0) {
			throw new RuntimeException(
					"Release signature string is null or missing.");
		}

		final Signature releaseSignature = new Signature(releaseSignatureString);
		try {
			PackageManager pm = Ioc.getIoc().getApplication()
					.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(Ioc.getIoc().getApplication()
					.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature sig : pi.signatures) {
				if (sig.equals(releaseSignature)) {
					return true;
				}
			}
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是模拟器
	 * 
	 * @author gdpancheng@gmail.com 2013-10-12 下午2:28:40
	 * @return
	 * @return boolean
	 */
	public static boolean isEmulator() {
		return Build.MODEL.equals("sdk") || Build.MODEL.equals("google_sdk");
	}

	/**
	 * @author 潘城 gdpancheng@gmail.com 2012-5-7 下午1:21:48
	 * @Title: getMobileInfo
	 * @Description: 获取手机的硬件信息
	 * @param @return 设定文件
	 * @return String 返回类型
	 */
	public static String getMobileInfo() {
		StringBuffer sb = new StringBuffer();
		/**
		 * 通过反射获取系统的硬件信息 获取私有的信息
		 */
		try {
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				String name = field.getName();
				String value = field.get(null).toString();
				sb.append(name + "=" + value);
				sb.append("\n");
			}
		} catch (Exception e) {
			Ioc.getIoc().getLogger().e(e);
		}
		return sb.toString();
	}

	/**
	 * @author 潘城 gdpancheng@gmail.com 2012-5-7 下午5:18:34
	 * @Title: getDisplayMetrics
	 * @Description: 获取屏幕的分辨率
	 * @param @param cx
	 * @param @return 设定文件
	 * @return HashMap<String,Integer> 返回类型
	 */
	public static HashMap<String, Integer> getDisplayMetrics() {
		if (map == null) {
			map = new HashMap<String, Integer>();
			Display display = ((WindowManager) Ioc.getIoc().getApplication()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			map.put(systemWidth, screenWidth);
			map.put(systemHeight, screenHeight);
		}
		return map;
	}

	/**
	 * 获取屏幕宽度缩放率
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:10:47
	 * @param width
	 * @return float
	 */
	public static float getWidthRoate() {
		if (map == null) {
			map = new HashMap<String, Integer>();
			Display display = ((WindowManager) Ioc.getIoc().getApplication()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			map.put(systemWidth, screenWidth);
			map.put(systemHeight, screenHeight);
		}
		return (map.get(systemWidth) * 1.00f) / Ioc.getIoc().getMode_w();
	}

	public static float getRoate() {
		if (map == null) {
			map = new HashMap<String, Integer>();
			Display display = ((WindowManager) Ioc.getIoc().getApplication()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			map.put(systemWidth, screenWidth);
			map.put(systemHeight, screenHeight);
		}
		float w = (map.get(systemWidth) * 1.00f) / Ioc.getIoc().getMode_w();
		float h = (map.get(systemHeight) * 1.00f) / Ioc.getIoc().getMode_h();
		return w > h ? w : h;
	}

	public static float getPadRoate() {
		if (map == null) {
			map = new HashMap<String, Integer>();
			Display display = ((WindowManager) Ioc.getIoc().getApplication()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			map.put(systemWidth, screenWidth);
			map.put(systemHeight, screenHeight);
		}
		float w = (map.get(systemWidth) * 1.00f) / Ioc.getIoc().getMode_w();
		float h = (map.get(systemHeight) * 1.00f) / Ioc.getIoc().getMode_h();
		return w < h ? w : h;
	}

	/**
	 * 获取屏幕高度缩放率
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:11:08
	 * @param height
	 * @return float
	 */
	public static float getHeightRoate() {
		if (map == null) {
			map = new HashMap<String, Integer>();
			Display display = ((WindowManager) Ioc.getIoc().getApplication()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			map.put(systemWidth, screenWidth);
			map.put(systemHeight, screenHeight);
		}
		return (map.get(systemHeight) * 1.00f) / Ioc.getIoc().getMode_h();
	}

	/**
	 * dp转px
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:11:48
	 * @param dipValue
	 * @return int
	 */
	public static int dip2px(float dipValue) {
		final float scale = Ioc.getIoc().getApplication().getResources()
				.getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * px转dip TODO(这里用一句话描述这个方法的作用)
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:12:09
	 * @param pxValue
	 * @return
	 * @return int
	 */
	public static int px2dip(float pxValue) {
		final float scale = Ioc.getIoc().getApplication().getResources()
				.getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 获取通知栏高度
	 * 
	 * @author gdpancheng@gmail.com 2012-2-12 下午07:37:13
	 * @Title: getBarHeight
	 * @param @param context
	 * @param @return 设定文件
	 * @return int 返回类型
	 */
	public static int getBarHeight() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = Ioc.getIoc().getApplication().getResources()
					.getDimensionPixelSize(x);
		} catch (Exception e1) {
			Ioc.getIoc().getLogger().e(e1);
		}
		return sbar;
	}

	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasGingerbreadMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasICS() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static boolean hasJellyBeanMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isHoneycombTablet(Context context) {
		return hasHoneycomb() && isTablet(context);
	}

	public static boolean isGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork == null || !activeNetwork.isConnected()) {
			return false;
		}
		return true;
	}

	public static final int DEFAULT_THREAD_POOL_SIZE = getDefaultThreadPoolSize();

	/**
	 * get recommend default thread pool size
	 * 
	 * @return if 2 * availableProcessors + 1 less than 8, return it, else
	 *         return 8;
	 * @see {@link #getDefaultThreadPoolSize(int)} max is 8
	 */
	public static int getDefaultThreadPoolSize() {
		return getDefaultThreadPoolSize(8);
	}

	/**
	 * get recommend default thread pool size
	 * 
	 * @param max
	 * @return if 2 * availableProcessors + 1 less than max, return it, else
	 *         return max;
	 */
	public static int getDefaultThreadPoolSize(int max) {
		int availableProcessors = 2 * Runtime.getRuntime()
				.availableProcessors() + 1;
		return availableProcessors > max ? max : availableProcessors;
	}

	/**
	 * 
	 * 设置手机立刻震动
	 * */
	public static void Vibrate(long milliseconds) {
		Vibrator vib = (Vibrator) Ioc.getIoc().getApplication()
				.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(milliseconds);
	}

	static TelephonyManager mTm = null;

	/**
	 * 在获取系统信息前初始化
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:14:12
	 * @return void
	 */
	public static void init() {
		mTm = (TelephonyManager) Ioc.getIoc().getApplication()
				.getSystemService(Context.TELEPHONY_SERVICE);
		/*mIMEI = mTm.getDeviceId();
		mMobileVersion = mTm.getDeviceSoftwareVersion();
		mCellinfos = mTm.getNeighboringCellInfo();
		mNetwrokIso = mTm.getNetworkCountryIso();
		mSIM = mTm.getSimSerialNumber();
		mDeviceID = getDeviceId();
		try {
			ConnectivityManager cm = (ConnectivityManager) Ioc.getIoc()
					.getApplication()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null) {
				mNetType = info.getTypeName();
			}
		} catch (Exception ex) {
		}*/
	}

	/**
	 * 获得android设备-唯一标识，android2.2 之前无法稳定运行.
	 * */
	public static String getDeviceId(Context mCm) {
		return Secure.getString(mCm.getContentResolver(), Secure.ANDROID_ID);
	}

	/**
	 * 获取设备号 TODO(这里用一句话描述这个方法的作用)
	 * 
	 * @author gdpancheng@gmail.com 2013-10-15 下午10:31:48
	 * @return
	 * @return String
	 */
	private static String getDeviceId() {
		return Secure.getString(Ioc.getIoc().getApplication()
				.getContentResolver(), Secure.ANDROID_ID);
	}

	public static String getImei() {
		return mIMEI;
	}

	public static String getSIM() {
		return mSIM;
	}

	public static String getUA() {
		return UA;
	}

	/**
	 * 获取设备信息 以字符串的形式
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:14:30
	 * @return String
	 */
	public static String getDeviceInfo() {
		StringBuffer info = new StringBuffer();
		info.append("IMEI:").append(getImei());
		info.append("\n");
		info.append("SIM:").append(getSIM());
		info.append("\n");
		info.append("UA:").append(getUA());
		info.append("\n");
		info.append("MobileVersion:").append(mMobileVersion);

		info.append("\n");
		info.append("SDK: ").append(android.os.Build.VERSION.SDK);
		info.append("\n");
		info.append(getCallState());
		info.append("\n");
		info.append("SIM_STATE: ").append(getSimState());
		info.append("\n");
		info.append("SIM: ").append(getSIM());
		info.append("\n");
		info.append(getSimOpertorName());
		info.append("\n");
		info.append(getPhoneType());
		info.append("\n");
		info.append(getPhoneSettings());
		info.append("\n");
		return info.toString();
	}

	/**
	 * 检查sim的状态
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:15:25
	 * @return String
	 */
	public static String getSimState() {
		switch (mTm.getSimState()) {
		case android.telephony.TelephonyManager.SIM_STATE_UNKNOWN:
			return "未知SIM状态_"
					+ android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;
		case android.telephony.TelephonyManager.SIM_STATE_ABSENT:
			return "没插SIM卡_"
					+ android.telephony.TelephonyManager.SIM_STATE_ABSENT;
		case android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return "锁定SIM状态_需要用户的PIN码解锁_"
					+ android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED;
		case android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return "锁定SIM状态_需要用户的PUK码解锁_"
					+ android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED;
		case android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return "锁定SIM状态_需要网络的PIN码解锁_"
					+ android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED;
		case android.telephony.TelephonyManager.SIM_STATE_READY:
			return "就绪SIM状态_"
					+ android.telephony.TelephonyManager.SIM_STATE_READY;
		default:
			return "未知SIM状态_"
					+ android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;

		}
	}

	/**
	 * 获取手机信号状态
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:15:37
	 * @return String
	 */
	public static String getPhoneType() {
		switch (mTm.getPhoneType()) {
		case android.telephony.TelephonyManager.PHONE_TYPE_NONE:
			return "PhoneType: 无信号_"
					+ android.telephony.TelephonyManager.PHONE_TYPE_NONE;
		case android.telephony.TelephonyManager.PHONE_TYPE_GSM:
			return "PhoneType: GSM信号_"
					+ android.telephony.TelephonyManager.PHONE_TYPE_GSM;
		case android.telephony.TelephonyManager.PHONE_TYPE_CDMA:
			return "PhoneType: CDMA信号_"
					+ android.telephony.TelephonyManager.PHONE_TYPE_CDMA;
		default:
			return "PhoneType: 无信号_"
					+ android.telephony.TelephonyManager.PHONE_TYPE_NONE;
		}
	}

	/**
	 * 服务商名称：例如：中国移动、联通 　　 SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断). 　　
	 */
	public static String getSimOpertorName() {
		if (mTm.getSimState() == android.telephony.TelephonyManager.SIM_STATE_READY) {
			StringBuffer sb = new StringBuffer();
			sb.append("SimOperatorName: ").append(mTm.getSimOperatorName());
			sb.append("\n");
			sb.append("SimOperator: ").append(mTm.getSimOperator());
			sb.append("\n");
			sb.append("Phone:").append(mTm.getLine1Number());
			return sb.toString();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("SimOperatorName: ").append("未知");
			sb.append("\n");
			sb.append("SimOperator: ").append("未知");
			return sb.toString();
		}
	}

	/**
	 * 获取手机设置状态 比如蓝牙开启与否
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:16:02
	 * @return String
	 */
	public static String getPhoneSettings() {
		StringBuffer buf = new StringBuffer();
		String str = Secure.getString(Ioc.getIoc().getApplication()
				.getContentResolver(), Secure.BLUETOOTH_ON);
		buf.append("蓝牙:");
		if (str.equals("0")) {
			buf.append("禁用");
		} else {
			buf.append("开启");
		}
		//
		str = Secure.getString(Ioc.getIoc().getApplication()
				.getContentResolver(), Secure.BLUETOOTH_ON);
		buf.append("WIFI:");
		buf.append(str);

		str = Secure.getString(Ioc.getIoc().getApplication()
				.getContentResolver(), Secure.INSTALL_NON_MARKET_APPS);
		buf.append("APP位置来源:");
		buf.append(str);

		return buf.toString();
	}

	/**
	 * 获取电话状态
	 * 
	 * @author gdpancheng@gmail.com 2013-10-22 下午1:16:37
	 * @return String
	 */
	public static String getCallState() {
		switch (mTm.getCallState()) {
		case android.telephony.TelephonyManager.CALL_STATE_IDLE:
			return "电话状态[CallState]: 无活动";
		case android.telephony.TelephonyManager.CALL_STATE_OFFHOOK:
			return "电话状态[CallState]: 无活动";
		case android.telephony.TelephonyManager.CALL_STATE_RINGING:
			return "电话状态[CallState]: 无活动";
		default:
			return "电话状态[CallState]: 未知";
		}
	}

	public static String getNetwrokIso() {
		return mNetwrokIso;
	}

	/**
	 * @return the mDeviceID
	 */
	public String getmDeviceID() {
		return mDeviceID;
	}

	/**
	 * @return the mNetType
	 */
	public String getNetType() {
		return mNetType;
	}

	//
	/**
	 * 
	 * @author longqiankun
	 * @description :获取设备类型,判断是Android平板，还是Android手机
	 * @param context
	 * @return 设备类型 0：手机 1：平板
	 */
	public static int getDeviceType(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		// 屏幕尺寸
		double screenInches = Math.sqrt(x + y);

		// 大于6尺寸则为Pad
		if (screenInches >= 6.0) {
			return DEVICE_PAD;
		}
		return DEVICE_PHONE;
	}

	// 设置Android设备的横竖屏模式
	public static void setScreenOrientation(Activity context) {
		int deviceType = getDeviceType(context);
		if (deviceType == 0) {
			// 手机竖屏
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			// 平板横屏
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	


	public static String getMetaDataValue(String name, String def) {
		String value = getMetaDataValue(name);
		return (value == null) ? def : value;

	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 获取matedate
	 * @param name
	 * @return
	 */
	private static String getMetaDataValue(String name) {
		Object value = null;
		PackageManager packageManager = Ioc.getIoc().getApplication()
				.getPackageManager();
		ApplicationInfo applicationInfo;
		try {
			applicationInfo = packageManager.getApplicationInfo(Ioc.getIoc()
					.getApplication()
					.getPackageName(), 128);
			if (applicationInfo != null && applicationInfo.metaData != null) {
				value = applicationInfo.metaData.get(name);
			}
		} catch (NameNotFoundException e) {
			throw new RuntimeException(
			"Could not read the name in the manifest file.", e);
		}
		if (value == null) {
			throw new RuntimeException("The name '" + name
			+ "' is not defined in the manifest file's meta data.");
		}
		return value.toString();
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 判断当前界面是否是从桌面启动
	 * @return
	 */
	public static boolean isLauncheredFromHome() {
		boolean isLauncherdFromHome = false;
		ActivityManager mActivityManager = (ActivityManager) Ioc.getIoc()
				.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> mRunningTaskInfos = mActivityManager
				.getRunningTasks(2);
		if (mRunningTaskInfos.size() > 1) {
			String topActivityName = mRunningTaskInfos.get(1).baseActivity
					.getPackageName();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_HOME);
			List<ResolveInfo> activities = Ioc.getIoc().getApplication()
					.getPackageManager().queryIntentActivities(intent, 0);
			for (ResolveInfo resolveInfo : activities) {
				String pName = resolveInfo.activityInfo.packageName;
				if (topActivityName.equals(pName)) {
					isLauncherdFromHome = true;
				}
			}
		}
		return isLauncherdFromHome;
	}

	/**
	 * 
	 * @author longqiankun
	 * @description :判断是否是自己启动
	 * @return
	 */
	public static boolean isSelfLauncher() {  
	    final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);  
	    filter.addCategory(Intent.CATEGORY_HOME);  
	  
	    List<IntentFilter> filters = new ArrayList<IntentFilter>();  
	    filters.add(filter);  
	  
	    final String myPackageName = Ioc.getIoc()
				.getApplication().getPackageName();  
	    List<ComponentName> activities = new ArrayList<ComponentName>();  
	    final PackageManager packageManager = (PackageManager) Ioc.getIoc()
				.getApplication().getPackageManager();  
	  
	    packageManager.getPreferredActivities(filters, activities, null);  
	    for (ComponentName activity : activities) {  
	        if (myPackageName.equals(activity.getPackageName())) {  
	            return true;  
	        }  
	    }  
	    return false;  
	} 
	
	/**
	 * 
	 * @author longqiankun
	 * @description : TODO
	 * @return
	 */
	   public static String getSerialNo(){
	  		String serialnum = null;                                                                                                                                        
	  		try {                                                           
	  		 Class<?> c = Class.forName("android.os.SystemProperties"); 
	  		 Method get = c.getMethod("get", String.class, String.class );     
	  		 serialnum = (String)(get.invoke(c, "ro.serialno", "unknown" ));  
	  		} catch (Exception e) {                              
	  			e.printStackTrace();
	  		}
	  		return serialnum;
	  	}
	   
	   /**
		 * 
		* @Title: getDeviceUnique
		* @Description: 获取设备的唯一标示
		* @param @param context
		* @param @return
		* @return String
		* @throws
		 */
		public static String getDeviceUnique(Context context){
			
			//1并且用户应当允许安装此应用。作为手机来讲，IMEI是唯一的，它应该类似于 359881030314356（除非你有一个没有量产的手机（水货）它可能有无
			TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); 
			String m_szImei = TelephonyMgr.getDeviceId();
			
			//2通过取出ROM版本、制造商、CPU型号、以及其他硬件信息来实现这一点。这样计算出来的ID不是唯一的（因为如果两个手机应用了同样的硬件以及Rom 镜像）
			String m_szDevIDShort = "35" + //we make this look like a valid IMEI 
			Build.BOARD.length()%10+ Build.BRAND.length()%10 + Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + Build.DISPLAY.length()%10 + Build.HOST.length()%10 + Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + Build.TAGS.length()%10 + Build.TYPE.length()%10 + Build.USER.length()%10 ; //13 digits  
			
			//3通常被认为不可信，因为它有时为null。开发文档中说明了：这个ID会改变如果进行了出厂设置。并且，如果某个Andorid手机被Root过的话，这个ID也可以被任意改变。
			String m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			
			//4是另一个唯一ID。但是你需要为你的工程加入android.permission.ACCESS_WIFI_STATE 权限，否则这个地址会为null。
			WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE); 
			String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
			
			//5只在有蓝牙的设备上运行
			BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter  
			try{
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
			}catch (Exception e) {
				// TODO: handle exception
			}
			String m_szBTMAC ="";
			if(m_BluetoothAdapter!=null){
				m_szBTMAC=m_BluetoothAdapter.getAddress();
			}
			
			String m_szLongID = m_szImei + m_szDevIDShort 
					+ m_szAndroidID+ m_szWLANMAC + m_szBTMAC;  
			
				// compute md5  
				 MessageDigest m = null;   
				try {
				 m = MessageDigest.getInstance("MD5");
				 } catch (NoSuchAlgorithmException e) {
				 e.printStackTrace();   
				}    
				m.update(m_szLongID.getBytes(),0,m_szLongID.length());   
				// get md5 bytes   
				byte p_md5Data[] = m.digest();   
				// create a hex string   
				String m_szUniqueID = new String();   
				for (int i=0;i<p_md5Data.length;i++) {   
				     int b =  (0xFF & p_md5Data[i]);    
				// if it is a single digit, make sure it have 0 in front (proper padding)    
				    if (b <= 0xF) 
				        m_szUniqueID+="0";    
				// add number to string    
				    m_szUniqueID+=Integer.toHexString(b); 
				   }   // hex string to uppercase   
				m_szUniqueID = m_szUniqueID.toUpperCase();
				
				return m_szUniqueID;
		}
		public static void installApk(String filePath) {
			File apkfile = new File(filePath);
			if (!apkfile.exists()||!filePath.endsWith(".apk")) {
				return;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW);
			
			String permission = "777"; 
			String command = "chmod " + permission + " " + apkfile.getAbsolutePath(); 
			Runtime runtime = Runtime.getRuntime(); 
			try {
				runtime.exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
			Ioc.getIoc().getApplication().startActivity(intent);
		}
		 /** 
	     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数. 
	     */  
	    public static void setFieldValue(final Object object, final String fieldName, final Object value) {  
	        Field field = getDeclaredField(object, fieldName);  
	        if (field == null)  
	            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");  
	        makeAccessible(field);  
	        try {  
	            field.set(object, value);  
	        } catch (IllegalAccessException e) {  
	        Log.e("zbkc", "", e);  
	        }  
	    }  
	  
	    /** 
	     * 循环向上转型,获取对象的DeclaredField. 
	     */  
	    protected static Field getDeclaredField(final Object object, final String fieldName) {  
	        return getDeclaredField(object.getClass(), fieldName);  
	    }  
	  
	    /** 
	     * 循环向上转型,获取类的DeclaredField. 
	     */  
	    @SuppressWarnings("unchecked")  
	    protected static Field getDeclaredField(final Class clazz, final String fieldName) {  
	        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {  
	            try {  
	                return superClass.getDeclaredField(fieldName);  
	            } catch (NoSuchFieldException e) {  
	                // Field不在当前类定义,继续向上转型  
	            }  
	        }  
	        return null;  
	    }  
	    /** 
	     * 强制转换fileld可访问. 
	     */  
	    protected static void makeAccessible(Field field) {  
	        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {  
	            field.setAccessible(true);  
	        }  
	    }  
	    
	    /**
		 * 判断服务是否正在运行
		 * @param context
		 * @param className
		 * @return
		 */
		public static boolean isServiceRunning(Context context,String className) {
			boolean isRunning = false;
			ActivityManager activityManager = (ActivityManager)context.
					getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> serviceList = activityManager
					.getRunningServices(50);
			if (!(serviceList.size() > 0)) {
				return false;
			}
			for (int i = 0; i < serviceList.size(); i++) {
				if (serviceList.get(i).service.getClassName().equals(className) == true) {
					isRunning = true;
					break;
				}
			}
			return isRunning;
		}
		/**
		 * @author longqiankun
		 * @description : 判断应用是否安装
		 * @param context 上下文
		 * @param pName 包名
		 * @return 安装结果
		 */
		public static boolean isInstalled(Context context,String pName){
			PackageManager mPackageManager = context.getPackageManager();
			List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES );
			for (int i = 0; i < installedPackages.size(); i++) {
				PackageInfo pInfo=installedPackages.get(i);
				if(pInfo.packageName.equals(pName)){
					return true;
				}
			}
			return false;
		}
		
		public static void runApp(Context context,String packageName,Bundle bundle) {  
	        PackageInfo pi;  
	        try {  
	            pi = context.getPackageManager().getPackageInfo(packageName, 0);  
	            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);  
	            resolveIntent.setPackage(pi.packageName);  
	            PackageManager pManager = context.getPackageManager();  
	            List<ResolveInfo> apps = pManager.queryIntentActivities(  
	                    resolveIntent, 0);  
	  
	            ResolveInfo ri = apps.iterator().next();  
	            if (ri != null) {  
	                packageName = ri.activityInfo.packageName;  
	                String className = ri.activityInfo.name;  
	                Intent intent = new Intent(Intent.ACTION_MAIN);  
	                intent.putExtra("bundle", bundle);
	                ComponentName cn = new ComponentName(packageName, className);  
	                intent.setComponent(cn);  
	                context.startActivity(intent);  
	            }  
	        } catch (NameNotFoundException e) {  
	            e.printStackTrace();  
	        }  
	  
	    }
}
