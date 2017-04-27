/*
 * Copyright (c) 2015 browniesoft - All rights
 * reserved.
 */
package com.lqk.framework.media;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.lqk.framework.image.ImageUtil;
import com.lqk.framework.util.DateUtil;
import com.lqk.framework.util.FileUtils;
import com.lqk.framework.util.SdCardUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @Title: VideoRecorder.java
 * @Package com.browniesoft.mTrace.camera
 * @author longqiankun
 * @email qiankun.long@browniesoft.com
 * @company browniesoft
 * @date 2015-7-29 下午5:23:57
 * @version V1.0
 * @Description: TODO
 */
@SuppressLint("NewApi")
public class PicVideoRecorder implements SurfaceHolder.Callback,
		Camera.PictureCallback {
	// 录制器
	private MediaRecorder recorder;
	// 相机
	private Camera camera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private static PicVideoRecorder instance;
	String videoPath;// 视频路径
	String picPath;// 图片路径
	private static Activity mActivity;
	private OrientationEventListener mOrEventListener; // 设备方向监听器
	private boolean mCurrentOrientation; // 当前设备方向 横屏false,竖屏true

	private boolean recoding = false;

	public boolean isRecoding() {
		return recoding;
	}

	private PicVideoRecorder(Activity activity, SurfaceView surfaceView) {
		mSurfaceView = surfaceView;
		mActivity = activity;
		mCurrentOrientation = mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? true
				: false;
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		recorder = new MediaRecorder();
		startOrientationChangeListener(); // 启动设备方向监听器

	}

	public synchronized static PicVideoRecorder getInstance(Activity mActivity) {
		// 获取实例
		return getInstance(mActivity, null);
	}

	public synchronized static PicVideoRecorder getInstance(Activity activity,
			SurfaceView surfaceView) {
		// 接收视频视图

		// if (instance == null) {
		instance = new PicVideoRecorder(activity, surfaceView);
		// }
		return instance;
	}

	@SuppressLint("NewApi")
	public void openCamera() {
		// 开启相机
		if (camera == null) {
			try {
				camera = Camera.open();
			} catch (RuntimeException e) {
				camera = Camera.open(Camera.getNumberOfCameras() - 1);
			}
			setDisplayOrientation();
			if (mSurfaceHolder != null) {
				try {
					camera.setPreviewDisplay(mSurfaceHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void setDisplayOrientation() {
		if (mActivity == null)
			return;
		int rotation = mActivity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 0;
			break;
		case Surface.ROTATION_90:
			degree = 90;
			break;
		case Surface.ROTATION_180:
			degree = 180;
			break;
		case Surface.ROTATION_270:
			degree = 270;
			break;
		}
		int result;
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(0, info);
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degree) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degree + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		// 这里的myCamera就是已经初始化的Camera对象
	}

	public void takePhoto() {
		takePhoto(null);
	}

	public void takePhoto(String mPicPath) {
		if (TextUtils.isEmpty(mPicPath)) {
			String fileName = DateUtil.formatDatetime(new Date(),
					"yyyyMMddHHmmss") + ".png";
			if (mActivity == null) {
				picPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/" + fileName;
			} else {
				try {
					picPath = SdCardUtils.getImgPath(mActivity, fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			picPath=mPicPath;
		}
		File file = new File(picPath);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		camera.takePicture(null, null, this);
	}

	public void startPreview() {
		camera.startPreview();
	}

	/**
	 * @author longqiankun
	 * @description : 开始录制
	 */
	public String start() {
		// 设置默认文件存储路径
		String dir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/video";
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdir();
		}
		videoPath = dir + "/VIDEO" + System.currentTimeMillis() + ".mp4";

		// 开始录制
		start(videoPath);

		return videoPath;
	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 获取文件路径，必须先录制
	 * @return
	 */
	public String getFilePath() {
		return videoPath;
	}

	/**
	 * @author longqiankun
	 * @description : 开始录制视频
	 * @param camera
	 * @param recorder
	 * @param surface
	 * @param fileName
	 */
	@SuppressLint("InlinedApi")
	public void start(String FilePath) {
		if (!recoding) {
			recoding = true;
			// 接收文件全路径
			this.videoPath = FilePath;

			if (recorder == null) {
				recorder = new MediaRecorder();
			}

			try {
				File myRecAudioFile = new File(FilePath);
				if (!myRecAudioFile.exists()) {
					myRecAudioFile.createNewFile();
				}

				// 关闭预览并释放资源
				stopPreviewDisplay();

				if (camera != null) {
					camera.unlock();
					recorder.setCamera(camera);
				}
				if(mCurrentOrientation){
					recorder.setOrientationHint(90);
				}
				
				if (mSurfaceView != null) {
					recorder.setPreviewDisplay(mSurfaceHolder.getSurface());
				}

				recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				// 设置录音源为麦克风
				recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				// 设置输出格式为mp4
				recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				CamcorderProfile profile = CamcorderProfile
						.get(CamcorderProfile.QUALITY_HIGH);

				if (profile != null) {
					recorder.setVideoSize(profile.videoFrameWidth,
							profile.videoFrameHeight);
					recorder.setVideoFrameRate(profile.videoFrameRate);
				} else {
					recorder.setVideoSize(176, 144);
					recorder.setVideoFrameRate(20);
				}
				// 在这里我提高了帧频率
				recorder.setVideoEncodingBitRate(2 * 1024 * 1024);

				// 设置视频编码
				recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				// 设置音频编码
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

				recorder.setOutputFile(myRecAudioFile.getAbsolutePath());

				try {
					recorder.prepare();
					recorder.start();
				} catch (Exception e) {
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * @author longqiankun
	 * @description : 停止录制
	 * @param recorder
	 */
	public void stopRecorder() {
		// 判断录制器是否为空
		if (recorder != null) {
			if (recoding) {
				recoding = false;
				if (mPicVideoListener != null) {
					mPicVideoListener.onRecoderVideo(videoPath);
				}
			}
			// 设置错误监听
			recorder.setOnErrorListener(null);
			recorder.setPreviewDisplay(null);

			try {
				// 停止录制
				recorder.stop();
				recorder.reset();
				// 释放资源
				recorder.release();
				recorder = null;
			} catch (IllegalStateException e) {
			} catch (RuntimeException e) {
			} catch (Exception e) {
			}
		}

		openCamera();
		// 开启预览
		startPreview();
	}

	public boolean isPortrait() {
		return mCurrentOrientation;
	}

	/**
	 * @author longqiankun
	 * @description : 停止预览
	 */
	public void stopPreviewDisplay() {
		// 判断相机是否为空
		if (camera != null) {
			// 停止预览
			camera.setPreviewCallback(null);
			camera.stopPreview();
			// 是否相机
			camera.release();
			camera = null;
		}
	}

	private final void startOrientationChangeListener() {
		mOrEventListener = new OrientationEventListener(mActivity) {
			@Override
			public void onOrientationChanged(int rotation) {
				if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)
						|| ((rotation >= 135) && (rotation <= 225))) {// portrait
					mCurrentOrientation = true;
				} else if (((rotation > 45) && (rotation < 135))
						|| ((rotation > 225) && (rotation < 315))) {// landscape
					mCurrentOrientation = false;
				}
				if (mPicVideoListener != null) {
					mPicVideoListener.onOrientationChange(rotation,
							mCurrentOrientation);
				}
			}
		};
		mOrEventListener.enable();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if (data != null) {
			Bitmap bitmap = ImageUtil.getImage(data, 480, 800, false);
			if (bitmap != null) {
				if (mCurrentOrientation) {
					Matrix matrix = new Matrix();
					matrix.setRotate(90);
					bitmap = Bitmap
							.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
									bitmap.getHeight(), matrix, true);
				}
				ImageUtil.saveImgTOLocal(picPath, bitmap);
				if (mPicVideoListener != null) {
					mPicVideoListener.onTakePhone(picPath, bitmap);
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.mSurfaceHolder = holder;
		// 开启相机
		openCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.mSurfaceHolder = holder;
		startPreview();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPreviewDisplay();
	}

	private PicVideoListener mPicVideoListener;

	public void setPicVideoListener(PicVideoListener mPicVideoListener) {
		this.mPicVideoListener = mPicVideoListener;
	}

	public interface PicVideoListener {
		void onTakePhone(String picPath, Bitmap bitmap);

		void onRecoderVideo(String videoPath);

		void onOrientationChange(int rotation, boolean isPortrait);
	}
}
