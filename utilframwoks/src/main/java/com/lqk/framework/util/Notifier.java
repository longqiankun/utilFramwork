/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lqk.framework.util;

import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 
* @ClassName: Notifier
* @Description: 通知
* @author longqiankun
* @date 2014-7-7 上午11:59:42
*
 */

public class Notifier {
	 private static final String LOGTAG = "Notifier";
	    
	    private static final Random random = new Random(System.currentTimeMillis());
	    private Context context;
	    private Class clazz;
	    private NotificationManager notificationManager;
	   private int icon;
	    public Notifier(Context context) {
	        this.context = context;
	        this.notificationManager = (NotificationManager) context
	                .getSystemService(Context.NOTIFICATION_SERVICE);
	    }
	    public Notifier(Context context,Class clazz) {
	        this.context = context;
	        this.notificationManager = (NotificationManager) context
	                .getSystemService(Context.NOTIFICATION_SERVICE);
	        this.clazz=clazz;
	    }
	    
	/**
	 * 
	* @Title: notify
	* @Description: 状态栏通知提示
	* @param @param p_from
	* @param @param p_content
	* @param @param p_time
	* @return void
	* @throws
	 */
	    public void notify(boolean isSingle,String p_from, String p_content, String p_time,int sound,boolean isVibreate) {
	            // Notification
	            Notification notification = new Notification();
	            notification.icon = getNotificationIcon();
//	            notification.defaults = Notification.DEFAULT_LIGHTS;
	            if(sound>0){
	            	playSound(sound);
	            }else{
	            	notification.defaults |= Notification.DEFAULT_SOUND;
	            }
	            long[] vibreate= new long[]{1000,1000,1000,1000,1000};  
	            if(isVibreate){
	            notification.vibrate = vibreate; 
	            }
	            notification.flags |= Notification.FLAG_AUTO_CANCEL;
	            notification.when =System.currentTimeMillis();
	            notification.ledARGB = 0xff00ff00; 
	            notification.ledOnMS = 300; //亮的时间 
	            notification.ledOffMS = 1000; //灭的时间 
	            notification.flags |= Notification.FLAG_SHOW_LIGHTS; 
	            notification.tickerText = p_content;
	            Intent intent = new Intent();
	            if(clazz!=null)
	            	intent=new Intent(context,clazz);
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

	            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
	                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

//	            notification.setLatestEventInfo(context, "", p_content, contentIntent);
	            int noticeid=random.nextInt();
	            if(isSingle){
	            	 notificationManager.notify(0, notification);
	            }else{
	            	 notificationManager.notify(noticeid, notification);
	            }
	           
	    }
	    public void notify(String p_from, String p_content, String p_time) {
	    	notify(true,p_from, p_content, p_time, 0,false);
	    }
	    public void notify(boolean isSingle,String p_from, String p_content, String p_time) {
	    	notify(isSingle,p_from, p_content, p_time, 0,false);
	    }
	    //播放自定义的声音  
	    public void playSound(int sound) {  
	        String uri = "android.resource://" + context.getPackageName() + "/"+sound;  
	        Uri no=Uri.parse(uri);  
	          
	        Ringtone r = RingtoneManager.getRingtone(context,  
	                no);  
	        r.play();  
	    }
	    
	    /**
	     * 
	    * @Title: getNotificationIcon
	    * @Description: 获取状态栏通知图标
	    * @param @return
	    * @return int
	    * @throws
	     */
	    private int getNotificationIcon() {
	        return icon;
	    }
	public void setNotificationIcon(int icon){
		this.icon=icon;
	}
	}