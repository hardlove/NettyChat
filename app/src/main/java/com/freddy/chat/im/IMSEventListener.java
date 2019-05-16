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
        MessageProcessor.getInstance().receiveMsg(MessageBuilder.getAppMessageByProtobuf(msg));
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
        headBuilder.setSource(IMConstant.SOURCE);
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
        headBuilder.setId(userId);
        headBuilder.setToken(token);
        headBuilder.setSource(IMConstant.SOURCE);
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

        headBuilder.setId(userId);
        headBuilder.setToken(token);
        headBuilder.setType(MessageType.HEARTBEAT);
        headBuilder.setSource(IMConstant.SOURCE);

        builder.setHead(headBuilder.build());

        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        builder.setBody(bodyBuilder.build());
        return builder.build();
    }

    /**
     * 服务端返回的消息发送状态报告消息类型
     *
     * @return
     */
    @Override
    public int getClientSendReportMsgType() {
        return MessageType.MSG_SENT_STATUS_REPORT;
    }

    /**
     * 客户端提交的消息接收状态报告消息类型
     *
     * @return
     */
    @Override
    public int getClientReceivedReportMsgType(MessageProtobuf.Msg msg) {
        if (msg == null || msg.getHead() == null) {
            return -1;
        }
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        int type = msg.getHead().getType();
        int newType = -1;
        switch (type) {
            case MessageType.SINGLE_CHAT:
                //单聊消息回执
                newType = MessageType.SINGLE_CHAT_RECEIPT;
                break;
            case MessageType.GROUP_CHAT:
                //群聊消息回执
                newType = MessageType.GROUP_CHAT_RECEIPT;
                break;
            case MessageType.MOMENTS:
                //朋友圈消息回执
                newType = MessageType.MOMENTS_RECEIPT;
                break;
            case MessageType.SYSTEM_NOTIFY:
                //系统通知回执
                newType = MessageType.SYSTEM_NOTIFY_RECEIPT;
                break;
            case MessageType.ADD_FRIEND:
                //好友添加回执
                newType = MessageType.ADD_FRIEND_RECEIPT;
                break;
            case MessageType.GROUP_INVITE:
                //群邀请回执
                newType = MessageType.GROUP_INVITE_RECEIPT;
                break;
            case MessageType.PC_LOGIN:
                //pc登陆回执
                newType = MessageType.PC_LOGIN_RECEIPT;
                break;
            case MessageType.PC_KICK_OUT:
                //pc强退回执
                newType = MessageType.PC_KICK_OUT_RECEIPT;
                break;
        }

        return newType;
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
