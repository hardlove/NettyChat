package com.freddy.chat.utils.encry;


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
    public static String genKeyAES() throws Exception{
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();
        String base64Str = byte2Base64(key.getEncoded());
        return base64Str;
    }

    //将Base64编码后的AES秘钥转换成SecretKey对象
    public static SecretKey loadKeyAES(String base64Key) throws Exception{
        byte[] bytes = base642Byte(base64Key);
        SecretKeySpec key = new SecretKeySpec(bytes, "AES");
        return key;
    }

    //加密
    public static byte[] encryptAES(byte[] source, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(source);
    }



    /**
     *
     * @param
     * @return 获取AES加密后的Prk
     */
    public static String getEncrptyAESPrk() throws Exception {
        //加密app生成的AES秘钥
        return Base64.encodeBase64String(AESUtil.encryptAES(Base64.decodeBase64(KeyUtil.APP_AES_KEY), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)));

    }

    /**
     * 加密
     * @param content
     * @return
     * @throws Exception
     */
    public static String encrptyAESData(String content) throws Exception {
        return  Base64.encodeBase64String(AESUtil.encryptAES(content.getBytes(), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)));
    }

    public static String getDecrptyAESPrk(String prk) throws Exception {
        //解密app生成的AES秘钥
        byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(prk), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY));
        return Base64.encodeBase64String(decryptAES);

    }

    /**
     * 解密data
     * @param prk
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrptyAESData(String prk,String data) throws Exception {
        //解密data
        byte[] decrpytBytes = AESUtil.decryptAES(Base64.decodeBase64(data), AESUtil.loadKeyAES(getDecrptyAESPrk(prk)));
        return new String(decrpytBytes);
    }




    //解密
    public static byte[] decryptAES(byte[] source, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(source);
    }

    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes){
        return Base64.encodeBase64String(bytes);
    }

    //Base64编码转字节数组
    public static byte[] base642Byte(String base64Key) throws IOException {
        return Base64.decodeBase64(base64Key);
    }

}
