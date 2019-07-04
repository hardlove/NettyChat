package com.freddy.chat;

import android.util.Log;

import com.freddy.chat.utils.encry.AESUtil;
import com.freddy.chat.utils.encry.HttpEncryptUtil;
import com.freddy.chat.utils.encry.KeyUtil;
import com.orhanobut.logger.Logger;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class IMessageEncrpty {
    @Test
    public void testGreateAES() throws Exception {
//        OUQyOTc5QjAtRjc0Ny00QTUwLUJGMUMtREEwOTQ4OTVBQUNF
        String ase = AESUtil.genKeyAES();
        Logger.d("App AES:" + ase);

    }

    @Test
    public void testBase64() {
        String source = "OUQyOTc5QjAtRjc0Ny00QTUwLUJGMUMtREEwOTQ4OTVBQUNF";
        String base64String = AESUtil.byte2Base64(source.getBytes());
        Logger.d("base64String:" + base64String);
    }

    @Test
    public void tesEncrpty() throws Exception {
        String serverAesKey = "ZW5+rAbLXU7QniZzUbRRbg==";

        String content = "123456";
        //加密app生成的AES秘钥
//        String prk = Base64.encodeBase64String(AESUtil.encryptAES(Base64.decodeBase64(KeyUtil.APP_AES_KEY), AESUtil.loadKeyAES(serverAesKey)));
//        String data = Base64.encodeBase64String(AESUtil.encryptAES(content.getBytes(), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)));
        String prk = HttpEncryptUtil.getEncrptyPrk();
        String data = HttpEncryptUtil.encrptyData(content);
        Logger.d("加密prk：" + prk);
        Logger.d("加密data：" + data);

    }

    @Test
   public void test() throws Exception {
        String ss = "9zB39ZtsPk2vv5NbQmVXWw==";
        String dd = "wxeQRQ4PIWrC6ytURY/ggnm5kbNV7EkR8a0ZdciL6S0=";
                byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(dd), AESUtil.loadKeyAES(ss));
        String date = new String(decryptAES);
        System.out.println("data:" + date);
    }


    @Test
    public void testdecrpty() throws Exception {


        String prk = "l9OneKO0oiS8qkzyCnDGQHm5kbNV7EkR8a0ZdciL6S0=";
        String data = "aHCDgtudkiaZjG+TK5uiOSt5qSEMqO0lWrC1k63rdzhZNRSCv4rugVYeP9ao3b/X7iP92OthVGWc3noXwjcho4scoQd0Dn53xyDsn1Tjx4Qcqy8WNm7piLpGiNdpChxN2kFny4Cn7l14rc1HjwDsbd7ZeAcH/eDUpvOoj4xHtj/Vlx3SxuZ+AyZetd48m93W2GXlx8yivHXVIbSfEcENkyEy1FB3MyeqHqKHPsVQQF/mUSAmCja1H6DvyhvgudLdcGLQjpTGGFHDqEytg5MZSg==";

//        String serverAesKey = "ZW5+rAbLXU7QniZzUbRRbg==";
//        //解密app生成的AES秘钥
//        byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(prk), AESUtil.loadKeyAES(serverAesKey));
//        //解密data
//        byte[] decrpytBytes = AESUtil.decryptAES(Base64.decodeBase64(data), AESUtil.loadKeyAES(Base64.encodeBase64String(decryptAES)));
//
//
//        Logger.d("prk:" + Base64.encodeBase64String(decryptAES));
//        Logger.d("data:" + new String(decrpytBytes));

        String desPrk = HttpEncryptUtil.getDecrptyPrk(prk);
        String desData = HttpEncryptUtil.decrptyData(desPrk, data);

        System.out.println("解密prk："+desPrk);
        System.out.println("解密data："+desData);
//        Logger.d("解密prk:" + desPrk);
//        Logger.d("解密data:" + desData);

    }
}
