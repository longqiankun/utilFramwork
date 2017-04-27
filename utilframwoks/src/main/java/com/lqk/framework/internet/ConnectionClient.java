package com.lqk.framework.internet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.lqk.framework.util.Logger;
import com.lqk.framework.util.PreferencesUtils;
import com.lqk.framework.util.StringUtils;
/**
 * 
* @ClassName: ConnectionClient
* @Description: 链接服务器的客户端操作
* @author longqiankun
* @date 2014-7-7 上午11:13:30
*
 */
public class ConnectionClient {

	/**
	 * 执行POST方法 不含有 文件传送
	 * 
	 * @param severMethod
	 *            服务器的方法接口
	 * @param p
	 *            传递给服务器的参数
	 * @param json
	 *            POST给服务器的参数
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	private static String Tag= "net";
//	 public static int CONNECTION_TIMEOUT = 2*60*1000;  
//	    public static int SOCKET_TIMEOUT  = 2*60*1000;  
	    private static int CONNECTION_TIMEOUT = 5000;  
	    private static int SOCKET_TIMEOUT  = 1*60*1000;  
	    
	    
	    	/**
		    * @Title: doGet
		    * @Description: get请求
		    * @param json json对象封装的参数
		    * @param method get请求的方法
		    * @param @throws IOException
		    * @param @throws JSONException
		    * @return String 服务器返回的json串
		    * @throws
		     */
	    public static String doGet(String url,JSONObject json,String methodName,String methodValue) throws IOException, JSONException {
	    	return doGet(null, url, json, methodName, methodValue, null);
	    }
	    
