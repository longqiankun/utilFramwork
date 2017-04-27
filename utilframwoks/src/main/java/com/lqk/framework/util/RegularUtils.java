package com.lqk.framework.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

/**   
 * @Title: RegularUtils.java 
 * @Package com.dilitech.qiyebao.utils 
 * @Description:  正则工具类
 * @author longqiankun   
 * @date 2013-7-5 下午12:59:08 
 * @version V1.0  
 * @Email:qiankun.long@dilitech.com
 */
public class RegularUtils {
	/**
	 * @description 验证ip地址是否合法
	 * @param ip ip地址  格式为：111.111.111.111
	 * @return 返回只有true 或false ,如果为true,则传入的Ip地址是合法，否则不合法。
	 */
	public static boolean isIp(String ip){
		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher matcher = pattern.matcher(ip); //以验证127.400.600.2为例
		return matcher.matches();
	}
	/**
	 * @description 验证邮箱地址是否合法
	 * @param email 邮箱地址
	 * @return 返回值为true时合法，false则不合法。
	 */
	 public static boolean isEmail(String email){     
		  boolean flag = false;  
	      try{  
	       String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";  
	       Pattern regex = Pattern.compile(check);  
	       Matcher matcher = regex.matcher(email);  
	       flag = matcher.matches();  
	      }catch(Exception e){  
	       flag = false;  
	      }  
	        
	      return flag;   
	    } 
	 /**
	  * @description 验证手机号码是否合法
	  * @param mobiles 手机号码
	  * @return 返回值为true时合法，false则不合法。
	  */
	 public static boolean isMobileNO(String mobiles){     
//		 String pattern="^[1][3-8]+\\d{9}";
		 String pattern="((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)";
	        Pattern p = Pattern.compile(pattern);     
	        Matcher m = p.matcher(mobiles);     
	        return m.matches();     
	    }
	 /**
	  * @description 验证传入的字符是否是数字
	  * @param digital 需要验证的字符串
	  * @return 返回值为true时表示全是数字，false则不全是数字。
	  */
		public static boolean isDigital(String digital){	
			if(TextUtils.isEmpty(digital)){
				return false;
			}
			return digital.matches("^[+-]?\\d+(\\.\\d+)?$");
			}

	/**
	 * 
	* @Title: matchDateString
	* @Description: 在字符串中匹配出时间
	* @param @param dateStr
	* @param @return
	* @return String
	* @throws
	 */
		public static String matchDateString(String dateStr) {  
		       try {  
		           List matches = null;  
		           Pattern p = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])?(\\s)*(\\d{1,2}([点|时])?((:)?\\d{1,2}(分)?((:)?\\d{1,2}(秒)?)?)?)?(\\s)*(PM|AM)?)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);  
		           Matcher matcher = p.matcher(dateStr);  
		           if (matcher.find() && matcher.groupCount() >= 1) {  
		               matches = new ArrayList();  
		               for (int i = 1; i <= matcher.groupCount(); i++) {  
		                   String temp = matcher.group(i);  
		                   matches.add(temp);  
		               }  
		           } else {  
		               matches = Collections.EMPTY_LIST;  
		           }             
		           if (matches.size() > 0) {  
		               return ((String) matches.get(0)).trim();  
		           } else {  
		           }  
		       } catch (Exception e) {  
		           return "";  
		       }  
		         
		    return dateStr;  
		   }  
		/**
		 * 
		* @Title: isDate
		* @Description: 匹配时间  格式：2009-01-01 12:30:30 
		*                              01/01/2009 12:30:30
		*                              2014年4月25日 15时36分21
		* @param @param strDate
		* @param @return
		* @return boolean
		* @throws
		 */
		public static boolean isDateTime(String strDate){
			String reg1 = "^\\d{4}-[0-1]\\d-[0-3]\\d [0-2][0-4]:[0-6]\\d:[0-6]\\d$";
			String reg2 = "^[0-1][0-2]/[0-3]\\d/\\d{4} [0-2][0-4]:[0-6]\\d:[0-6]\\d$";
			String reg3="(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])?(\\s)*(\\d{1,2}([点|时])?((:)?\\d{1,2}(分)?((:)?\\d{1,2}(秒)?)?)?)?(\\s)*(PM|AM)?)";
			if(strDate!=null&&!"".equals(strDate)){
				if(strDate.matches(reg1)){
					return true;
				}else if(strDate.matches(reg2)){
					return true;
				}else if(strDate.matches(reg3)){
					return true;
				}
			}
			return false;
		}
		/**
		 * 
		 * @author longqiankun
		 * @description : 判断是否是文件
		 * @param fileName
		 * @return
		 */
		public static boolean isFile(String fileName){
			return fileName.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
		}
}
