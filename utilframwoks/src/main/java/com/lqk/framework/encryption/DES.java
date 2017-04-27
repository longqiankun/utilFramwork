package com.lqk.framework.encryption;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DES {
	private static byte[] iv = {1,2,3,4,5,6,7,8};
	
	/**加密解密的key*/  
    private Key mKey;  
    /**解密的密码*/  
    private Cipher mDecryptCipher;  
    /**加密的密码*/  
    private Cipher mEncryptCipher;  
    public DES(String key) throws Exception  
    {  
        initKey(key);  
        initCipher();  
    }  
	/**
	 * 
	 * @author longqiankun
	 * @description : des加密
	 * @param encryptString
	 * @param encryptKey 长度是8
	 * @return
	 * @throws Exception
	 */
	public static String encryptDES(String encryptString, String encryptKey) throws Exception {
//		IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
		byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
	 
		return Base64.encode(encryptedData);
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : des解密
	 * @param decryptString
	 * @param decryptKey 长度是8
	 * @return
	 * @throws Exception
	 */
	public static String decryptDES(String decryptString, String decryptKey) throws Exception {
		byte[] byteMi = new Base64().decode(decryptString);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
//		IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
		byte decryptedData[] = cipher.doFinal(byteMi);
	 
		return new String(decryptedData);
	}
	

      
    /** 
     * 创建一个加密解密的key 
     * @param keyRule  
     */  
    public void initKey(String keyRule) {  
        byte[] keyByte = keyRule.getBytes();  
        // 创建一个空的八位数组,默认情况下为0   
        byte[] byteTemp = new byte[8];  
        // 将用户指定的规则转换成八位数组   
        for (int i = 0; i < byteTemp.length && i < keyByte.length; i++) {  
            byteTemp[i] = keyByte[i];  
        }  
        mKey = new SecretKeySpec(byteTemp, "DES");  
    }  
      
    /*** 
     * 初始化加载密码 
     * @throws Exception 
     */  
    private void initCipher() throws Exception  
    {  
        mEncryptCipher = Cipher.getInstance("DES");  
        mEncryptCipher.init(Cipher.ENCRYPT_MODE, mKey);  
          
        mDecryptCipher = Cipher.getInstance("DES");  
        mDecryptCipher.init(Cipher.DECRYPT_MODE, mKey);  
    }  
      
    /** 
     * 加密文件 
     * @param in 
     * @param savePath 加密后保存的位置 
     */  
    public void doEncryptFile(InputStream in,String savePath)  
    {  
        if(in==null)  
        {  
            System.out.println("inputstream is null");  
            return;  
        }  
        try {  
            CipherInputStream cin = new CipherInputStream(in, mEncryptCipher);  
            OutputStream os = new FileOutputStream(savePath);  
            byte[] bytes = new byte[1024];  
            int len = -1;  
            while((len=cin.read(bytes))>0)  
            {  
                os.write(bytes, 0, len);  
                os.flush();  
            }  
            os.close();  
            cin.close();  
            in.close();  
            System.out.println("加密成功");  
        } catch (Exception e) {  
            System.out.println("加密失败");  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * 加密文件 
     * @param filePath 需要加密的文件路径 
     * @param savePath 加密后保存的位置 
     * @throws FileNotFoundException  
     */  
    public void doEncryptFile(String filePath,String savePath) throws FileNotFoundException  
    {  
        doEncryptFile(new FileInputStream(filePath), savePath);  
    }  
      
      
    /** 
     * 解密文件 
     * @param in 
     */  
    public void doDecryptFile(InputStream in,String targetFilePath)  
    {  
        if(in==null)  
        {  
            System.out.println("inputstream is null");  
            return;  
        }  
        try {  
            CipherInputStream cin = new CipherInputStream(in, mDecryptCipher);  
            InputStreamReader inBuff = null;
			BufferedOutputStream outBuff = null;
			try {
				// 新建文件输入流并对它进行缓冲
				inBuff = new InputStreamReader(cin);
				// 新建文件输出流并对它进行缓冲
				outBuff = new BufferedOutputStream(new FileOutputStream(targetFilePath));
				// 缓冲数组
				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = cin.read(b)) != -1) {
					outBuff.write(b, 0, len);
				}
				// 刷新此缓冲的输出流
				outBuff.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 关闭流
				try {
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
						outBuff.close();
				} catch (IOException e) {
				}
			}
		
            
            in.close();  
            System.out.println("解密成功");  
        } catch (Exception e) {  
            System.out.println("解密失败");  
            e.printStackTrace();  
        }  
    }  
    /** 
     * 解密文件 
     * @param filePath  文件路径 
     * @throws Exception 
     */  
    public void doDecryptFile(String filePath,String targetFilePath) throws Exception  
    {  
        doDecryptFile(new FileInputStream(filePath),targetFilePath);  
    }  
}
