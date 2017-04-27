package com.lqk.framework.encryption;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class AES {
    public static final String TAG = "AES";
    
    public static boolean AESCipher(int cipherMode, String sourceFilePath,  
            String targetFilePath, String seed) {  
        boolean result = false;  
        FileChannel sourceFC = null;  
        FileChannel targetFC = null;  
  
        try {  
  
            if (cipherMode != Cipher.ENCRYPT_MODE  
                    && cipherMode != Cipher.DECRYPT_MODE) {  
                Log.d(TAG,  
                        "Operation mode error, should be encrypt or decrypt!");  
                return false;  
            }  
  
            Cipher mCipher = Cipher.getInstance("AES/CFB/NoPadding");  
  
            byte[] rawkey = getRawKey(seed.getBytes());  
            File sourceFile = new File(sourceFilePath);  
            File targetFile = new File(targetFilePath);  
  
            sourceFC = new RandomAccessFile(sourceFile, "r").getChannel();  
            targetFC = new RandomAccessFile(targetFile, "rw").getChannel();  
  
            SecretKeySpec secretKey = new SecretKeySpec(rawkey, "AES");  
  
            mCipher.init(cipherMode, secretKey, new IvParameterSpec(  
                    new byte[mCipher.getBlockSize()]));  
  
            ByteBuffer byteData = ByteBuffer.allocate(1024);  
            while (sourceFC.read(byteData) != -1) {  
                // 通过通道读写交叉进行。  
                // 将缓冲区准备为数据传出状态  
                byteData.flip();  
  
                byte[] byteList = new byte[byteData.remaining()];  
                byteData.get(byteList, 0, byteList.length);  
//此处，若不使用数组加密解密会失败，因为当byteData达不到1024个时，加密方式不同对空白字节的处理也不相同，从而导致成功与失败。   
                byte[] bytes = mCipher.doFinal(byteList);  
                targetFC.write(ByteBuffer.wrap(bytes));  
                byteData.clear();  
            }  
  
            result = true;  
        } catch (IOException e) {  
            Log.d(TAG, e.getMessage());  
  
        } catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {  
            try {  
                if (sourceFC != null) {  
                    sourceFC.close();  
                }  
                if (targetFC != null) {  
                    targetFC.close();  
                }  
            } catch (IOException e) {  
                Log.d(TAG, e.getMessage());  
            }  
        }  
  
        return result;  
    }

    public static String encrypt(String seed, String clearText) {
        // Log.d(TAG, "加密前的seed=" + seed + ",内容为:" + clearText);
        byte[] result = null;
        try {
            byte[] rawkey = getRawKey(seed.getBytes());
            result = encrypt(rawkey, clearText.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String content = toHex(result);
        // Log.d(TAG, "加密后的内容为:" + content);
        return content;

    }

    public static String decrypt(String seed, String encrypted) {
        // Log.d(TAG, "解密前的seed=" + seed + ",内容为:" + encrypted);
        byte[] rawKey;
        try {
            rawKey = getRawKey(seed.getBytes());
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            String coentn = new String(result);
            // Log.d(TAG, "解密后的内容为:" + coentn);
            return coentn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static byte[] getRawKey(byte[] seed) throws NoSuchAlgorithmException  {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr);
        SecretKey sKey = kgen.generateKey();
        byte[] raw = sKey.getEncoded();

        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        final String HEX = "0123456789ABCDEF";
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}