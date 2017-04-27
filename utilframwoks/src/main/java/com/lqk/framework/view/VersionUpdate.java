package com.lqk.framework.view;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;

import com.lqk.framework.util.Handler_System;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class VersionUpdate {
	public static final String DOWNLOAD_FOLDER_NAME = "download";
	// 应用程序Context
	private Context mContext;
	// 提示消息
	private String updateMsg = "有最新的软件包，请下载！";
	// 下载安装包的网络路径
	private String apkUrl;
	private Dialog noticeDialog;// 提示有软件更新的对话框
	private Dialog downloadDialog;// 下载对话框
	// 进度条与通知UI刷新的handler和msg常量
	private ProgressBar mProgress;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;
	private static final int DOWN_FAIL = 3;
	private int progress;// 当前进度
	private boolean interceptFlag = false;// 用户取消下载
	private boolean isShowProgress = true;
	private DownloadManager downloadManager;
	private SharedPreferences prefs;
	private static final String DL_ID = "downloadId";
	/** Called when the activity is first created. */
	DownloadReceiver receiver;
	String fileName;
	long id;

	// 通知处理刷新界面的handler

	public VersionUpdate(Context context) {
		this.mContext = context;
		downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	// 显示更新程序对话框，供主程序调用
	public void checkUpdateInfo(boolean isShowNotNew, boolean isNew,
			boolean isShowProgress, String apkUrl) {
		this.apkUrl = apkUrl;
		this.isShowProgress = isShowProgress;
		fileName = apkUrl.substring(apkUrl.lastIndexOf("/"));
		if (isNew) {
			showNoticeDialog();
		} else {
			if (isShowNotNew) {
				notNewVersionShow();
			}
		}
	}

	private void notNewVersionShow() {
		String verCode = Handler_System.getAppVersionCode();
		String verName = Handler_System.getAppVersionNumber();
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本:");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(",/n已是最新版,无需更新!");
		Dialog dialog = new AlertDialog.Builder(mContext).setTitle("软件更新")
				.setMessage(sb.toString())// 设置内容
				.setPositiveButton("确定",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();// 创建
		// 显示对话框
		dialog.show();
	}

	private void showNoticeDialog() {
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				mContext);// Builder，可以通过此builder设置改变AleartDialog的默认的主题样式及属性相关信息
		builder.setTitle("软件版本更新");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("下载", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();// 当取消对话框后进行操作一定的代码？取消对话框
				if (isShowProgress) {
					showDownloadDialog();
				}
				downloadApk();
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	protected void showDownloadDialog() {
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				mContext);
		builder.setTitle("软件版本更新");
		LinearLayout lay = new LinearLayout(mContext);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 10);
		mProgress = new ProgressBar(mContext);
		Handler_System.setFieldValue(mProgress, "mOnlyIndeterminate",
				new Boolean(false));
		mProgress.setIndeterminate(false);
		mProgress.setMax(100);
		mProgress.setProgressDrawable(mContext.getResources().getDrawable(
				android.R.drawable.progress_horizontal));
		mProgress.setIndeterminateDrawable(mContext.getResources().getDrawable(
				android.R.drawable.progress_indeterminate_horizontal));
		lay.addView(mProgress, layoutParams);
		builder.setView(lay);// 设置对话框的内容为一个View
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				downloadManager.remove(prefs.getLong(DL_ID, 0));
				prefs.edit().clear().commit();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();

	}

	private void downloadApk() {
		if (!prefs.contains(DL_ID)) {
			receiver = new DownloadReceiver();
			mContext.registerReceiver(receiver, new IntentFilter(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			// 开始下载
			Uri resource = Uri.parse(encodeGB(apkUrl));
			DownloadManager.Request request = new DownloadManager.Request(
					resource);
			request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
					| Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			// 设置文件类型
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap
					.getMimeTypeFromExtension(MimeTypeMap
							.getFileExtensionFromUrl(apkUrl));
			request.setMimeType(mimeString);
			// 在通知栏中显示
			request.setShowRunningNotification(true);
			request.setVisibleInDownloadsUi(true);
			// 设置下载后文件存放的位置
			File folder = Environment
					.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
			if (!folder.exists() || !folder.isDirectory()) {
				folder.mkdirs();
			}
			request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME,
					fileName);
			request.setTitle(fileName);
			long id = downloadManager.enqueue(request);
			// 保存id
			prefs.edit().putLong(DL_ID, id).commit();

			handler.post(runnable);
		} else {
			// 下载已经开始，检查状态
			queryDownloadStatus();
		}

	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (prefs.contains(DL_ID)) {
				downloadApk();
				handler.postDelayed(this, 500);
			} else {
				handler.removeCallbacks(this);
			}
		}
	};

	/**
	 * 如果服务器不支持中文路径的情况下需要转换url的编码。
	 * 
	 * @param string
	 * @return
	 */
	public String encodeGB(String string) {
		// 转换中文编码
		String split[] = string.split("/");
		for (int i = 1; i < split.length; i++) {
			try {
				split[i] = URLEncoder.encode(split[i], "GB2312");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			split[0] = split[0] + "/" + split[i];
		}
		split[0] = split[0].replaceAll("\\+", "%20");// 处理空格
		return split[0];
	}

	class DownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			// 这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
			queryDownloadStatus();
		}

	}

	private void queryDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(prefs.getLong(DL_ID, 0));
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			int status = c.getInt(c
					.getColumnIndex(DownloadManager.COLUMN_STATUS));

			int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);
			int titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
			int fileSizeIdx = c
					.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
			int bytesDLIdx = c
					.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
			String title = c.getString(titleIdx);
			int fileSize = c.getInt(fileSizeIdx);
			int bytesDL = c.getInt(bytesDLIdx);
			double a = (((float) bytesDL) / fileSize) * 100;
			// 对进度进行四舍五入操作
			progress = Integer.parseInt(new java.text.DecimalFormat("0")
					.format(a));
			if (mProgress != null)
				mProgress.setProgress(progress);
			// Translate the pause reason to friendly text.
			int reason = c.getInt(reasonIdx);
			switch (status) {
			case DownloadManager.STATUS_PAUSED:
				Log.v("down", "STATUS_PAUSED");
			case DownloadManager.STATUS_PENDING:
				Log.v("down", "STATUS_PENDING");
			case DownloadManager.STATUS_RUNNING:
				// 正在下载，不做任何事情
				Log.v("down", "STATUS_RUNNING");

				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				// 完成
				String saveFileName = new StringBuilder(Environment
						.getExternalStorageDirectory().getAbsolutePath())
						.append(File.separator).append(DOWNLOAD_FOLDER_NAME)
						.append(File.separator).append(fileName).toString();
				Handler_System.installApk(saveFileName);
				if (downloadDialog != null) {
					downloadDialog.dismiss();
				}
				if (receiver != null) {
					mContext.unregisterReceiver(receiver);
					receiver = null;
				}
				downloadManager.remove(prefs.getLong(DL_ID, 0));
				prefs.edit().clear().commit();
				break;
			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				Log.v("down", "STATUS_FAILED");
				downloadManager.remove(prefs.getLong(DL_ID, 0));
				prefs.edit().clear().commit();
				if (downloadDialog != null) {
					downloadDialog.dismiss();
				}
				if (receiver != null) {
					mContext.unregisterReceiver(receiver);
					receiver = null;
				}
				break;
			}
		}
	}
}