	    /**
	    * @Title: doGet
	    * @Description: get请求,添加cookie
	    * @param json json对象封装的参数
	    * @param method get请求的方法
	    * @param @throws IOException
	    * @param @throws JSONException
	    * @return String 服务器返回的json串
	    * @throws
	     */
	public static String doGet(Context mContext,String url,JSONObject json,String methodName,String methodValue,String cookieMethod) throws IOException, JSONException {
		Logger.getLogger(Tag).i( "url is "+url);
		StringBuilder sb=new StringBuilder();
		sb.append("\r\n");
		sb.append(url);
		sb.append("\r\n");
		if (json != null) {
			url+="?";
			if(!TextUtils.isEmpty(methodName)){
			url=url+methodName+"="+methodValue+"&";
			}
			@SuppressWarnings("unchecked")
			Iterator<String> iter = json.keys();
			int i=0;
			while (iter.hasNext()) {
				String key = iter.next();
				if(i!=0)url+="&";
				else i++;
				url+=(key+"="+json.getString(key));
				sb.append(json.getString(key));
				sb.append("\r\n");
				Logger.getLogger(Tag).i( "key="+key+" value="+json.getString(key));
			}
		}
		url = url.replaceAll(" ", "%20");
//		url = url.replaceAll("&", "%26");
		Logger.getLogger(Tag).i( "url is "+url);
		try{
		HttpGet httpGet = new HttpGet(url);
		if(mContext!=null){
			String reqesutCookie=PreferencesUtils.getString(mContext, "Cookie");
			httpGet.addHeader("Cookie", reqesutCookie);
			}
		HttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpGet);
		if(!StringUtils.isEmpty(methodName)&&!StringUtils.isEmpty(cookieMethod)){
			String cookie=httpResponse.getHeaders("Cookie").toString();
			if(mContext!=null){
			PreferencesUtils.setString(mContext, "Cookie",cookie);
			}
		}
		 HttpConnectionParams.setConnectionTimeout(httpGet.getParams(), CONNECTION_TIMEOUT);
		 HttpConnectionParams.setSoTimeout(httpGet.getParams(), SOCKET_TIMEOUT); 
		 Logger.getLogger(Tag).i( "status result is "+ httpResponse.getStatusLine().getStatusCode());
		 if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity he = httpResponse.getEntity();
			String response = EntityUtils.toString(he);
			Logger.getLogger(Tag).i( "result is "+ response);
			sb.append(response);
			sb.append("\r\n\r\n\r\n\r\n\r\n");
			return response;
		}else{
			return "";
		}
		} catch (ConnectTimeoutException e) {
		   }catch (SocketTimeoutException e)
		   {
			    e.printStackTrace();
			}
			catch (IOException e)
			{
			    e.printStackTrace();
			}
			return "";
	}
	/**
	 * 
	* @Title: doPost
	* @Description: post请求
	* @param task 异步操作类
	* @param json json对象封装的参数
	* @param method 请求方法
	* @param @throws IOException
	* @param @throws JSONException
	* @return String 服务器返回的json串
	* @throws
	 */
	public static String doPost(String url,JSONObject json,String methodName,String methodValue) throws IOException, JSONException {
		return doPost(null, url, json, methodName, methodValue, null);
	}

	/**
	 * 
	* @Title: doPost
	* @Description: post请求,添加cookie
	* @param mContext 上下文
	* @param json json对象封装的参数
	* @param method 请求方法
	* @param @throws IOException
	* @param @throws JSONException
	* @return String 服务器返回的json串
	* @throws
	 */
	public static String doPost(Context mContext,String url,JSONObject json,String methodName,String methodValue,String cookieMethod) throws IOException, JSONException {
		Logger.getLogger(Tag).i( "url is "+url);
		try{
		HttpPost httpPost = new HttpPost(url);
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		if (json != null) {
			@SuppressWarnings("unchecked")
			Iterator<String> iter = json.keys();
			if(!TextUtils.isEmpty(methodName)){
			params.add(new BasicNameValuePair(methodName, methodValue));
			}
			Logger.getLogger(Tag).i( "key is "+" type "+" value is "+methodValue);
			while (iter.hasNext()) {
				String key = iter.next();
				params.add(new BasicNameValuePair(key, json.getString(key)));
				Logger.getLogger(Tag).i( "key is "+key+"   value is "+json.getString(key));
			}
		}
		HttpEntity httpEntity = new UrlEncodedFormEntity(params,"UTF-8");
		if(mContext!=null){
		String reqesutCookie=PreferencesUtils.getString(mContext, "Cookie");
		httpPost.addHeader("Cookie", reqesutCookie);
		}
		httpPost.setEntity(httpEntity);
		HttpClient httpClient =getHttpClient();
		
		HttpResponse httpResponse = httpClient.execute(httpPost);
		if(!StringUtils.isEmpty(methodName)&&!StringUtils.isEmpty(cookieMethod)){
			String cookie=httpResponse.getHeaders("Cookie").toString();
			if(mContext!=null){
			PreferencesUtils.setString(mContext, "Cookie",cookie);
			}
		}
		
		 HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), CONNECTION_TIMEOUT);
