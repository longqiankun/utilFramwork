package com.lqk.framework.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.lqk.framework.encryption.MD5;
import com.lqk.framework.util.SdCardUtils;

/**
 * @ClassName: ImageLoader
 * @Description: 图片加载类
 * @author longqiankun
 * @date 2014-7-7 上午11:36:52
 */
public class ImageLoader {
	private static ImageLoader instance;
	private ExecutorService executorService;
	public static final int ScalSize = 2;
	// 线程池
	private ImageMemoryCache memoryCache; // 内存缓存
	// private ImageFileCache fileCache; //文件缓存
	private Map<String, LoadImageInfo> taskMap; // 存放任务
	private int px;
	private boolean isround = false;
	private boolean allowLoad = true; // 是否允许加载图片
	Context context;

	private ImageLoader(Context context) { // 获取当前系统的CPU数目
		int cpuNums = Runtime.getRuntime().availableProcessors(); // 根据系统资源情况灵活定义线程池大小
		this.executorService = Executors.newFixedThreadPool(cpuNums + 1);
		this.memoryCache = new ImageMemoryCache(context);
		// this.fileCache = new ImageFileCache(context);
		this.taskMap = new HashMap<String, LoadImageInfo>();
		this.context = context;
	}

	/** * 使用单例，保证整个应用中只有一个线程池和一份内存缓存和文件缓存 */
	public static ImageLoader getInstance(Context context) {
		if (instance == null)
			instance = new ImageLoader(context);
		return instance;
	}

	/** * 恢复为初始可加载图片的状态 */
	public void restore() {
		this.allowLoad = true;
	}

	/** * 锁住时不允许加载图片 */
	public void lock() {
		this.allowLoad = false;
	}

	/** * 解锁时加载图片 */
	public void unlock() {
		this.allowLoad = true;
		doTask();
	}
	public void showBitmap(LoadImageInfo mInfo,View view,Bitmap bitmap){
		if(bitmap!=null&&view!=null&&mInfo!=null){
			if(mInfo.roundPx>0||mInfo.subnailWH>0){
				if(mInfo.roundPx>0&&mInfo.subnailWH<1){
					mInfo.subnailWH=200;
				}
				if(bitmap.getWidth()>mInfo.subnailWH||bitmap.getHeight()>mInfo.subnailWH){
				bitmap=ImageUtil.zoomBitmap(bitmap, mInfo.subnailWH, mInfo.subnailWH);
				}
				if(mInfo.roundPx>0){
				bitmap=ImageUtil.getRoundedCornerBitmap(bitmap,  mInfo.roundPx);
				}
			}
			if(mInfo.frameColor>0){
				bitmap=ImageUtil.addFrame(bitmap, mInfo.frameColor);
			}
		if(view instanceof ImageView){
			ImageView imageView=(ImageView) view;
			imageView.setImageBitmap(bitmap);
		}else{
			view.setBackgroundDrawable(ImageUtil.bitmap2Drawable(bitmap));
		}
		if(mInfo.imageCallback!=null){
			mInfo.imageCallback.imageLoaded(bitmap);
		}
		}
	}
	public void addTask(LoadImageInfo mInfo){
		if(mInfo!=null){
			String url=mInfo.url;
			View view=mInfo.view;
		// 先从内存缓存中获取，取到直接加载
		Bitmap bitmap = null;
		SoftReference<Bitmap> sb = memoryCache.getBitmapFromCache(url);
		if (sb != null) {
			bitmap = sb.get();
		} else {
			bitmap = null;
		}
		if (bitmap != null) {
			showBitmap(mInfo, view, bitmap);
			
		} else {
			synchronized (taskMap) {
				/**
				 * * 因为ListView或GridView的原理是用上面移出屏幕的item去填充下面新显示的item, *
				 * 这里的img是item里的内容，所以这里的taskMap保存的始终是当前屏幕内的所有ImageView。
				 * */
				view.setTag(url);
				taskMap.put(Integer.toString(view.hashCode()), mInfo);
			}
			if (allowLoad) {
				doTask();
			}
		}
		}
	}

