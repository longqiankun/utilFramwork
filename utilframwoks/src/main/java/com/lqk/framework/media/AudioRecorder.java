package com.lqk.framework.media;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaRecorder;
import android.text.format.DateFormat;

import com.lqk.framework.util.SdCardUtils;
import com.lqk.framework.util.StringUtils;

/**
 * 音频录制
 * @author longqiankun
 *
 */
public class AudioRecorder
{

	 MediaRecorder recorder;
	 String path;

	private boolean isStart=false;
	Context mContext;
	public AudioRecorder(Context mContext)
	{
		this.mContext=mContext;
	}

	public boolean isStart(){
	return isStart;
}
	public void start(){
		start(null);
	}
	public String getAudioPath(){
		return path;
	}
	//开始录制
	@SuppressLint("InlinedApi")
	public void start(String path)
	{
		if(StringUtils.isEmpty(path)){
			path=getAmrPath();
		}
		this.path=path;
		
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//		recorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
		recorder.setOutputFile(path);
		try {
			recorder.prepare();
			recorder.start();
			isStart=true;
		} catch (Exception e) {
		}
		
	}

	//停止录制
	public void stop() 
	{
		
		//判断录制器是否为空
		if (recorder != null) {
			//设置错误监听
			recorder.setOnErrorListener(null);
			recorder.setPreviewDisplay(null);

			try {
				//停止录制
				recorder.stop();
				recorder.reset();
				//释放资源
				recorder.release();
				
			} catch (IllegalStateException e) {
			} catch (RuntimeException e) {
			} catch (Exception e) {
			}
			isStart=false;
		}
		
	}
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
	public double getAmplitude() {		
		if (recorder != null){			
			return  (recorder.getMaxAmplitude());		
			}		
		else			
			return 0;	
		}
}