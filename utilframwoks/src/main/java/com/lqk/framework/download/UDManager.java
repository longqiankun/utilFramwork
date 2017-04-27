package com.lqk.framework.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.lqk.framework.image.ImageUtil;
import com.lqk.framework.util.CoverByteUtils;
import com.lqk.framework.util.Logger;
import com.lqk.framework.util.PreferencesUtils;
import com.lqk.framework.util.SdCardUtils;
import com.lqk.framework.util.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 * 
* @ClassName: UploadDownloadManager
* @Description: 文件上传、下载类
* @author longqiankun
* @date 2014-7-7 下午12:11:34
*
 */
public class UDManager {
	/**
	 * @description 用于从网络下载文件
	 * @param dir 存放的文件目录
	 * @param urlPath 请求网络的URL地址
	 * @param mPd 下载文件的进度条的显示
	 * @param context 上下文
	 * @param isInnerStroger 是否存放在应用的内部存储
	 * @return
	 * @throws Exception
	 */
	public static final String TAG="UploadDownloadManager";
	

	private static final int TCOUNT = 10;
	
	private CountDownLatch latch = new CountDownLatch(TCOUNT);

	private long completeLength = 0;
	
	private long fileLength;

	private static ProgressListener mListener;

	public static void setProgressListener(ProgressListener listener) {
		mListener = listener;
	}

	public interface ProgressListener {
		void onProgress(String url, long progress, long max);
	}

	public static boolean downloadFile(String dir, String fileName, String urlPath)
			throws NameNotFoundException, IOException {
		if ((dir.charAt(dir.length() - 1)) == '/') {
			dir = dir + fileName;
		} else {
			dir = dir + "/" + fileName;
		}
		return downloadFile(dir, urlPath, null, false);
	}