	/** * 添加任务 */
	public void addTask(String url, ImageView img) {
		addTask(new LoadImageInfo(url, img));
	}

	/** * 添加任务 */
	public void addTask(String url, ImageView img,
			ImageCallback imageCallbackString) {
		LoadImageInfo mImageInfo=new LoadImageInfo();
		mImageInfo.url=url;
		mImageInfo.view=img;
		mImageInfo.imageCallback=imageCallbackString;
		addTask(mImageInfo);
	}

	/** * 添加任务 */
	public void addTask(String url, ImageView img, int subnailWH, int px) {
		addTask(new LoadImageInfo(url, img, subnailWH, px));
	}
	public void addTask(String url, ImageView img, int px) {
		addTask(url, img, 0, px);
	}
	/** * 加载存放任务中的所有图片 */
	private void doTask() {
		synchronized (taskMap) {
			Collection<LoadImageInfo> con = taskMap.values();
			for (LoadImageInfo i : con) {
				if (i != null) {
						loadImage(i);
				}
			}
			taskMap.clear();
		}
	}

	private void loadImage(LoadImageInfo mInfo) {
		this.executorService.submit(new TaskWithResult(
				new TaskHandler(mInfo), mInfo.url));
	}

	/*** 获得一个图片,从三个地方获取,首先是内存缓存,然后是文件缓存,最后从网络获取 ***/
	public Bitmap getBitmap(String url) {
		// 从内存缓存中获取图片
		Bitmap result = null;
		SoftReference<Bitmap> sb = memoryCache.getBitmapFromCache(url);
		if (sb != null) {
			result = sb.get();
		} else {
			result = null;
		}
		if (result == null) {
			String path = "";
			try {
				if(url.startsWith("http")){
					if(!url.contains(".")||(!url.endsWith("png")&&!url.endsWith("jpg"))){
						path = SdCardUtils.getImgPath(context, MD5.encode(url));
					}else{
						path = SdCardUtils.getImgPath(context, url);
					}
				}else{
					path=url;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 文件缓存中获取
			result = ImageUtil.getImage(path);
			if (result == null) {
				// 从网络获取
				result = loadImageFromUrl(context, url, 480, 800);
				if (result != null) {
					ImageUtil.saveImgTOLocal(path, result);
					memoryCache.addBitmapToCache(url,
							new SoftReference<Bitmap>(result));
				}
			} else {
				// 添加到内存缓存
				memoryCache.addBitmapToCache(url, new SoftReference<Bitmap>(
						result));
			}
		} else {
		}
		return result;
	}

	/*** 子线程任务 ***/
	private class TaskWithResult implements Callable<String> {
		private String url;
		private Handler handler;

		public TaskWithResult(Handler handler, String url) {
			this.url = url;

			this.handler = handler;
		}

		@Override
		public String call() throws Exception {

			Message msg = new Message();
			msg.obj = getBitmap(url);
			if (msg.obj != null) {
				handler.sendMessage(msg);
			}
			return url;
		}
	}

	/*** 完成消息 ***/
	private class TaskHandler extends Handler {
	private LoadImageInfo	mInfo;
		public TaskHandler(LoadImageInfo mInfo) {
			this.mInfo = mInfo;
		}

		@Override
		public void handleMessage(Message msg) {
			/*** 查看ImageView需要显示的图片是否被改变 ***/
			View view=mInfo.view;
			if (view.getTag().equals(mInfo.url)) {
				if (msg.obj != null) {
					Bitmap bitmap = (Bitmap) msg.obj;
					showBitmap(mInfo, view, bitmap);
				}
			} else {
			}
		}
	}

	/**
	 * 
	 * @Title: getStreamFromURL
	 * @Description: 将请求的返回流转换的图片
	 * @param @param imageURL
	 * @param @return
	 * @return Bitmap
	 * @throws
	 */
	public static Bitmap getStreamFromURL(String imageURL) {
		InputStream in = null;
		try {
			URL url = new URL(imageURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			in = connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(in);
		if(bitmap==null){
			bitmap=getBitmapFromUrl(imageURL);
		}
		return bitmap;
	}
	/**  
	 * 到Url地址上去下载图片，并回传Bitmap回來  
	 *   
	 * @param imgUrl  
	 * @return  
	 */  
	public  static Bitmap getBitmapFromUrl(String imgUrl) {   
	    URL url;   
	    Bitmap bitmap = null;   
	    try {   
	        url = new URL(imgUrl);   
	        InputStream is = url.openConnection().getInputStream();   
	        BufferedInputStream bis = new BufferedInputStream(is);  // bitmap = BitmapFactory.decodeStream(bis);     注释1                                           
	        byte[] b = getBytes(is);   
	        bitmap = BitmapFactory.decodeByteArray(b,0,b.length);bis.close();   
	    } catch (MalformedURLException e) {   
	        e.printStackTrace();   
	    } catch (IOException e) {   
	        e.printStackTrace();   
	    }   
	    return bitmap;   
	} 
	/**  
	 * 将InputStream对象转换为Byte[]  
	 * @param is  
	 * @return  
	 * @throws IOException  
	 */  
	public static byte[] getBytes(InputStream is) throws IOException{              
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();              
	    byte[] b = new byte[1024];              
	    int len = 0;              
	    while ((len = is.read(b, 0, 1024)) != -1) {               
	        baos.write(b, 0, len);              
	        baos.flush();              
	    }          
	    byte[] bytes = baos.toByteArray();              
	    return bytes;           
	} 
	
	/**
	 * @Title: loadImageFromUrl
	 * @Description: 加载图片
	 * @param context
	 * @param url
	 * @param  width
	 * @param height
	 * @param 
	 * @return Bitmap
	 * @throws
	 */
	
	public Bitmap loadImageFromUrl(Context context, String url, int width,
			int height) {
		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = m.openStream();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Options bitmapFactoryOptions = new BitmapFactory.Options();
		// 下面这个设置是将图片边界不可调节变为可调节
		bitmapFactoryOptions.inJustDecodeBounds = true;
		bitmapFactoryOptions.inSampleSize = 2;
		int outWidth = bitmapFactoryOptions.outWidth;
		int outHeight = bitmapFactoryOptions.outHeight;
		Bitmap bmap = BitmapFactory.decodeStream(i, new Rect(0, 0, 0, 0),
				bitmapFactoryOptions);

		float imagew = width / ScalSize;
		float imageh = height / ScalSize;
		int yRatio = (int) Math.ceil(bitmapFactoryOptions.outHeight / imageh);
		int xRatio = (int) Math.ceil(bitmapFactoryOptions.outWidth / imagew);
		if (yRatio > 1 || xRatio > 1) {
			if (yRatio > xRatio) {
				bitmapFactoryOptions.inSampleSize = yRatio;
			} else {
				bitmapFactoryOptions.inSampleSize = xRatio;
			}
		}
		bitmapFactoryOptions.inJustDecodeBounds = false;
		try {
			m = new URL(url);
			i = m.openStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bmap = BitmapFactory.decodeStream(i, new Rect(0, 0, 0, 0),
				bitmapFactoryOptions);
		if (bmap != null) {

			return bmap;
		}

		return null;
	}
	public class LoadImageInfo{
		public String url;
		public  View view;
		public int roundPx;
		public int frameColor;
		public Context mContext;
		public int screenW;
		public int screenH;
		public int subnailWH=200;
		ImageCallback imageCallback;
		
		public LoadImageInfo() {
			super();
		}
		public LoadImageInfo(String url, View view) {
			super();
			this.url = url;
			this.view = view;
		}
		public LoadImageInfo(String url, View view, int subnailWH, int roundPx) {
			super();
			this.url = url;
			this.view = view;
			this.subnailWH = subnailWH;
			this.roundPx = roundPx;
		}
		public LoadImageInfo(String url, View view, int subnailWH,int roundPx,
				ImageCallback imageCallbackString) {
			super();
			this.url = url;
			this.view = view;
			this.subnailWH = subnailWH;
			this.roundPx = roundPx;
			this.imageCallback = imageCallbackString;
		}
		
	}

	ImageCallback imageCallbackString;

	public interface ImageCallback {
		public void imageLoaded(Bitmap bitmap);
	}
}
