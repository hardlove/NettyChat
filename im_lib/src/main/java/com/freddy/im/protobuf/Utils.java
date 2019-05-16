package com.freddy.im.protobuf;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * Created by CL on 2019/5/8.
 *
 * @description:
 */

public class Utils {
    public static String format(Message protoMsg) {
        String jsonFormat = JsonFormat.printToString(protoMsg);
        return jsonFormat;

    }

    public static String getMessageTypeName(int type) {
        String msgType = "" + type;
        switch (type) {
            case 0:
                msgType = "心跳";
                break;
            case 1:
                msgType = "单聊";
                break;
            case 2:
                msgType = "群聊";
                break;
            case 3:
                msgType = "朋友圈";
                break;
            case 4:
                msgType = "系统通知";
                break;
            case 5:
                msgType = "登录认证";
                break;
            case 6:
                msgType = "条件好友通知";
                break;
            case 7:
                msgType = "群邀请通知";
                break;
            case 8:
                msgType = "PC登录";
                break;
            case 9:
                msgType = "PC强退";
                break;
        }
        return msgType;
    }
}
