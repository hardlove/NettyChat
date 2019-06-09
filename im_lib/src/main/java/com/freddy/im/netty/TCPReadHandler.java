package com.freddy.im.netty;

import android.support.annotation.NonNull;
import android.util.Log;

import com.freddy.im.MessageType;
import com.freddy.im.constant.IMConstant;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.StringUtil;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       TCPReadHandler.java</p>
 * <p>@PackageName:     com.freddy.im.netty</p>
 * <b>
 * <p>@Description:     消息接收处理handler</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/07 21:40</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class TCPReadHandler extends ChannelInboundHandlerAdapter {

    private NettyTcpClient imsClient;

    public TCPReadHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Logger.e(String.format("当前链路[%s]已经 激活 了。", ctx.channel() != null ? ctx.channel().id().asLongText() : "Unknown"));


    }

    /**
     * 当链路断开的时候会触发channelInactive这个方法，也就说触发重连的导火索是从这边开始的
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Logger.e(String.format("当前链路[%s]已经 断开 了。", ctx.channel() != null ? ctx.channel().id().asLongText() : "Unknown"));
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Logger.e(String.format("移除链路[%s]:", ctx.channel() != null ? ctx.channel().id().asLongText() : "Unknown"));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Logger.e(String.format("链路[%s]出现异常了。 error:%s", ctx.channel() != null ? ctx.channel().id().asLongText() : "Unknown", cause.getLocalizedMessage()));
        ctx.close().sync();
        imsClient.resetConnect(false);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProtobuf.Msg message = (MessageProtobuf.Msg) msg;
        if (message == null || message.getHead() == null) {
            return;
        }

        int msgType = message.getHead().getType();
        Logger.d(String.format("[channel:%s]-[收到 %s 消息  " + Utils.format(message) + "]", ctx.channel().id().asLongText(), Utils.getMessageTypeName(msgType)));


        switch (msgType) {
            //接收到回执（代表客户端发送的消息已经发送成功）
            case MessageType.SINGLE_CHAT_RECEIPT://单聊消息回执 5001
            case MessageType.GROUP_CHAT_RECEIPT://群聊消息回执  5002
            case MessageType.MOMENTS_RECEIPT://朋友圈消息回  5003
            case MessageType.SYSTEM_NOTIFY_RECEIPT://系统通知回执  5004:
            case MessageType.ADD_FRIEND_RECEIPT://好友添加回执  5008
            case MessageType.GROUP_INVITE_RECEIPT://群邀请回执  5009
            case MessageType.PC_LOGIN_RECEIPT://pc登陆回执  5010
            case MessageType.PC_KICK_OUT_RECEIPT://pc强退回执  5011
                Logger.d("收到消息回执，消息已发送成功。type:" + msgType + "  ，" + "从超时管理器移除:" + message.getHead().getMessageId());
                imsClient.getMsgTimeoutTimerManager().remove(message.getHead().getMessageId());
                // 接收消息，由消息转发器转发到应用层
                MessageProtobuf.Msg reportMsg = getClientSendReportMsg(message);
                imsClient.getMsgDispatcher().receivedMsg(reportMsg);
                break;

            // 接收到消息
            default:
                String chanel = ctx.channel().id().asLongText();
                // 收到消息后，立马给服务端回一条消息接收状态报告
                MessageProtobuf.Msg receivedReportMsg = buildReceivedReportMsg(message);
                if (receivedReportMsg != null) {
                    Logger.d("收到服务端发送过来的消息,type:" + msgType + " contentType:" + message.getHead().getContentType() + "  messageId:" + message.getHead().getMessageId() + "  ,发送消息回执：" + receivedReportMsg.getHead().getType() + " [chanel:" + chanel + "]");
                    imsClient.sendMsg(receivedReportMsg, false);
                } else {
                    Logger.e("收到服务端发送过来的消息,type:" + msgType + " contentType:" + message.getHead().getContentType() + "  messageId:" + message.getHead().getMessageId() + "   ,未找到对应的回执消息类型,无法发送消息回执！" + " [chanel:" + chanel + "]");
                }
                // 接收消息，由消息转发器转发到应用层
                imsClient.getMsgDispatcher().receivedMsg(message);
        }

    }

    /**
     * 构建消息发送成功的通知消息
     *
     * @param message 服务端发送过来的回执消息
     * @return
     */
    private MessageProtobuf.Msg getClientSendReportMsg(MessageProtobuf.Msg message) {
        int type = imsClient.getClientSendReportMsgType();
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headerBuilder = MessageProtobuf.Head.newBuilder();
        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        headerBuilder.setType(type);
        builder.setHead(headerBuilder.build());
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(IMConstant.STATUS, IMConstant.SEND_MSG_SUCCEED);
            jsonObject.put(IMConstant.TYPE, getMsgTypeByReceipt(message.getHead().getType()));//更新为回执状态对应消息类型type
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

    private int getMsgTypeByReceipt(int type) {
        int newType = -1;
        switch (type) {
            case MessageType.SINGLE_CHAT_RECEIPT://单聊消息回执 5001
                newType = MessageType.SINGLE_CHAT;
                break;
            case MessageType.GROUP_CHAT_RECEIPT://群聊消息回执  5002
                newType = MessageType.GROUP_CHAT;
                break;
            case MessageType.MOMENTS_RECEIPT://朋友圈消息回  5003
                newType = MessageType.MOMENTS;
                break;
            case MessageType.SYSTEM_NOTIFY_RECEIPT://系统通知回执  5004:
                newType = MessageType.SYSTEM_NOTIFY;
                break;
            case MessageType.ADD_FRIEND_RECEIPT://好友添加回执  5008
                newType = MessageType.ADD_FRIEND;
                break;
            case MessageType.GROUP_INVITE_RECEIPT://群邀请回执  5009
                newType = MessageType.GROUP_INVITE;
                break;
            case MessageType.PC_LOGIN_RECEIPT://pc登陆回执  5010
                newType = MessageType.PC_LOGIN;
                break;
            case MessageType.PC_KICK_OUT_RECEIPT://pc强退回执  5011
                newType = MessageType.PC_KICK_OUT;
                break;
            default:

        }
        return newType;
    }


    /**
     * 构建客户端消息接收状态报告
     *
     * @param msg
     * @return
     */
    private MessageProtobuf.Msg buildReceivedReportMsg(MessageProtobuf.Msg msg) {
        if (msg == null || msg.getHead() == null) {
            return null;
        }
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        int newType = imsClient.getClientReceivedReportMsgType(msg);//IMSEventListener.getClientReceivedReportMsgType(MessageProtobuf.Msg msg)中实现
        if (newType == -1) {
            return null;
        }
        headBuilder.setType(newType);
        headBuilder.setId(msg.getHead().getId());
        headBuilder.setToken(msg.getHead().getToken());
        headBuilder.setMessageId(msg.getHead().getMessageId());
        headBuilder.setContentType(msg.getHead().getContentType());
        headBuilder.setSource(IMConstant.SOURCE);


        builder.setHead(headBuilder.build());

        return builder.build();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state().equals(IdleState.WRITER_IDLE)){//如果写通道处于空闲状态,就发送心跳命令
//                //Logger.d("发送心跳包");
////                String token = ctx.channel().attr(IMConstant.TOKEN).get();
//                MessageProtobuf.Head head = MessageProtobuf.Head.newBuilder().setType(0).setToken("1475ae4964f9497c85f63f22c5a255ee").build();
//                MessageProtobuf.Msg msg = MessageProtobuf.Msg.newBuilder().setHead(head).build();
//                ctx.channel().writeAndFlush(msg);
//            }

        }
    }
}
