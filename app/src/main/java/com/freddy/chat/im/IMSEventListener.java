package com.freddy.chat.im;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.freddy.chat.NettyChatApp;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.constant.IMConstant;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.protobuf.MessageProtobuf;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMSEventListener.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     与ims交互的listener</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/07 23:55</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class IMSEventListener implements OnEventListener {

    private String userId;
    private String token;

    public IMSEventListener(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    /**
     * 接收ims转发过来的消息
     *
     * @param msg
     */
    @Override
    public void dispatchMsg(MessageProtobuf.Msg msg) {
        MessageProcessor.getInstance().receiveMsg(MessageBuilder.getMessageByProtobuf(msg));
    }

    /**
     * 网络是否可用
     *
     * @return
     */
    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) NettyChatApp.sharedInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 设置ims重连间隔时长，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getReconnectInterval() {
        return IMSConfig.DEFAULT_RECONNECT_INTERVAL;
    }

    /**
     * 设置ims连接超时时长，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getConnectTimeout() {
        return IMSConfig.DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * 设置应用在前台时ims心跳间隔时长，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getForegroundHeartbeatInterval() {
        return IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    }

    /**
     * 设置应用在后台时ims心跳间隔时长，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getBackgroundHeartbeatInterval() {
        return IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_BACKGROUND;
    }

    /**
     * 构建登录认证消息
     *
     * @return
     */
    @Override
    public MessageProtobuf.Msg getLoginAuthMsg() {
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();

        headBuilder.setType(MessageType.LOGIN_AUTH);
        headBuilder.setToken(token);
        builder.setHead(headBuilder.build());

        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        builder.setBody(bodyBuilder.build());

        return builder.build();
    }

    /**
     * 获取登录状态报告的消息
     * @param status 0:正在登录；1：登录成功；2：登录失败
     * @return
     */
    @Override
    public MessageProtobuf.Msg getLoginAuthStatusReportMsg(int status) {
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();

        headBuilder.setType(MessageType.LOGIN_AUTH_STATUS_REPORT);
        headBuilder.setToken(token);
        builder.setHead(headBuilder.build());

        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(IMConstant.STATUS, status);
        } catch (JSONException e) {
            System.out.println("构建Json数据异常：" + e.getMessage());
        }
        bodyBuilder.setData(jsonObject.toString());
        builder.setBody(bodyBuilder.build());

        return builder.build();
    }

    /**
     * 构建心跳消息
     *
     * @return
     */
    @Override
    public MessageProtobuf.Msg getHeartbeatMsg() {
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMessageId(UUID.randomUUID().toString());

        headBuilder.setType(MessageType.HEARTBEAT);
        headBuilder.setSendUserId(userId);
        headBuilder.setToken(token);
        headBuilder.setTime(System.currentTimeMillis());
        headBuilder.setSource("android");

        builder.setHead(headBuilder.build());

        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        bodyBuilder.setPrk("私钥");
        bodyBuilder.setData("消息体");
        builder.setBody(bodyBuilder.build());
        return builder.build();
    }

    /**
     * 服务端返回的消息发送状态报告消息类型
     *
     * @return
     */
    @Override
    public int getServerSentReportMsgType() {
        return 0;
    }

    /**
     * 客户端提交的消息接收状态报告消息类型
     *
     * @return
     */
    @Override
    public int getClientReceivedReportMsgType() {
        return 0;
    }

    /**
     * 设置ims消息发送超时重发次数，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getResendCount() {
        return 0;
    }

    /**
     * 设置ims消息发送超时重发间隔时长，0表示默认使用ims的值
     *
     * @return
     */
    @Override
    public int getResendInterval() {
        return 0;
    }


}
