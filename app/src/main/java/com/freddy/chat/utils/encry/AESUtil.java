package com.freddy.chat.utils.encry;


import android.os.Handler;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class AESUtil {
    //生成AES秘钥，然后Base64编码
    public static String genKeyAES() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();
        String base64Str = byte2Base64(key.getEncoded());
        return base64Str;
    }

    //将Base64编码后的AES秘钥转换成SecretKey对象
    public static SecretKey loadKeyAES(String base64Key) throws Exception {
        byte[] bytes = base642Byte(base64Key);
        SecretKeySpec key = new SecretKeySpec(bytes, "AES");
        return key;
    }

    //加密
    public static byte[] encryptAES(byte[] source, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(source);
    }


    //解密
    public static byte[] decryptAES(byte[] source, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(source);
    }

    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes) {
        if (HttpEncryptUtil.useAndroidBase64) {
            return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        } else {
            return Base64.encodeBase64String(bytes);
        }

    }

    //Base64编码转字节数组
    public static byte[] base642Byte(String base64Key) throws IOException {
        if (HttpEncryptUtil.useAndroidBase64) {
            return android.util.Base64.decode(base64Key, android.util.Base64.DEFAULT);
        } else {
            return Base64.decodeBase64(base64Key);
        }

    }

}
