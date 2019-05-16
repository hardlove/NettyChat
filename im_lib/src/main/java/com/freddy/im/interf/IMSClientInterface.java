package com.freddy.im.interf;

import com.freddy.im.MsgDispatcher;
import com.freddy.im.MsgTimeoutTimerManager;
import com.freddy.im.listener.IMSConnectStatusCallback;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.protobuf.MessageProtobuf;

import java.util.Vector;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMSClientInterface.java</p>
 * <p>@PackageName:     com.freddy.im.interf</p>
 * <b>
 * <p>@Description:     ims抽象接口，需要切换到其它方式实现im功能，实现此接口即可</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/03/31 20:04</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public interface IMSClientInterface {

    /**
     * 初始化
     *
     * @param serverUrlList 服务器地址列表
     * @param listener      与应用层交互的listener
     * @param callback      ims连接状态回调
     */
    void init(Vector<String> serverUrlList, OnEventListener listener, IMSConnectStatusCallback callback);

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     */
    void resetConnect();

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     * 重载
     *
     * @param isFirst 是否首次连接
     */
    void resetConnect(boolean isFirst);

    /**
     * 关闭连接，同时释放资源
     */
    void close();

    /**
     * 关闭连接，同时释放资源
     * @param isAutoReconnect  关闭后是否自动重新连接
     * 重载
     */
    void close(boolean isAutoReconnect);

    /**
     * 链路断开后是否需要自动重连（正常情况下自动重连，用户主动断开不需要重连）
     * @return
     */
    boolean isAutoReConnect();

    /**
     * 标识ims是否已关闭
     *
     * @return
     */
    boolean isClosed();

    /**
     * 发送消息
     *
     * @param msg
     */
    void sendMsg(MessageProtobuf.Msg msg);

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param isJoinTimeoutManager 是否加入发送超时管理器
     */
    void sendMsg(MessageProtobuf.Msg msg, boolean isJoinTimeoutManager);

    /**
     * 获取重连间隔时长
     *
     * @return
     */
    int getReconnectInterval();

    /**
     * 获取连接超时时长
     *
     * @return
     */
    int getConnectTimeout();

    /**
     * 获取应用在前台时心跳间隔时间
     *
     * @return
     */
    int getForegroundHeartbeatInterval();

    /**
     * 获取应用在后台时心跳间隔时间
     *
     * @return
     */
    int getBackgroundHeartbeatInterval();

    /**
     * 设置app前后台状态
     *
     * @param appStatus
     */
    void setAppStatus(int appStatus);

    /**
     * 获取由应用层构造的登录认证消息
     *
     * @return
     */
    MessageProtobuf.Msg getLoginAuthMsg();

    /**
     * 获取登录状态报告的消息
     * @param status 0:正在登录；1：登录成功；2：登录失败
     * @return
     */
    MessageProtobuf.Msg getLoginAuthStatusReportMsg(int status);

    /**
     * 获取由应用层构造的心跳消息
     *
     * @return
     */
    MessageProtobuf.Msg getHeartbeatMsg();

    /**
     * 获取应用层消息 发送 状态报告消息类型
     * (即服务端返回给客户端的消息状态报告消息类型)
     *
     * @return
     */
    int getClientSendReportMsgType();

    /**
     * 获取应用层消息 接收 状态报告消息类型
     * （即客户端接收到服务端消息后返回给服务端消息状态报告消息类型）
     *
     * @return
     */
    int getClientReceivedReportMsgType(MessageProtobuf.Msg msg);

    /**
     * 获取应用层消息发送超时重发次数
     *
     * @return
     */
    int getResendCount();

    /**
     * 获取应用层消息发送超时重发间隔
     *
     * @return
     */
    int getResendInterval();

    /**
     * 获取消息转发器
     *
     * @return
     */
    MsgDispatcher getMsgDispatcher();

    /**
     * 获取消息发送超时管理器
     *
     * @return
     */
    MsgTimeoutTimerManager getMsgTimeoutTimerManager();


}
