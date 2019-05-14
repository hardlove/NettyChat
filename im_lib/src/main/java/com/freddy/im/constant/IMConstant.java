package com.freddy.im.constant;

/**
 * Created by CL on 2019/5/14.
 *
 * @description:
 */

public interface IMConstant {
    String STATUS = "status";
    String SOURCE = "android";
    int LOGIN_AUTH_SUCCEED = 1;//登录成功
    int LOGIN_AUTH_FAILED = 2;//登录失败
    int LOGIN_AUTH_PROGRESSING = 0;//正在登录
    int LOGIN_AUTH_KICK_OUT = 3;//被踢下线
}
