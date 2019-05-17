package com.freddy.chat;

import com.freddy.chat.utils.encry.AESUtil;
import com.freddy.chat.utils.encry.KeyUtil;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 * Created by CL on 2019/5/17.
 *
 * @description:
 */

public class IMessageEncrpty {

    @Test
    public void tesEncrpty() throws Exception {
        String serverAesKey = "ZW5+rAbLXU7QniZzUbRRbg==";

        String content = "123456";
        //加密app生成的AES秘钥
        String prk = Base64.encodeBase64String(AESUtil.encryptAES(Base64.decodeBase64(KeyUtil.APP_AES_KEY), AESUtil.loadKeyAES(serverAesKey)));
        String data = Base64.encodeBase64String(AESUtil.encryptAES(content.getBytes(), AESUtil.loadKeyAES(KeyUtil.APP_AES_KEY)));
        System.out.println("加密prk：" + prk);
        System.out.println("加密data：" + data);
//        加密prk：R0ljotvF+6GLml9Uwf/mfXm5kbNV7EkR8a0ZdciL6S0=
//        加密data：30VzqVBXm4aXMsKIty3o6A==
    }

    @Test
    public void testdecrpty() throws Exception {

        String prk = "R0ljotvF+6GLml9Uwf/mfXm5kbNV7EkR8a0ZdciL6S0=";
        String data = "30VzqVBXm4aXMsKIty3o6A==";

        String serverAesKey = "ZW5+rAbLXU7QniZzUbRRbg==";
        //解密app生成的AES秘钥
        byte[] decryptAES = AESUtil.decryptAES(Base64.decodeBase64(prk), AESUtil.loadKeyAES(serverAesKey));
        //解密data
        byte[] decrpytBytes = AESUtil.decryptAES(Base64.decodeBase64(data), AESUtil.loadKeyAES(Base64.encodeBase64String(decryptAES)));


        System.out.println("prk:" + Base64.encodeBase64String(decryptAES));
        System.out.println("data:" + new String(decrpytBytes));

    }
}
