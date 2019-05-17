package com.freddy.chat.utils.encry;

import org.json.JSONObject;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class HttpEncryptUtil {

    /**
     * APP端的AES秘钥加密data部分内容
     * @param aesKeyStr Base64编码后的AES秘钥
     * @param data 加密的数据
     * @return
     */
    public static String encrprtyData(String aesKeyStr, String data) throws Exception {
        SecretKey aesKey = AESUtil.loadKeyAES(aesKeyStr);
        byte[] bytes = AESUtil.encryptAES(data.getBytes(), aesKey);
        return RSAUtil.byte2Base64(bytes);
    }

    /**
     * 用服务端提供的AES秘钥加密App端的AES秘钥
     * @param serverAesKey
     * @param appAesKey 自己app生成的AES秘钥
     * @return
     * @throws Exception
     */
    public static String encrptyAppAeskey(String serverAesKey,String appAesKey) throws Exception {
        SecretKey aesKey = AESUtil.loadKeyAES(serverAesKey);
        byte[] bytes = AESUtil.encryptAES(appAesKey.getBytes(), aesKey);
        return RSAUtil.byte2Base64(bytes);
    }

    /**
     * 用服务端提供的AES秘钥解密APP端的AES秘钥（即解密收到的消息中的AES秘钥，然后再用该AES秘钥解密消息data）
     * @param serverAesKey
     * @param appAesKe  prk内容
     * @return
     * @throws Exception
     */
    public static String decrptyAppAesKey(String serverAesKey,String appAesKe) throws Exception {
        SecretKey aesKey = AESUtil.loadKeyAES(serverAesKey);
        byte[] bytes = AESUtil.decryptAES(appAesKe.getBytes(), aesKey);
        return RSAUtil.byte2Base64(bytes);
    }

    //========================================================


    /**
     * App 公钥加密AES秘钥
     * @param aesKeyStr AES秘钥
     * @param publicKeyStr App公钥
     * @return
     */
    public static String encrptyAesKey(String aesKeyStr,String publicKeyStr) throws Exception {
        //用App公钥加密AES秘钥
        PublicKey publicKey = RSAUtil.string2PublicKey(publicKeyStr);
        byte[] encryptAesKey = RSAUtil.publicEncrypt(aesKeyStr.getBytes(), publicKey);
        return RSAUtil.byte2Base64(encryptAesKey);

    }

    /**
     * App 秘钥解密AES秘钥
     * @param encryptAesKey 加密后的AES秘钥
     * @param privateKeyStr App私钥
     *
     * @return
     */
    public static String decrprtAesKey(String encryptAesKey,String privateKeyStr) throws Exception {
        //用App秘钥解密AES秘钥
        PrivateKey privateKey = RSAUtil.string2PrivateKey(privateKeyStr);
        byte[] decryptAesKey = RSAUtil.privateDecrypt(encryptAesKey.getBytes(), privateKey);
        return RSAUtil.byte2Base64(decryptAesKey);

    }



    //APP加密请求内容
    public static String appEncrypt(String appPublicKeyStr, String content) throws Exception{
        //将Base64编码后的Server公钥转换成PublicKey对象
        PublicKey serverPublicKey = RSAUtil.string2PublicKey(KeyUtil.SERVER_PUBLIC_KEY);
        //每次都随机生成AES秘钥
        String aesKeyStr = AESUtil.genKeyAES();
        SecretKey aesKey = AESUtil.loadKeyAES(aesKeyStr);
        //用Server公钥加密AES秘钥
        byte[] encryptAesKey = RSAUtil.publicEncrypt(aesKeyStr.getBytes(), serverPublicKey);
        //用AES秘钥加密APP公钥
        byte[] encryptAppPublicKey = AESUtil.encryptAES(appPublicKeyStr.getBytes(), aesKey);
        //用AES秘钥加密请求内容
        byte[] encryptRequest = AESUtil.encryptAES(content.getBytes(), aesKey);

        JSONObject result = new JSONObject();
        result.put("ak", RSAUtil.byte2Base64(encryptAesKey).replaceAll("\r\n", ""));
        result.put("apk", RSAUtil.byte2Base64(encryptAppPublicKey).replaceAll("\r\n", ""));
        result.put("ct", RSAUtil.byte2Base64(encryptRequest).replaceAll("\r\n", ""));
        return result.toString();
    }

    //APP解密服务器的响应内容
    public static String appDecrypt(String appPrivateKeyStr, String content) throws Exception{
        JSONObject result = new JSONObject(content);
        String encryptAesKeyStr = (String) result.get("ak");
        String encryptContent = (String) result.get("ct");

        //将Base64编码后的APP私钥转换成PrivateKey对象
        PrivateKey appPrivateKey = RSAUtil.string2PrivateKey(appPrivateKeyStr);
        //用APP私钥解密AES秘钥
        byte[] aesKeyBytes = RSAUtil.privateDecrypt(RSAUtil.base642Byte(encryptAesKeyStr), appPrivateKey);
        //用AES秘钥解密请求内容
        SecretKey aesKey = AESUtil.loadKeyAES(new String(aesKeyBytes));
        byte[] response = AESUtil.decryptAES(RSAUtil.base642Byte(encryptContent), aesKey);

        return new String(response);
    }

    //服务器加密响应给APP的内容
    public static String serverEncrypt(String appPublicKeyStr, String aesKeyStr, String content) throws Exception{
        //将Base64编码后的APP公钥转换成PublicKey对象
        PublicKey appPublicKey = RSAUtil.string2PublicKey(appPublicKeyStr);
        //将Base64编码后的AES秘钥转换成SecretKey对象
        SecretKey aesKey = AESUtil.loadKeyAES(aesKeyStr);
        //用APP公钥加密AES秘钥
        byte[] encryptAesKey = RSAUtil.publicEncrypt(aesKeyStr.getBytes(), appPublicKey);
        //用AES秘钥加密响应内容
        byte[] encryptContent = AESUtil.encryptAES(content.getBytes(), aesKey);

        JSONObject result = new JSONObject();
        result.put("ak", RSAUtil.byte2Base64(encryptAesKey).replaceAll("\r\n", ""));
        result.put("ct", RSAUtil.byte2Base64(encryptContent).replaceAll("\r\n", ""));
        return result.toString();
    }

    //服务器解密APP的请求内容
    public static String serverDecrypt(String content) throws Exception{
        JSONObject result = new JSONObject(content);
        String encryptAesKeyStr = (String) result.get("ak");
        String encryptAppPublicKeyStr = (String) result.get("apk");
        String encryptContent = (String) result.get("ct");

        //将Base64编码后的Server私钥转换成PrivateKey对象
        PrivateKey serverPrivateKey = RSAUtil.string2PrivateKey(KeyUtil.SERVER_PRIVATE_KEY);
        //用Server私钥解密AES秘钥
        byte[] aesKeyBytes = RSAUtil.privateDecrypt(RSAUtil.base642Byte(encryptAesKeyStr), serverPrivateKey);
        //用AES秘钥解密APP公钥
        SecretKey aesKey = AESUtil.loadKeyAES(new String(aesKeyBytes));
        byte[] appPublicKeyBytes = AESUtil.decryptAES(RSAUtil.base642Byte(encryptAppPublicKeyStr), aesKey);
        //用AES秘钥解密请求内容
        byte[] request = AESUtil.decryptAES(RSAUtil.base642Byte(encryptContent), aesKey);

        JSONObject result2 = new JSONObject();
        result2.put("ak", new String(aesKeyBytes));
        result2.put("apk", new String(appPublicKeyBytes));
        result2.put("ct", new String(request));
        return result2.toString();
    }
}
