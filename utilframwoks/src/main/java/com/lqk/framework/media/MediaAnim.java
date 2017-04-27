package com.lqk.framework.media;

import java.io.IOException;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * @Title: MediaUtils.java
 * @Package com.dilitech.qiyebao.utils
 * @Description:
 * @author longqiankun
 * @date 2013-7-3 上午10:25:15
 * @version V1.0
 * @Email:qiankun.long@dilitech.com
 */
public class MediaAnim {

	private String lastPath;
	ImageView last_voice;
	private static MediaAnim intance;
	int time = 1;

	public synchronized static MediaAnim getInstance() {
		if (intance == null) {
			intance = new MediaAnim();
		}
		return intance;
	}

	private MediaPlayer mp;
	private AnimationDrawable anim;

	private MediaAnim() {
		super();
		mp = new MediaPlayer();
	}

	// 停止播放
	public void stop() {
		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
			}
		}
	}

	Handler handler2 = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			int value = time % 3;
			if (last_voice != null) {
				time++;
				/*
				 * if(1==value){ last_voice.setImageResource(R.drawable.send3);
				 * }else if(2==value){
				 * last_voice.setImageResource(R.drawable.send1); }else{
				 * last_voice.setImageResource(R.drawable.send2); }
				 */
			}
			handler2.postDelayed(this, 500);
		}
	};

	// 开始播放
	public void start(String path) {
		if (lastPath != null && !lastPath.equals(path)) {
			time = 1;
			// 如果播放就停止
			if (mp != null && mp.isPlaying()) {
				mp.stop();
			}
			handler2.post(runnable);
		} else {
			time = 1;
			// 如果播放就停止，并听500毫秒
			if (mp != null && mp.isPlaying()) {
				mp.stop();
				SystemClock.sleep(500);
				handler2.removeCallbacks(runnable);
			} else {
				handler2.post(runnable);
			}
		}
		lastPath = path;
		if (mp != null) {
			// try{
			// if(mp.isPlaying()){
			// mp.stop();
			// mp.release();
			// }else{
			if (!TextUtils.isEmpty(path)) {
				// MediaPlayer mp = new MediaPlayer();
				try {
					if (!mp.isPlaying()) {
						mp.reset();
						mp.setDataSource(path);
						mp.prepare();
						new Thread() {
							@Override
							public void run() {
								super.run();
								// 播放
								mp.start();
							}
						}.start();
					} else {
						mp.stop();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// }
			// }catch(Exception e){
			// e.printStackTrace();
			// }

			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {

					time = 1;
					SystemClock.sleep(1000);
					handler2.removeCallbacks(runnable);

					if (last_voice != null) {
						// last_voice.setImageResource(R.drawable.send3);
					}
				}
			});
		}
	}

	public void start(String path, final AnimationDrawable anim) {
		this.anim = anim;
		if (mp != null) {
			if (!TextUtils.isEmpty(path)) {
				try {
					if (!mp.isPlaying()) {
						mp.reset();
						mp.setDataSource(path);
						mp.prepare();
						mp.start();
						if (anim != null)
							if (!anim.isRunning()) {
								handler.sendEmptyMessage(1);
							}
					} else {
						if (anim != null) {
							if (anim.isRunning()) {
								handler.sendEmptyMessage(0);
							}
						}
						mp.stop();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					if (anim != null) {
						handler.sendEmptyMessage(0);
					}

				}
			});
		}

	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {

			case 1:
				anim.start();
				break;
			case 0:
				anim.stop();
				break;
			default:
				break;
			}
		};
	};
}
