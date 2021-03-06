package com.freddy.chat.utils.encry;


import org.apache.commons.codec.binary.Base64;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class HttpEncryptUtil {

   static boolean useAndroidBase64 = false;

    /**
     * 获取AES加密后的Prk
     */
    public static String getEncrptyPrk() throws Exception {
        //加密app生成的AES秘钥
        if (useAndroidBase64) {
            return android.util.Base64.encodeToString(AESUtil.encryptAES(android.util.Base64.decode(KeyUtil.APP_AES_KEY, android.util.Base64.DEFAULT), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)), android.util.Base64.DEFAULT);
        } else {
            return Base64.encodeBase64String(AESUtil.encryptAES(Base64.decodeBase64(KeyUtil.APP_AES_KEY), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)));

        }

    }

    /**
     * 加密data
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrptyData(String data) throws Exception {
        if (useAndroidBase64) {
            return android.util.Base64.encodeToString(AESUtil.encryptAES(data.getBytes("UTF-8"), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)), android.util.Base64.DEFAULT);
        } else {
            return Base64.encodeBase64String(AESUtil.encryptAES(data.getBytes(), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)));

        }

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
        if (useAndroidBase64) {
            return android.util.Base64.encodeToString(AESUtil.decryptAES(android.util.Base64.decode(prk, android.util.Base64.DEFAULT), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY)), android.util.Base64.DEFAULT);
        } else {
            byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(prk), AESUtil.loadKeyAES(KeyUtil.SERVER_AES_KEY));
            return Base64.encodeBase64String(decryptAES);
        }




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
        if (useAndroidBase64) {
            byte[] decrpytBytes = AESUtil.decryptAES(android.util.Base64.decode(data, android.util.Base64.DEFAULT), AESUtil.loadKeyAES(prk));
            return new String(decrpytBytes, "UTF-8");
        } else {
            byte[] decrpytBytes = AESUtil.decryptAES(Base64.decodeBase64(data), AESUtil.loadKeyAES(prk));
            return new String(decrpytBytes);
        }



    }


}
