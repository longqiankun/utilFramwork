package com.lqk.framework.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**   
 * @Title: ToastUtils.java 
 * @Package com.dilitech.qiyebao.utils 
 * @Description:  提示工具
 * @author longqiankun   
 * @date 2013-6-19 下午5:41:01 
 * @version V1.0  
 * @Email:qiankun.long@dilitech.com
 */
public class ToastUtils {
	private static int textsize=15;
	private static int textColor=Color.WHITE;
	private static int bg=android.R.drawable.toast_frame;

	public static void setTextsize(int textsize) {
		ToastUtils.textsize = textsize;
	}

	public static void setTextColor(int textColor) {
		ToastUtils.textColor = textColor;
	}

	public static void setBg(int bg) {
		ToastUtils.bg = bg;
	}

	public static void setStyle(int textsize,int textColor,int bg){
		ToastUtils.textsize = textsize;
		ToastUtils.textColor = textColor;
		ToastUtils.bg = bg;
	}
	/**
	 * 
	* @Title: showToast
	* @Description: 提示信息
	* @param @param activity
	* @param @param text
	* @return void
	* @throws
	 */
	public static void showToast(Activity activity,String text){
		showToast(activity.getApplicationContext(),text);
	}
	/**
	 * 
	* @Title: showToast
	* @Description: 提示信息
	* @param @param activity
	* @param @param text
	* @return void
	* @throws
	 */
	public static void showToast(Context activity,String text){
		Toast toast = new Toast(activity);
		 LinearLayout linearLayout = new LinearLayout(activity);
		 linearLayout.setOrientation(LinearLayout.VERTICAL); 
		 linearLayout.setPadding(20, 5, 5, 20);
		
	/*	// 定义一个ImageView
		 ImageView imageView = new ImageView(activity);
		 imageView.setImageResource(R.drawable.voice_to_short); // 图标
*/		 
		 TextView mTv = new TextView(activity);
		 mTv.setText(text);
		 mTv.setTextSize(textsize);
		 mTv.setTextColor(textColor);//字体颜色
		 //mTv.setPadding(0, 10, 0, 0);
		 
		// 将ImageView和ToastView合并到Layout中
//		 linearLayout.addView(imageView);
		 linearLayout.addView(mTv);
		 linearLayout.setGravity(Gravity.CENTER);//内容居中
		 linearLayout.setBackgroundResource(bg);//设置自定义toast的背景
		 
		 toast.setView(linearLayout); 
		 toast.setGravity(Gravity.CENTER, 0,0);//起点位置为中间     100为向下移100dp
		 toast.show();				
	}
}