//		 HttpConnectionParams.setSoTimeout(httpPost.getParams(), SOCKET_TIMEOUT); 
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity he = httpResponse.getEntity();
            long length = he.getContentLength();  
            InputStream is = he.getContent();  
            String s = "";  
            if(is != null) {  
                ByteArrayOutputStream baos = new ByteArrayOutputStream();  
                byte[] buf = new byte[1024];  
                int ch = -1;  
                int count = 0;  
                while((ch = is.read(buf)) != -1) {  
                   baos.write(buf, 0, ch);  
                   baos.flush();
                   count += ch;  
                   if(length > 0) {  
                       // 如果知道响应的长度
                   }
                }  
                s = new String(baos.toByteArray());     
            }
			return s;
		}else{
			 return "";
		}
		} catch (ConnectTimeoutException e) {
		   }catch (SocketTimeoutException e)
		   {
			    e.printStackTrace();
			}
			catch (IOException e)
			{
			    e.printStackTrace();
			}
			return "";
	}
	
	 private static HttpClient getHttpClient() {  
	        try {  
	            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
	            trustStore.load(null, null);  
	  
	            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);  
	            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
	  
	            HttpParams params = new BasicHttpParams();  
	  
	            HttpConnectionParams.setConnectionTimeout(params, 10000);  
	            HttpConnectionParams.setSoTimeout(params, 10000);  
	  
	            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  
	            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);  
	  
	            SchemeRegistry registry = new SchemeRegistry();  
	            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));  
	            registry.register(new Scheme("https", sf, 443));  
	  
	            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);  
	  
	            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);  
	            HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);  
	            HttpClient client = new DefaultHttpClient(ccm, params);  
	  
	            return client;  
	        } catch (Exception e) {  
	            return new DefaultHttpClient();  
	        }  
	    } 
	 
	 private static class MySSLSocketFactory extends SSLSocketFactory {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
					KeyManagementException, KeyStoreException, UnrecoverableKeyException {
				super(truststore);

				TrustManager tm = new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};

				sslContext.init(null, new TrustManager[] { tm }, null);
			}

			@Override
			public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
					throws IOException, UnknownHostException {
				return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
			}

			@Override
			public Socket createSocket() throws IOException {
				return sslContext.getSocketFactory().createSocket();
			}
		}
	/**
	* @Title: getConnection
	* @Description: 获取服务器请求连接
	* @param url 请求地址
	* @param @throws MalformedURLException
	* @param @throws IOException
	* @return HttpURLConnection
	* @throws
	 */
	
	static HttpURLConnection getConnection(String url)
			throws MalformedURLException, IOException {
		Logger.getLogger(Tag).i( "request url is :" + url);
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
					new InetSocketAddress(android.net.Proxy.getDefaultHost(),
							android.net.Proxy.getDefaultPort()));

			return (HttpURLConnection) new URL(url).openConnection(p);

		} else {
			return (HttpURLConnection) new URL(url).openConnection();
		}
	}
	/**
	 * 
	* @Title: doPostMethod
	* @Description: 上传文件
	* @param @param fileUrl 请求地址
	* @param @param json 参数信息
	* @param @param filePath 文件路径
	* @param @throws Exception
	* @return String
	* @throws
	 */
	public static String doPostMethod(String fileUrl,JSONObject json,String filePath)
			throws Exception {
		String b = "";
		try{
		File f = new File(filePath);
		HttpURLConnection request = getConnection(fileUrl);
		request.setDoOutput(true);
		request.setRequestMethod("POST");
		String boundary = "---------------------------37531613912423";
		String name = "pic";
		request.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);

		String pic = "\r\nContent-Disposition: form-data; name=\""
				+ name
				+ "\"; filename=\"postpic.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n";
		byte[] end_data = ("\r\n--" + boundary + "--\r\n").getBytes();
		FileInputStream stream = new FileInputStream(f);
		byte[] file = new byte[(int) f.length()];
		stream.read(file);

		OutputStream ot = request.getOutputStream();
		ot.write(("\r\n--" + boundary).getBytes());
		if (json != null) {
			@SuppressWarnings("rawtypes")
			Iterator iter = json.keys();
			@SuppressWarnings("unused")
			int i = 0;
			while (iter.hasNext()) {
				String key = (String) iter.next();
				ot.write(contentType(key).getBytes());
				// ot.write(json.getString(key).getBytes());
				ot.write(json.getString(key).getBytes());
				ot.write(("\r\n--" + boundary).getBytes());
			}
		}
		ot.write(pic.getBytes());
		ot.write(file);
		ot.write(end_data);
		ot.flush();
		ot.close();
		// stream.close();
		Logger.getLogger(Tag).i( "sending request....");
		request.setConnectTimeout(10000);
		request.setReadTimeout(10000);
		request.connect();
		Logger.getLogger(Tag).i( request.getResponseCode() + " "
				+ request.getResponseMessage());
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				request.getInputStream(), "utf-8"));
		String s = "";
		while ((b = reader.readLine()) != null) {
			Logger.getLogger(Tag).i( b);
			s += b;
		}
		if ("".equals(s)) {
		} else
			b = s;
		} catch (ConnectTimeoutException e) {
		   }catch (SocketTimeoutException e)
		   {
			    e.printStackTrace();
			}
			catch (IOException e)
			{
			    e.printStackTrace();
			}
			return b;
	}
	private static String contentType(String key) {
		return "\r\nContent-Disposition: form-data; name=\"" + key
				+ "\"\r\n\r\n";
	}
}
