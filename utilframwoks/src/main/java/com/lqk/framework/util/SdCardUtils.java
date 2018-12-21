package com.lqk.framework.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
/**
 * 
* @ClassName: SdCardUtils
* @Description: sd卡工具类
* @author longqiankun
* @date 2014-7-7 下午12:01:45
*
 */
public class SdCardUtils {
	/**
	 * 
	* @Title: ExistSDCard
	* @Description: 检查sd卡是否存在
	* @param @return
	* @return boolean
	* @throws
	 */
	public static  boolean ExistSDCard() {  
		  if (android.os.Environment.getExternalStorageState().equals(  
		    android.os.Environment.MEDIA_MOUNTED)) {  
		   return true;  
		  } else  
		   return false;  
		 }  
/**
 * 
* @Title: getSDAllSize
* @Description: 获取sd卡所有大小
* @param @return
* @return long
* @throws
 */
	public static long getSDAllSize(){  
	     //取得SD卡文件路径   
	     File path = Environment.getExternalStorageDirectory();   
	     StatFs sf = new StatFs(path.getPath());   
	     //获取单个数据块的大小(Byte)   
	     long blockSize = sf.getBlockSize();   
	     //获取所有数据块数   
	     long allBlocks = sf.getBlockCount();  
	     //返回SD卡大小   
	     //return allBlocks * blockSize; //单位Byte   
	     //return (allBlocks * blockSize)/1024; //单位KB   
	     return (allBlocks * blockSize)/1024/1024; //单位MB   
	   } 
	/**
	 * 
	* @Title: getSDFreeSize
	* @Description: 获取sd卡的剩余空间
	* @param @return
	* @return long
	* @throws
	 */
	public static long getSDFreeSize(){  
	     //取得SD卡文件路径   
	     File path = Environment.getExternalStorageDirectory();   
	     StatFs sf = new StatFs(path.getPath());   
	     //获取单个数据块的大小(Byte)   
	     long blockSize = sf.getBlockSize();   
	     //空闲的数据块的数量   
	     long freeBlocks = sf.getAvailableBlocks();  
	     //返回SD卡空闲大小   
	     //return freeBlocks * blockSize;  //单位Byte   
	     //return (freeBlocks * blockSize)/1024;   //单位KB   
	     return (freeBlocks * blockSize)/1024 /1024; //单位MB   
	   } 
	/**
	* @Title: getSDRoot
	* @Description: 获取sd卡根目录
	* @param @return
	* @return String
	* @throws
	 */
	public  static String getSDRoot(){
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	public String getSDAppRoot(Context context) throws NameNotFoundException{
		return getSDRoot()+"/"+getAppName(context);
	}
	/**
	 * 
	* @Title: getFilePath
	* @Description: 获取文件路径
	* @param @param context
	* @param @return
	* @param @throws NameNotFoundException
	* @return String
	* @throws
	 */
	public static String getFilePath(Context context) throws NameNotFoundException{
		return createDir(context, "File");
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 视频路径
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getVideoPath(Context context) throws NameNotFoundException{
		return createDir(context, "Video");
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 音频路径
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getAudioPath(Context context) throws NameNotFoundException{
		return createDir(context, "Audio");
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 临时路径
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getTempPath(Context context) throws NameNotFoundException{
		return createDir(context, "Temp");
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 创建目录
	 * @param context
	 * @param fileName
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String createDir(Context context,String fileName) throws NameNotFoundException{
		String dir=getSDRoot()+"/"+getAppName(context)+"/"+fileName;
		File f=new File(dir);
		if(!f.exists()){
			f.mkdirs();
		}
		return dir;
	}
	
	public static String getDir(Context context,String fileName) throws NameNotFoundException{
		return createDir(context, fileName);
	}
	
	public static void createFile(String filePath) throws IOException{
		File file=new File(filePath);
		if(!file.exists()){
			file.createNewFile();
		}
	}
	/**
	 * 
	* @Title: getImagePath
	* @Description:获取图片路径
	* @param @param context
	* @param @return
	* @param @throws NameNotFoundException
	* @return String
	* @throws
	 */
	public static String getImagePath(Context context) throws NameNotFoundException{
		return createDir(context, "Image");
	}
	/**
	 * 
	* @Title: getAppName
	* @Description:获取应用程序的名称
	* @param @param context
	* @param @return
	* @param @throws NameNotFoundException
	* @return String
	* @throws
	 */
	public static String getAppName(Context context)throws NameNotFoundException{
		String appName=Handler_System.getAppName();
		appName = HanziToPinyin.getInstance().convert(appName, true);
		return appName;
	}
	/**
	 * 
	* @Title: getFileName
	* @Description:根据路径获取文件名
	* @param @param dir 路径
	* @param @return
	* @param @throws NameNotFoundException
	* @return String
	* @throws
	 */
	public static String getFileName(String path)throws NameNotFoundException{
		String fileName="";
		if(path.contains("/")){
			fileName=path.substring(path.lastIndexOf("/") + 1);
		}else if(path.contains("\\")){
			fileName=path.substring(path.lastIndexOf("\\") + 1);
		}else{
			fileName=path;
		}
		return fileName;
	
	
	}
	/**
	 * 
	 * 描述: 获取文件的全路径
	 * @param context
	 * @param dir
	 * @return
	 * @throws IOException
	 * @throws NameNotFoundException
	 */
	public static String getFilePath(Context context,String dir) throws IOException, NameNotFoundException{
		String path=getFilePath(context)+"/"+getFileName(dir);
		createFile(path);
		return path;
	}
	/**
	 * 
	 * 描述: 获取图片的全路径
	 * @param context
	 * @param dir
	 * @return
	 * @throws IOException
	 * @throws NameNotFoundException
	 */
	public static String getImgPath(Context context,String dir) throws IOException, NameNotFoundException{
		String path=getImagePath(context)+"/"+getFileName(dir);
		createFile(path);
		return path;
	}
	

}