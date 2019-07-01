package com.freddy.chat.utils.encry;


import org.apache.commons.codec.binary.Base64;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class HttpEncryptUtil {

    /**
     * 获取AES加密后的Prk
     */
    public static String getEncrptyPrk() throws Exception {
        //加密app生成的AES秘钥
//      return   Base64.encodeToString(AESUtil.encryptAES(Base64.decode(KeyUtil.APP_AES_KEY, Base64.DEFAULT), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)), Base64.DEFAULT);
        return Base64.encodeBase64String(AESUtil.encryptAES(Base64.decodeBase64(KeyUtil.APP_AES_KEY), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)));

    }

    /**
     * 加密data
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrptyData(String data) throws Exception {
        return Base64.encodeBase64String(AESUtil.encryptAES(data.getBytes(), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)));
//        return Base64.encodeToString(AESUtil.encryptAES(data.getBytes("UTF-8"), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)), Base64.DEFAULT);
    }

    /**
     * 获取解密后的prk
     *
     * @param prk
     * @return
     * @throws Exception
     */
    public static String getDecrptyPrk(String prk) throws Exception {
        //解密app生成的AES秘钥
        byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(prk), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY));
        return Base64.encodeBase64String(decryptAES);

//        return Base64.encodeToString(AESUtil.decryptAES(Base64.decode(prk, Base64.DEFAULT), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)), Base64.DEFAULT);

    }

    /**
     * 解密data
     *
     * @param prk
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrptyData(String prk, String data) throws Exception {
        //解密data
        byte[] decrpytBytes = AESUtil.decryptAES(Base64.decodeBase64(data), AESUtil.loadKeyAES(prk));
        return new String(decrpytBytes);
//        byte[] decrpytBytes = AESUtil.decryptAES(Base64.decode(data, Base64.DEFAULT), AESUtil.loadKeyAES(prk));
//        return new String(decrpytBytes, "UTF-8");
    }


}
