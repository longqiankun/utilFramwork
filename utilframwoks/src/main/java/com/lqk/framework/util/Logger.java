package com.lqk.framework.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Hashtable;

import android.os.Environment;
import android.util.Log;

import com.lqk.framework.app.Ioc;
import com.lqk.framework.core.kernel.KernelClass;

/**
 * 日志工具类
 */
public class Logger{

	private static boolean debug = false;
	public static String tag = "Inject_android";
	private final static int logLevel = Log.VERBOSE;
	private static Hashtable<String, Logger> logger = new Hashtable<String, Logger>();
	private String name;

	private Logger(String name) {
		this.name = name;
		Class clazz = KernelClass.forName(Ioc.getIoc().getApplication().getPackageName() + "." + "BuildConfig");
		if (null == clazz) {
			debug = false;
			return;
        }
		 try {
	        Field filed = clazz.getDeclaredField("DEBUG");
	        debug = Boolean.valueOf(filed.get(null).toString());
        } catch (Exception e) {
        }
	}
private static <T>  String getClassName(T t){
	Class clazz = t.getClass();
	String className = clazz.getName();
	className = className.substring(className.lastIndexOf(".")+1);
	return className;
}
	/**
	 * 
	 * @param className
	 * @return
	 */
	public static <T>  Logger getLogger(T t) {
		String name=getClassName(t);
		Logger classLogger = (Logger) logger.get(name);
		if (classLogger == null) {
			classLogger = new Logger(name);
			logger.put(name, classLogger);
		}
		return classLogger;
	}

	/**
	 * Get The Current Function Name
	 * 
	 * @return
	 */
	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}
			return name + "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() + " ]";
		}
		return null;
	}

	public static boolean isDebug() {
		return debug;
	}

	/**
	 * The Log Level:i
	 * 
	 * @param str
	 */
	public void i(Object str) {
		if (debug) {
			if (logLevel <= Log.INFO) {
				String name = getFunctionName();
				if (name != null) {
					Log.i(tag, name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					Log.i(tag, str.toString());
				}
			}
		}
	}
	
	public void s(Object str) {
		if (debug) {
			if (logLevel <= Log.INFO) {
				String name = getFunctionName();
				if (name != null) {
					System.out.println( name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					System.out.println(str.toString());
				}
			}
		}

	}

	/**
	 * The Log Level:d
	 * 
	 * @param str
	 */
	public void d(Object str) {
		if (debug) {
			if (logLevel <= Log.DEBUG) {
				String name = getFunctionName();
				if (name != null) {
					Log.d(tag, name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					Log.d(tag, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:V
	 * 
	 * @param str
	 */
	public void v(Object str) {
		if (debug) {
			if (logLevel <= Log.VERBOSE) {
				String name = getFunctionName();
				if (name != null) {
					Log.v(tag, name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					Log.v(tag, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:w
	 * 
	 * @param str
	 */
	public void w(Object str) {
		if (debug) {
			if (logLevel <= Log.WARN) {
				String name = getFunctionName();
				if (name != null) {
					Log.w(tag, name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					Log.w(tag, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param str
	 */
	public void e(Object str) {
		if (debug) {
			if (logLevel <= Log.ERROR) {
				String name = getFunctionName();
				if (name != null) {
					Log.e(tag, name + "\n" + str + "\n------------------------------------------------------------------------------");
				} else {
					Log.e(tag, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param ex
	 */
	public void e(Exception ex) {
		if (debug) {
			if (logLevel <= Log.ERROR) {
				Log.e(tag, "error", ex);
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param log
	 * @param tr
	 */
	public void e(String log, Throwable tr) {
		if (debug) {
			String line = getFunctionName();
			Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + name + line + ":] " + log + "\n", tr);
		}
	}
	
	/**
	* @Title: saveLog
	* @Description: 保存Log信息到本地
	* @param @param appName 
	* @param @param interName
	* @param @param content Log信息
	* @return void
	* @throws
	 */
	  public static void saveLog(String path,String content) {   
	        if (Environment.getExternalStorageState().equals(   
	                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中    
	        File file = new File(path);   
	            try {
	            	 if (!file.exists()) {  
					file.createNewFile();
	            	 }
					FileWriter writer = new FileWriter(file, true); 
					writer.write("\r\n"+DateUtil.formatDatetime(new Date())+":      "+content); 
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}   
}
	  }
}
