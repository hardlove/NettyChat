package com.freddy.im.constant;

/**
 * Created by CL on 2019/5/14.
 *
 * @description:
 */

public interface IMConstant {
    String STATUS = "status";
    String SOURCE = "android";

    String HEAD = "head";
    String DATA = "data";
    String TYPE = "type";
    String CONTENT_TYPE = "contentType";
    String MESSAGE_ID = "messageId";
    String ID = "id";

    //登录状态
    int LOGIN_AUTH_SUCCEED = 1;//登录成功
    int LOGIN_AUTH_FAILED = 2;//登录失败
    int LOGIN_AUTH_PROGRESSING = 0;//正在登录
    int LOGIN_AUTH_KICK_OUT = 3;//被踢下线
    //发送消息状态结果
    int SEND_MSG_PROGRESSING=1;
    int SEND_MSG_SUCCEED = 2;
    int SEND_MSG_FAILED = 3;




}
