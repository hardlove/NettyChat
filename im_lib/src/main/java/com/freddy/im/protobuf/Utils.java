package com.freddy.im.protobuf;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CL on 2019/5/8.
 *
 * @description:
 */

public class Utils {
    public static String format(Message protoMsg) {
        String jsonFormat = JsonFormat.printToString(protoMsg);
        try {
            return new JSONObject(jsonFormat).toString(1);
        } catch (JSONException e) {
            e.printStackTrace();
            return jsonFormat;
        }

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
            case 5001:
            case 5002:
            case 5003:
            case 5004:
            case 5005:
            case 5006:
            case 5007:
            case 5008:
            case 5009:
            case 50010:
                msgType = "回执";
                break;
            default:
                msgType = "Unknown";

        }
        return msgType;
    }

}
