package com.freddy.im;

/**
 * @author CL
 * 消息类型
 */
public interface MessageType {
    //=========================消息类型=========================
    /*
    * 心跳消息
    */
    int HEARTBEAT = 0;
    /**
     * 单聊消息
     */
    int SINGLE_CHAT = 1;

    /**
     * 群聊消息
     */
    int GROUP_CHAT = 2;

    /**
     * 朋友圈消息
     */

    int MOMENTS = 3;
    /**
     * 系统通知
     */
    int SYSTEM_NOTIFY = 4;
    /*
     * 登录认证消息
     */
    int LOGIN_AUTH = 5;
    /**
     * 好友添加通知
     */
    int ADD_FRIEND = 6;
    /**
     * 群邀请通知
     */
    int GROUP_INVITE = 7;
    /**
     * PC登录
     */
    int PC_LOGIN = 8;
    /**
     * PC强退
     */
    int PC_KICK_OUT = 9;
    //=========================消息类型=========================


    //=========================消息回执=========================
    /**
     * 单聊消息回执
     */
    int SINGLE_CHAT_RECEIPT = 5001;

    /**
     * 群聊消息回执
     */
    int GROUP_CHAT_RECEIPT = 5002;

    /**
     * 朋友圈消息回执
     */
    int MOMENTS_RECEIPT = 5003;
    /**
     * 系统通知回执
     */
    int SYSTEM_NOTIFY_RECEIPT = 5004;
    /**
     * 登录验证失败
     */
    int LOGIN_AUTH_SUCCEED_RECEIPT = 5005;
    /**
     * 登录认证成功
     */
    int LOGIN_AUTH_FAILED_RECEIPT = 5006;
    /**
     * 好友添加回执
     */
    int ADD_FRIEND_RECEIPT = 5008;
    /**
     * 群邀请回执
     */
    int GROUP_INVITE_RECEIPT = 5009;
    /**
     * pc登陆回执
     */
    int PC_LOGIN_RECEIPT = 5010;

    /**
     * pc强退回执
     */
    int PC_KICK_OUT_RECEIPT = 5011;
    //=========================消息回执=========================


    //=========================消息状态报告======================
    /*
     * 消息发送成功的报告
     */
    int MSG_SENT_SUCCEED_REPORT = 1010;
    /*
    * 消息发送失败的报告
    */
    int MSG_SENT_FAILED_REPORT = 1011;
    /**
     * 登录状态报告（status：0 正在登录，1 登录成功，2 登录失败）
     */
    int LOGIN_AUTH_STATUS_REPORT = 1000;
    //=========================消息状态报告======================


    /**
     * 内容类型
     */
    interface MessageContentType {
        int TEXT = 1;
        int IMAGE = 2;
        int VOICE = 3;


    }
}
