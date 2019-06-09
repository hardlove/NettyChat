package com.freddy.im;

import android.util.Log;

import com.freddy.im.constant.IMConstant;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelOutboundBuffer;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       MsgTimeoutTimer.java</p>
 * <p>@PackageName:     com.freddy.im</p>
 * <b>
 * <p>@Description:     消息发送超时定时器，每一条消息对应一个定时器</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/09 22:38</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class MsgTimeoutTimer extends Timer {

    private IMSClientInterface imsClient;// ims客户端
    private MessageProtobuf.Msg msg;// 发送的消息
    private int currentResendCount;// 当前重发次数
    private MsgTimeoutTask task;// 消息发送超时任务

    public MsgTimeoutTimer(IMSClientInterface imsClient, MessageProtobuf.Msg msg) {
        this.imsClient = imsClient;
        this.msg = msg;
        task = new MsgTimeoutTask();
        this.schedule(task, imsClient.getReconnectInterval(), imsClient.getReconnectInterval());
    }

    /**
     * 消息发送超时任务
     */
    private class MsgTimeoutTask extends TimerTask {

        @Override
        public void run() {
            if (imsClient.isClosed()) {
                if (imsClient.getMsgTimeoutTimerManager() != null) {
                    System.out.print("imsClient.isClosed(),从发送消息管理器移除消息："+Utils.format(msg));
                    imsClient.getMsgTimeoutTimerManager().remove(msg.getHead().getMessageId());
                }

                return;
            }

            currentResendCount++;
            if (currentResendCount > imsClient.getResendCount()) {
                // 重发次数大于可重发次数，直接标识为发送失败，并通过消息转发器通知应用层
                try {
                    Logger.e("消息发送3次都失败，通知应用层，msg：" + Utils.format(msg));
                    // 通知应用层消息发送失败
                    imsClient.getMsgDispatcher().receivedMsg(getClientSendReportMsg(msg));
                } catch (Error error) {
                    Logger.e("重发消息异常，断开连接。。。。，msg：" + Utils.format(msg) + "   error:" + error.getLocalizedMessage());
                } finally {
                    // 从消息发送超时管理器移除该消息
                    imsClient.getMsgTimeoutTimerManager().remove(msg.getHead().getMessageId());
                    // 执行到这里，认为连接已断开或不稳定，触发重连
                    Logger.e("重发失败，从发送消息管理器移除消息, close Channel,触发重连。。。。");
//                    imsClient.resetConnect(false);
                    currentResendCount = 0;
                }
            } else {
                // 发送消息，但不再加入超时管理器，达到最大发送失败次数就算了
                sendMsg();
            }
        }
    }

    public void sendMsg() {
        Logger.d("正在重发消息，messageId:" + msg.getHead().getMessageId());
        imsClient.sendMsg(msg, false);
    }

    public MessageProtobuf.Msg getMsg() {
        return msg;
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        super.cancel();
    }

    /**
     * 构建消息发送失败的通知消息
     * @param message 客户端发送的消息
     * @return
     */
    private MessageProtobuf.Msg getClientSendReportMsg(MessageProtobuf.Msg message)  {
        int type = imsClient.getClientSendReportMsgType();
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headerBuilder = MessageProtobuf.Head.newBuilder();
        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        headerBuilder.setType(type);
        builder.setHead(headerBuilder.build());
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(IMConstant.STATUS, IMConstant.SEND_MSG_SUCCEED);
            jsonObject.put(IMConstant.TYPE, message.getHead().getType());
            jsonObject.put(IMConstant.CONTENT_TYPE, message.getHead().getContentType());
            jsonObject.put(IMConstant.MESSAGE_ID, message.getHead().getMessageId());
            jsonObject.put(IMConstant.ID, message.getHead().getId());
            bodyBuilder.setData(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e("getClientSendReportMsg（），构建Json消息失败，message:" + Utils.format(message));

        }
        builder.setBody(bodyBuilder.build());
        return builder.build();

    }
}