	public static boolean downloadFile(String filePath, String urlPath)
			throws NameNotFoundException, IOException {
		return downloadFile(filePath, urlPath, null, false);
	}
	/**
	 * 
	* @Title: downloadFile
	* @Description: 下载文件
	* @param @param dir 保存目录
	* @param @param urlPath 文件地址
	* @param @param mPd 进度
	* @param @param context 上下文
	* @param @param isInnerStroger 是否保存到内部
	* @param @return
	* @param @throws NameNotFoundException
	* @param @throws IOException
	* @return boolean
	* @throws
	 */
	public static boolean downloadFile(String filePath, String urlPath,
			Context context, boolean isInnerStroger) throws NameNotFoundException, IOException {
		String cookie="";
		if(null!=context){
			 cookie=PreferencesUtils.getString(context, "Cookie");
		}
	
		URL url = new URL(urlPath);
		boolean downloadStatus = false;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Cookie", cookie);
		if(conn.getResponseCode() == 200){
			int max = conn.getContentLength();
            InputStream is = conn.getInputStream();	
            FileOutputStream fos=null;
            if (isInnerStroger) {
				if (context != null) {
					fos = context.openFileOutput(getFileName(filePath),
							Context.MODE_PRIVATE);
				}
			} else {
				if (SdCardUtils.ExistSDCard()) {
					File file = new File(filePath);
					if (!file.exists()) {
						file.createNewFile();
					}
					fos = new FileOutputStream(file);
				}
			}
            
            byte[] buffer = new byte[1024];
            int len = 0;
            int count = 0;
            while((len = is.read(buffer)) != -1){
            	if(fos!=null){
            	fos.write(buffer, 0, len);
            	}
            	count = count + len;
            	if (mListener != null) {
					mListener.onProgress(urlPath, count, max);
				}
            }
            is.close();
            if (fos != null) {
				fos.close();
			}
			downloadStatus = true;
		}
		return downloadStatus;
	}
	/**
	 * 
	* @Title: downloadFile
	* @Description:下载文件
	* @param @param dir 保存目录
	* @param @param fileName 文件名
	* @param @param context 上下文
	* @param @param urlPath 文件地址
	* @param @return
	* @return boolean 是否下载成功
	* @throws
	 */
	public boolean downloadFile(String dir,String fileName,Context context,String urlPath){
		//判断文件是否在本地存在，如果存在就不下载
		 File file = new File(dir, fileName);
	 	   if(file.exists()&&file.length()>0){
	 		 return false;
	 	   }
		SharedPreferences sp = context.getSharedPreferences("downloadfail.xml", Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		try {
			URL url = new URL(urlPath);
			Logger.getLogger(this).i(urlPath);
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setDoInput(true);
			openConnection.setDoOutput(true);
			openConnection.setRequestMethod("POST");
			System.out.println("=========="+openConnection.getResponseCode());
			if(openConnection.getResponseCode()!=200){
				return false;
				}
			    InputStream is = url.openStream();	
			    
			    FileOutputStream fos=null;
			    if(SdCardUtils.ExistSDCard()){
			 	   if(!file.exists()){
			 		  file.createNewFile();
			 	   }
			 		System.out.println("====www");
			         fos = new FileOutputStream(file);  
			    }
			    byte[] buffer = new byte[1024];
			    int len = 0;
			    while((len = is.read(buffer)) != -1){
			    	if(fos!=null){
			    		System.out.println("====ww"+buffer.length);
			    	fos.write(buffer, 0, len);
			    	fos.flush();
			    	}
			    
			    }
			    is.close();
			    fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * 
	* @Title: downloadFile
	* @Description: 下载文件
	* @param @param dir 保存目录
	* @param @param context
	* @param @param urlPath 文件地址
	* @param @return
	* @return boolean
	* @throws
	 */
	public boolean downloadFile(String dir,Context context,String urlPath){
		//判断文件是否在本地存在，如果存在就不下载
		 File file = new File(dir, getFileName(urlPath));
	 	   if(file.exists()&&file.length()>0){
	 		 return false;
	 	   }
		SharedPreferences sp = context.getSharedPreferences("downloadfail.xml", Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		try {
			URL url = new URL(urlPath);
			Logger.getLogger(this).i( urlPath);
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
		
			if(openConnection.getResponseCode()!=200){
				edit.putString(urlPath, "");
				return false;
				}
			    InputStream is = url.openStream();	
			    if(is==null){
			    	edit.putString(urlPath, "");
			    	return false;
			    	}
			    
			    FileOutputStream fos=null;
			    if(SdCardUtils.ExistSDCard()){
			 	   if(!file.exists()){
			 		  file.createNewFile();
			 	   }
			         fos = new FileOutputStream(file);  
			    }
			    
			    byte[] buffer = new byte[1024];
			    int len = 0;
			    while((len = is.read(buffer)) != -1){
			    	if(fos!=null){
			    	fos.write(buffer, 0, len);
			    	fos.flush();
			    	}
			    
			    }
			    if(sp.contains(urlPath)){
			    	edit.remove(urlPath);
			    }
			    edit.commit();
			    is.close();
			    fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * @description 上传文件到服务器
	 * @param uploadUrl 上传到服务器的URL地址
	 * @param srcPath 上传的文件目录
	 * @return
	 */
	  public static String uploadFile(String uploadUrl,String srcPath)
	  {
	    String end = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "******";
	    try
	    {
	      URL url = new URL(uploadUrl);
	      HttpURLConnection httpURLConnection = (HttpURLConnection) url
	          .openConnection();
	      httpURLConnection.setDoInput(true);
	      httpURLConnection.setDoOutput(true);
	      httpURLConnection.setUseCaches(false);
	      httpURLConnection.setRequestMethod("POST");
	      httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
	      httpURLConnection.setRequestProperty("Charset", "UTF-8");
	      httpURLConnection.setRequestProperty("Content-Type",
	          "multipart/form-data;boundary=" + boundary);

	      DataOutputStream dos = new DataOutputStream(httpURLConnection
	          .getOutputStream());
	      dos.writeBytes(twoHyphens + boundary + end);
	      dos
	          .writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
	              + srcPath.substring(srcPath.lastIndexOf("/") + 1)
	              + "\"" + end);
	      dos.writeBytes(end);

	      FileInputStream fis = new FileInputStream(srcPath);
	      byte[] buffer = new byte[8192]; // 8k
	      int count = 0;
	      while ((count = fis.read(buffer)) != -1)
	      {
	        dos.write(buffer, 0, count);
	        dos.flush();
	      }
	      fis.close();
	      dos.writeBytes(end);
	      dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
	      dos.flush();
	      InputStream is = httpURLConnection.getInputStream();
	      InputStreamReader isr = new InputStreamReader(is, "utf-8");
	      BufferedReader br = new BufferedReader(isr);
	      String result = br.readLine();
	      dos.close();
	      is.close();
	      if(result!=null){
	      return result;
	      }
	    } catch (Exception e)
	    {
	      e.printStackTrace();
	      Logger.getLogger(new UDManager()).i(e.getMessage());
	    }
	    return "";
	  }
/**
 * 
* @Title: downloadCacheImage
* @Description: 下载图片
* @param @param dir 保存目录
* @param @param path 图片地址
* @param @return 
* @param @throws Exception
* @return boolean
* @throws
 */
	public static boolean downloadCacheImage(String dir,String path) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		if(conn.getResponseCode() == 200){
            InputStream is = conn.getInputStream();	
            File file = new File(dir,getFileName(path));
            if(!file.exists()){
            	file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = is.read(buffer)) != -1){
            	fos.write(buffer, 0, len);
            }
            is.close();
            fos.close();
            return true;
		}
		return false;
	}
	/**
	 * 
	* @Title: downloadImage
	* @Description: 下载图片，获取图片流
	* @param @param path 图片路径
	* @param @return
	* @param @throws Exception
	* @return InputStream
	* @throws
	 */
	public static InputStream downloadImage(String path) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		if(conn.getResponseCode() == 200){
            InputStream is = conn.getInputStream();	
            return is;
		}
		return null;
	}
/**
 * 
* @Title: getFileName
* @Description: 获取文件名
* @param @param path
* @param @return
* @return String
* @throws
 */
	public static String getFileName(String path){
		String fileName="";
		if(path.contains("/")){
			fileName=path.substring(path.lastIndexOf("/") + 1);
		}else if(path.contains("\\")){
			fileName=path.substring(path.lastIndexOf("\\") + 1);
		}
		return fileName;
	}
	/**
     * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
     * 
     * @param actionUrl 访问的服务器URL
     * @param params 普通参数
     * @param files 文件参数
     * @return
     * @throws IOException
     */
    public static String uploadMutilPartFile(String actionUrl, Map<String, String> params, Map<String, File> files) throws IOException
    {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(5 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
        Logger.getLogger(TAG).i("url="+actionUrl);
        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        if(params!=null){
        for (Map.Entry<String, String> entry : params.entrySet())
        {
        	  Logger.getLogger(TAG).i("key="+entry.getKey()+":"+"value="+entry.getValue());
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }
        }
        StringBuilder sb2 = new StringBuilder();
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
//        outStream.write(sb.toString().getBytes());
        outStream.writeChars(sb.toString());
        outStream.flush();
        InputStream in = null;
        // 发送文件数据
        if (files != null)
        {
        	long count=0;
			long max=0;
			for (Map.Entry<String, File> file : files.entrySet()) {
				max=max+file.getValue().length();
			}
            for (Map.Entry<String, File> file : files.entrySet())
            {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                // name是post中传参的键 filename是文件的名称
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1)
                {
                    outStream.write(buffer, 0, len);
                	count = count + len;
					if (mListener != null) {
						mListener.onProgress(actionUrl, count, max);
					}
                }
                is.close();
                outStream.write(LINEND.getBytes());
            }
//            // 请求结束标志
//            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
//            outStream.write(end_data);
//            outStream.flush();
        }
        	  // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();
            Logger.getLogger(TAG).i("返回状态嘛"+res);
            System.out.println("wwws"+conn.getContentLength());
            if (res == 200)
            {
                in = conn.getInputStream();
                int ch;
                while ((ch = in.read()) != -1)
                {
                    sb2.append((char) ch);
                }
                outStream.close();
                conn.disconnect();
                String result=new String(sb2.toString().getBytes("iso-8859-1"), "utf-8");
                return result;
            }else{
            	 outStream.close();
                 conn.disconnect();
                 return "";
            }
    }
/**
 * 
* @Title: uploadMutilPartStream
* @Description: 上传字节数组
* @param @param context
* @param @param actionUrl
* @param @param params
* @param @param files
* @param @return
* @param @throws Exception
* @return String
* @throws
 */
    public static String uploadMutilPartStream(Context context,String actionUrl, Map<String, String> params, Map<String, byte[]> files)
            throws Exception
    {
    	  Logger.getLogger(TAG).i("url="+actionUrl);
        StringBuilder sb2 = null;
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        String cookie=PreferencesUtils.getString(context, "Cookie");
        conn.setReadTimeout(6 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Cookie", cookie);
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
            Logger.getLogger(TAG).i("key="+entry.getKey()+"---"+"value="+entry.getValue());
        }
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        outStream.write(sb.toString().getBytes());
        InputStream in = null;
        // 发送文件数据
        if (files != null)
        {
            for (Map.Entry<String, byte[]> file : files.entrySet())
            {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"pic\"; filename=\"" + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());

                // InputStream is = new FileInputStream(file.getValue());
                // byte[] buffer = new byte[1024];
                // int len = 0;
                // while ((len = is.read(buffer)) != -1)
                // {
                // outStream.write(buffer, 0, len);
                // }
                // is.close();
                outStream.write(file.getValue());

                outStream.write(LINEND.getBytes());
            }

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200)
            {
                in = conn.getInputStream();
                int ch;
                sb2 = new StringBuilder();
                while ((ch = in.read()) != -1)
                {
                    sb2.append((char) ch);
                }
            }
            outStream.close();
            conn.disconnect();
            // 解析服务器返回来的数据
            String result=new String(sb2.toString().getBytes("iso-8859-1"), "utf-8");
            return result;
        }
        else
        {
            return "Update icon Fail";
        }
        // return in.toString();
    }
    /**
     * @description 上传多个文件
     * @param params 表单参数
     * @param files 文件
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String uploadMutilPartFileHttpClient(String actionUrl,Map<String, String> params, Map<String, File> files) throws ClientProtocolException, IOException{
    	//定义HttpClient对象
   		/*HttpClient client = new DefaultHttpClient();
   		//获得HttpPost对象
   		HttpPost post = new HttpPost(actionUrl);
   		post.addHeader("charset", HTTP.UTF_8);  
   		HttpConnectionParams.setConnectionTimeout(post.getParams(), 60000);
   		//实例化
   		MultipartEntity me = new MultipartEntity();
   		if(params!=null){
   	 for (Map.Entry<String, String> entry : params.entrySet()){
   		 String key=entry.getKey();
   		 String value=entry.getValue();
   	  Logger.getLogger(TAG).i("key="+key+"___"+"value="+value);
   		 if(!TextUtils.isEmpty(value)){
   			me.addPart(key,new StringBody(value));
   		 }else{
   			me.addPart(key,new StringBody(""));
   		 }
   		
   	 }
   		}
   		if(files!=null){
	 for (Map.Entry<String, File> entry : files.entrySet()){
		 String key=entry.getKey();
		  Logger.getLogger(TAG).i("key_fileName="+key);
		 File f=entry.getValue();
		 if(f!=null){
			 if(key.startsWith("image")){
				 long stime=System.currentTimeMillis();
			Bitmap bitmap=ImageUtil.getImage(f.getAbsolutePath(), 320, 480, false);
			long etime=System.currentTimeMillis();
			*//*Matrix matrix = new Matrix();
			// rotate the Bitmap 
			matrix.postRotate(90); 
			// recreate the new Bitmap 
			 bitmap = Bitmap.createBitmap(bitmap, 0, 0, 
			bitmap.getHeight(), bitmap.getWidth(), matrix, true); 
			 ImageFileCache.saveImgTOLocal("/rotateiamge.png", bitmap);*//*
			me.addPart(key, new InputStreamBody(CoverByteUtils.BytesToInStream(CoverByteUtils.Bitmap2Bytes(bitmap)), "**//*//*",key));
		//}else{
//			me.addPart(key, new InputStreamBody(new FileInputStream(f), "**//*//*",key));
//		}
//
//		 }
//	 }
//   		}
//   			post.setEntity(me);
//   			//获得响应消息
//   			HttpResponse resp;
//   				resp = client.execute(post);
//   				if(resp.getStatusLine().getStatusCode()==200){
//   					String result=EntityUtils.toString(resp.getEntity());
//   					return result;
//   				}else{
//   					return "";
//   				}*/
		return "";
    }
    /**
     * @description 上传多个文件
     * @param params 表单参数
     * @param files 文件
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String uploadMutilPartFileHttpClientBitmap(String actionUrl,Map<String, String> params, Map<String, Bitmap> files) throws ClientProtocolException, IOException{
    	//定义HttpClient对象
//   		HttpClient client = new DefaultHttpClient();
//   		//获得HttpPost对象
//   		HttpPost post = new HttpPost(actionUrl);
//   		post.addHeader("charset", HTTP.UTF_8);
//   		HttpConnectionParams.setConnectionTimeout(post.getParams(), 60000);
//   		//实例化
//   		MultipartEntity me = new MultipartEntity();
//   		if(params!=null){
//   	 for (Map.Entry<String, String> entry : params.entrySet()){
//   		 String key=entry.getKey();
//   		 String value=entry.getValue();
//   	  Logger.getLogger(TAG).i("key="+key+"___"+"value="+value);
//   		 if(!TextUtils.isEmpty(value)){
//   			me.addPart(key,new StringBody(value));
//   		 }else{
//   			me.addPart(key,new StringBody(""));
//   		 }
//
//   	 }
//   		}
//   		if(files!=null){
//	 for (Map.Entry<String, Bitmap> entry : files.entrySet()){
//		 String key=entry.getKey();
//		  Logger.getLogger(TAG).i("key_fileName="+key);
//		 Bitmap f=entry.getValue();
//		 if(f!=null){
//			 me.addPart(key, new InputStreamBody(CoverByteUtils.BytesToInStream(CoverByteUtils.Bitmap2Bytes(f)), "image/*",key));
//		 }
//
//	 }
//   		}
//   			post.setEntity(me);
//   			//获得响应消息
//   			HttpResponse resp;
//   				resp = client.execute(post);
//   				if(resp.getStatusLine().getStatusCode()==200){
//   					String result=EntityUtils.toString(resp.getEntity());
//   				  Logger.getLogger(TAG).i("resut="+result);
//   					return result;
//   				}else{
//   					return "";
//   				}
//
   		
       	return "";
   	
    }
    
/**
 * 
 * 描述:多线程下载文件
 * @param path 本地路径
 * @param address 文件的网络地址
 * @throws Exception
 */
	public void moreThreaddownload(String path,String address) throws Exception{
		ExecutorService service = Executors.newFixedThreadPool(TCOUNT);
		URL url = new URL(address);
		URLConnection cn = url.openConnection();
//		cn.setRequestProperty("Referer", "http://www.test.com");
		fileLength = cn.getContentLength();
		long packageLength = fileLength/TCOUNT;
		long leftLength = fileLength%TCOUNT;
		RandomAccessFile file = new RandomAccessFile(path,"rw");
		//计算每个线程请求文件的开始和结束位置
		long pos = 0;
		long endPos = pos + packageLength;
		for(int i=0; i<TCOUNT; i++){
			if(leftLength >0){
				endPos ++;
				leftLength--;
			}
			service.execute(new DownLoadThread(url, file, pos, endPos));
			pos = endPos;
			endPos = pos + packageLength;
		}
		System.out.println("waiting........................................");
		long begin = System.currentTimeMillis();
		latch.await();
		file.close();
		System.out.println("end........................................");
		System.out.println(System.currentTimeMillis() - begin + "ms");
		service.shutdown();
	}
	
	class DownLoadThread implements Runnable{
		
		private URL url;
		private RandomAccessFile file;
		private long from;
		private long end;
		
		DownLoadThread(URL url, RandomAccessFile file, long from, long end){
			this.url = url;
			this.file = file;
			this.from = from;
			this.end = end;
		}
		public void run() {
			long pos = from;
			byte[] buf = new byte[512];
			try {
				HttpURLConnection cn = (HttpURLConnection) url.openConnection();
				cn.setRequestProperty("Range", "bytes=" + from + "-" + end);
				if(cn.getResponseCode() != 200 && cn.getResponseCode()!=206){
					run();
					return;
				}
				BufferedInputStream bis = new BufferedInputStream(cn.getInputStream());
				int len ;
				while((len = bis.read(buf)) != -1){
//					synchronized(file){
						file.seek(pos);
						file.write(buf, 0, len);
//					}
					pos += len;
					completeLength +=len;
					System.out.println("threadName: " + Thread.currentThread().getName() 
							+ "persent: " + completeLength * 100 /fileLength + "%");
				}
				cn.disconnect();
				latch.countDown();
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		}
	}  
	/**
	 * 
	* @Title: getByteFromFile
	* @Description: 获取文件的字节数组
	* @param @param pathStr
	* @param @return
	* @param @throws Exception
	* @return byte[]
	* @throws
	 */
    public static byte[] getByteFromFile(String pathStr) throws Exception{
    	InputStream is = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            is = new FileInputStream(pathStr);// pathStr 文件路径
            byte[] b = new byte[1024];
            int n;
            while ((n = is.read(b)) != -1) {
                out.write(b, 0, n);
            }// end while
        } catch (Exception e) {
            throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }// end try
            }// end if
        }// end try
        return out.toByteArray();
    }
    /**
     * 
    * @Title: getByteFromFile
    * @Description: 获取文件字节数组
    * @param @param file
    * @param @return
    * @param @throws Exception
    * @return byte[]
    * @throws
     */
    public static byte[] getByteFromFile(File file) throws Exception{
    	InputStream is = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            is = new FileInputStream(file);// pathStr 文件路径
            byte[] b = new byte[1024];
            int n;
            while ((n = is.read(b)) != -1) {
                out.write(b, 0, n);
            }// end while
        } catch (Exception e) {
            throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }// end try
            }// end if
        }// end try
        return out.toByteArray();

    }
    /**
	 * 连续上传文件
	 * @param position 制定位置上传
	 * @param actionUrl 请求的地址
	 * @param params 上传的参数
	 * @param file 上传的文件
	 * @return
	 * @throws IOException
	 */
    public static String uploadContinuFile(String position,String actionUrl, Map<String, String> params, File file,String fileNamePre) throws IOException
    {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
      //  conn.setReadTimeout(5 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        if(params!=null){
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
            Logger.getLogger(TAG).i("key="+entry.getKey()+"  value="+entry.getValue());
        }
        }
        StringBuilder sb2 = new StringBuilder();
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        outStream.write(sb.toString().getBytes());
//        outStream.writeChars(sb.toString());
        outStream.flush();
        InputStream in = null;
        // 发送文件数据
        if (file != null)
        {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                // name是post中传参的键 filename是文件的名称
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileNamePre+file.getName() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
				RandomAccessFile fileOutStream = new RandomAccessFile(file, "r");
				fileOutStream.seek(Integer.valueOf(position));
				byte[] buffer = new byte[50*1024];
				int len = -1;
				int length = Integer.valueOf(position);
				if( (len = fileOutStream.read(buffer)) != -1){
					outStream.write(buffer, 0, len);
					length += len;
				}
				fileOutStream.close();
                outStream.write(LINEND.getBytes());
        }
        	  // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();
            System.out.println("status="+res);
            if (res == 200)
            {
                in = conn.getInputStream();
                int ch;
                while ((ch = in.read()) != -1)
                {
                    sb2.append((char) ch);
                }
                outStream.close();
                conn.disconnect();
                String result=new String(sb2.toString().getBytes("iso-8859-1"), "utf-8");
                System.out.println("result="+result);
                return result;
            }else{
            	 outStream.close();
                 conn.disconnect();
                 return "";
            }
    }
	public static Map<String, Object> socketServerFormFiles(String saveDir,
			DataInputStream in) throws IOException, FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		// 读取文件个数
		String fileCount = readString(in);
		long totol=0;
		long progress=0;
		if (null != fileCount && !"".equals(fileCount)) {
			int fCount = Integer.valueOf(fileCount);
			if(fCount>0){
				totol=Integer.valueOf(readString(in));
			}
			for (int i = 0; i < fCount; i++) {
				// 读取文件名
				String fileName = readString(in);
				// 读取文件大小
				String fileSize1 = readString(in);
				// 读取文件流
				long fileSize = Long.parseLong(fileSize1);
				String trueName = fileName
						.substring(fileName.lastIndexOf("\\") + 1);
				String suffix = "";
				if (trueName.indexOf('.') != -1)
					suffix = trueName.substring(trueName.lastIndexOf('.') + 1);
				suffix = suffix.toLowerCase();
				Date date = new Date();
				String tempName = "@F-" + "@" + date.getTime();
				if (!"".equals(suffix))
					tempName += "." + suffix;

				if (fileSize != 0 && fileName != null) {
					String fileTempPath = saveDir + "\\";
					File file = new File(fileTempPath);
					if (!file.exists())
						file.mkdirs();

					file = new File(fileTempPath + tempName);
					if (!file.exists())
						file.createNewFile();
					map.put(file.getAbsolutePath(), fileName);
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					byte[] buf = new byte[1024];
					if (fileSize <= buf.length) {
						buf = new byte[(int) fileSize];
					}
					int num = in.read(buf);
					int count = 0;
					while ((count <= fileSize && num > 0)) {
						raf.write(buf, 0, num);
						raf.skipBytes(num);
						count = count + num;
						if ((fileSize - count) <= buf.length) {
							buf = new byte[(int) (fileSize - count)];
						}
						progress=progress+num;
						if(mListener!=null){
							mListener.onProgress(file.getAbsolutePath(), progress, totol);
						}
						num = in.read(buf);
					}
					raf.close();
					System.out.println(file.length());
					if (fileSize > file.length()) {
						file.delete();
					}
				}
			}
		}
		return map;
	}

	public static Map<String, Object> socketServerFormParams(
			DataInputStream in) throws IOException {
		Map<String, Object> map=new HashMap<String, Object>();
		// 读取参数
		String result = readString(in);
		String[] ps = result.split("--");
		for (int i = 0; i < ps.length; i++) {
			String[] p = ps[i].split("\r\n");
			String key = "";
			for (int j = 0; j < p.length; j++) {
				String[] s = p[j].split("=");
				if (s.length > 1) {
					if ("key".equals(s[0])) {
						key = s[1];
					}
					if ("value".equals(s[0])) {
						map.put(key, s[1]);
					}
				}
			}
		}
		return map;
	}

	public static String socketClientForm(String host,int port, Map<String, String> params,
			Map<String, File> files) throws IOException {
		Socket socket=new Socket(host, port);
		String PREFIX = "--", LINEND = "\r\n";
		OutputStream os=socket.getOutputStream();
		InputStream is=socket.getInputStream();
		DataOutputStream dataOutputStream =new DataOutputStream(
				new BufferedOutputStream(os));
//		DataOutputStream dataOutputStream = new DataOutputStream(os);
		StringBuffer p = new StringBuffer();

		// 拼接参数
		for (Map.Entry<String, String> entry : params.entrySet()) {
			p.append(PREFIX);
			p.append(LINEND);
			p.append("Content-type=text/plain");
			p.append(LINEND);
			String key = entry.getKey();
			String value = entry.getValue();
			p.append("key=" + key);
			p.append(LINEND);
			p.append("value=" + value);
			p.append(LINEND);
		}
		writeString(dataOutputStream, p.toString());
		// 文件个数
		int fileCount = 0;
		if (files != null) {
			fileCount = files.size();
		}
		writeString(dataOutputStream, fileCount + "");
		// 添加文件
		if (files != null) {
			long totol=0;
			for (Map.Entry<String, File> entry : files.entrySet()) {
				totol=totol+entry.getValue().length();
			}
			writeString(dataOutputStream, totol + "");
			long progress=0;
			for (Map.Entry<String, File> entry : files.entrySet()) {
				String key = entry.getKey();
				File file = entry.getValue();
				// 写入文件名
				writeString(dataOutputStream, key);
				// 写入文件大小
				writeString(dataOutputStream, file.length() + "");
				FileInputStream fos = new FileInputStream(file);
				byte[] buf = new byte[1024];
				int num = 0;
				while ((num = fos.read(buf)) != (-1)) {
					dataOutputStream.write(buf, 0, num);
					progress=progress+num;
					dataOutputStream.flush();
					if(mListener!=null){
						mListener.onProgress(file.getAbsolutePath(), progress, totol);
					}
				}
				fos.close();
			}
		}
		dataOutputStream.flush();
		
		String result="";
		BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
		String line = null;
		StringBuilder sb=new StringBuilder();
		
		try{
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}}catch (Exception e) {
			// TODO: handle exception
		}
		br.close();
		result=sb.toString();
		dataOutputStream.close();
		os.close();
		is.close();
		socket.close();
		System.out.println("result="+result);
		return result;
	}

	private static String readString(DataInputStream in) throws IOException {
		String data = "";
		char c = in.readChar();
		while (c != '*') {
			data += c;
			c = in.readChar();
		}
		return data;
	}

	private  static void writeString(DataOutputStream out, String data)
			throws IOException {
		if(!StringUtils.isEmpty(data)){
		for (int i = 0; i < data.length(); i++){
			out.writeChar(data.charAt(i));
		}
		}
		out.writeChar('*');
	}
   static String key="!@#$%@#$";
	private static byte[] compress(byte[] str) throws IOException {
		if (null == str || str.length == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str);
		gzip.close();
		byte[] res = out.toByteArray();
		out.close();
		return res;
	}

	private static byte[] unCompress(byte[] str) throws IOException {
		if (null == str || str.length == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str);
		GZIPInputStream gzip = new GZIPInputStream(in);
		byte[] buffer = new byte[1024];
		int n = 0;
		while ((n = gzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		byte[] res = out.toByteArray();
		gzip.close();
		in.close();
		out.close();
		return res;
	}
}
