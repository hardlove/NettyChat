package com.freddy.im;

import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import java.util.Timer;
import java.util.TimerTask;

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
                    MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
                    MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
                    headBuilder.setMessageId(msg.getHead().getMessageId());
                    headBuilder.setType(imsClient.getServerSentReportMsgType());
                    headBuilder.setTime(System.currentTimeMillis());
//                    headBuilder.setStatusReport(IMSConfig.DEFAULT_REPORT_SERVER_SEND_MSG_FAILURE);
                    builder.setHead(headBuilder.build());

                    System.err.println("消息发送3次都失败，msg：" + Utils.format(msg));
                    // 通知应用层消息发送失败
                    imsClient.getMsgDispatcher().receivedMsg(builder.build());
                } catch (Error error) {
                    System.err.println("重发消息异常，断开连接。。。。，msg：" + Utils.format(msg) + "   error:" + error.getLocalizedMessage());
                } finally {

                    // 从消息发送超时管理器移除该消息
                    imsClient.getMsgTimeoutTimerManager().remove(msg.getHead().getMessageId());
                    // 执行到这里，认为连接已断开或不稳定，触发重连
                    System.err.println("从发送消息管理器移除消息, 重新连接。。。。");
                    imsClient.resetConnect(false);
                    currentResendCount = 0;
                }
            } else {
                // 发送消息，但不再加入超时管理器，达到最大发送失败次数就算了
                sendMsg();
            }
        }
    }

    public void sendMsg() {
        System.out.println("正在重发消息，message=" + Utils.format(msg));
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
}
