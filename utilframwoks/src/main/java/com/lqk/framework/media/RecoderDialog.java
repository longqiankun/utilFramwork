/*
 * Copyright (c) 2015 longqiankun - All rights
 * reserved.
 */
package com.lqk.framework.media;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lqk.framework.util.SdCardUtils;
import com.lqk.framework.util.ToastUtils;

/**
 * @Title: RecoderDialog.java
 * @Package com.browniesoft.mVideo.activity
 * @author longqiankun
 * @email longqiankun@163.com
 * @company xxx
 * @date 2015-10-13 上午11:18:34
 * @version V1.0
 * @Description: TODO
 */
public class RecoderDialog {

	private Dialog dialog;
	private AudioRecorder mr;
	private Thread recordThread;
	// 最长录制时间，单位秒，0为无时间限制
	private static int MAX_TIME = 15;
	// 最短录制时间，单位秒，0为无时间限制，建议设为1
	private static int MIX_TIME = 1;
	// 不在录音
	private static int RECORD_NO = 0;
	// 正在录音
	private static int RECORD_ING = 1;
	// 完成录音
	private static int RECODE_ED = 2;
	// 录音的状态
	private static int RECODE_STATE = 0;
	// 录音的时间
	private static float recodeTime = 0.0f;
	// 麦克风获取的音量值
	private static double voiceValue = 0.0;
	private ImageView dialog_img;
	// 播放状态
	private static boolean playState = false;
	boolean isType;
	boolean isClickUpload = true;
	long lastTime;
	long lastDownTime;
	boolean isCan = true;
	String picPath;

	Context mContext;
	View view;
	String pressText;
	String unPressText;
	int pressRes;
	int unPressRes;
	int dialogBg;
	int dialogStyle;
	int[] animArr;

	public RecoderDialog(Context mContext, View view, String pressText,
			String unPressText, int pressRes, int unPressRes, int dialogBg,
			int dialogStyle, int[] animArr) {
		super();
		this.mContext = mContext;
		this.view = view;
		this.pressText = pressText;
		this.unPressText = unPressText;
		this.pressRes = pressRes;
		this.unPressRes = unPressRes;
		this.dialogBg = dialogBg;
		this.dialogStyle = dialogStyle;
		this.animArr = animArr;
	}

	boolean isFirst=true;
	// 录制音频
	public void recoder() {
		// 录音
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				long curTime = System.currentTimeMillis();
				if (lastTime == 0) {
					lastTime = System.currentTimeMillis();
				} else if ((curTime - lastTime) / 1000 > 3) {
					lastTime = System.currentTimeMillis();
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!isCan)
						break;
					isFirst=false;
					isCan = false;
					// 按下操作，记录时间
					long curTimew = System.currentTimeMillis();
					int maxSec=3;
					if (lastDownTime != 0
							&& (curTimew - lastDownTime) / 1000 < maxSec) {
						ToastUtils.showToast(mContext, maxSec+"内不能连续点击！");
						break;
					}
					lastDownTime = curTimew;
					
					if (RECODE_STATE != RECORD_ING) {
						if (view instanceof TextView) {
							((TextView) view).setText("松开    发送");
						}
						if (pressRes > 0) {
							view.setBackgroundResource(pressRes);
						}
						picPath = getAmrPath();
						if(mr==null){
							mr = new AudioRecorder(mContext);
						}
						RECODE_STATE = RECORD_ING;
						// 显示录音动画
						showVoiceDialog();
							// 启动录音
							if (!mr.isStart()){
								mr.start(picPath);
							}
						// 开启线程监听音量大小
						mythread();
					}
					break;
				case MotionEvent.ACTION_UP:
					// 手指离开操作
					if (isCan)
						break;
					isCan = true;
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE = RECODE_ED;
						// 更改状态
						if (view instanceof TextView) {
							((TextView) view).setText("按住    说话");
						}
						if (unPressRes > 0) {
							view.setBackgroundResource(unPressRes);
						}
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
						try {
							if (mr != null&&mr.isStart())
								// 停止录音
								mr.stop();
							voiceValue = 0.0;
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 判断录音时长
						if (recodeTime < MIX_TIME) {
							voiceValue = 0.0;
							ToastUtils.showToast(mContext, "录音时长太短！");
							RECODE_STATE = RECORD_NO;
						} else {
							isType = false;
							if (mRecorderListner != null) {
								mRecorderListner.onRecoder(picPath);
							}
						}
						
					}
					break;
				}
				return true;
			}
		});
	}

	// 录音时显示Dialog
	void showVoiceDialog() {
		if (dialogStyle > 0) {
			dialog = new Dialog(mContext, dialogStyle);
		} else {
			dialog = new Dialog(mContext);
		}
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		LinearLayout layout = new LinearLayout(mContext);
		layout.setPadding(20, 20, 20, 20);
		layout.setGravity(Gravity.CENTER);
		if (dialogBg > 0) {
			layout.setBackgroundResource(dialogBg);
		}
		dialog_img = new ImageView(mContext);
		layout.addView(dialog_img);
		dialog.setContentView(layout);

		dialog.show();
	}

	// 获取文件手机路径
	private String getAmrPath() {
		String Name = new DateFormat().format("yyyyMMdd_hhmmss",
				Calendar.getInstance(Locale.CHINA))
				+ ".mp4";
		String path = "/mnt/sdcart/Audio" + Name;
		try {
			path = SdCardUtils.getFilePath(mContext, "/Audio" + Name);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	// 录音计时线程
	void mythread() {
		recordThread = new Thread(ImgThread);
		recordThread.start();
	}

	// 录音Dialog图片随声音大小切换
	void setDialogImage() {
		if (voiceValue < 10000) {
			for (int i = 0; i < animArr.length / 2; i++) {
				if (voiceValue < (i * (1000 / (animArr.length / 2)))) {
					dialog_img.setImageResource(animArr[i]);
				break;
				}
			}
		} else {
			for (int i = 0; i < animArr.length / 2; i++) {
				if (voiceValue < (i * (3000 / (animArr.length / 2)))) {
					dialog_img.setImageResource(animArr[animArr.length / 2 + i]);
					break;
				}
				
			}
		}
	}

	// 录音线程
	private Runnable ImgThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (RECODE_STATE == RECORD_ING) {
				if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
					imgHandle.sendEmptyMessage(0);
				} else {
					try {
						Thread.sleep(200);
						recodeTime += 0.2;
						if (RECODE_STATE == RECORD_ING) {
							if (mr != null){
								voiceValue = mr.getAmplitude();
							imgHandle.sendEmptyMessage(1);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Handler imgHandle = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					// 录音超过15秒自动停止
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE = RECODE_ED;
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
							if (mr != null)
								mr.stop();
							voiceValue = 0.0;
						if (recodeTime < 1.0) {
							ToastUtils.showToast(mContext, "录音时长太短！");
							if (view instanceof TextView) {
								((TextView) view).setText("按住    说话");
							}
							RECODE_STATE = RECORD_NO;
						} else {
							if (view instanceof TextView) {
								((TextView) view).setText("按住    说话");
							}
						}
					}
					break;
				case 1:
					setDialogImage();
					break;
				default:
					break;
				}
			}
		};
	};
	RecorderListner mRecorderListner;

	public void setRecorderListner(RecorderListner mRecorderListner) {
		this.mRecorderListner = mRecorderListner;
	}

	public interface RecorderListner {
		void onRecoder(String audioPath);
	}
}
