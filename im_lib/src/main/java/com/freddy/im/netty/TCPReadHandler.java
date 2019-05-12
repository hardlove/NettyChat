package com.freddy.im.netty;

import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
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
        System.out.println(String.format("当前链路[%s]已经 激活 了。", ctx.channel() != null ? ctx.channel().id() : "-1"));


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
        System.err.println("====================================");
        System.err.println(String.format("当前链路[%s]已经 断开 了。", ctx.channel() != null ? ctx.channel().id() : "-1"));
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
        if (imsClient.isAutoReConnect()) {
            System.err.println("链路断开，自动重启连接。。。");
            // 触发重连
            imsClient.resetConnect(false);
        } else {
            System.out.println("用户主动断开链路，不需要重连");
        }
        System.err.println("====================================");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.err.println("====================================");
        System.err.println(String.format("当前链路[%s]出现异常了。 error:%s", ctx.channel() != null ? ctx.channel().id() : "-1", cause.getMessage()));
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
        System.err.println("出现异常，重启连接。");
        System.err.println("====================================");
        // 触发重连
        imsClient.resetConnect(false);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProtobuf.Msg message = (MessageProtobuf.Msg) msg;
        if (message == null || message.getHead() == null) {
            return;
        }

        System.out.println("====================================");
        System.out.println("收到服务端发送过来的消息：" + Utils.format(message));
        System.out.println("====================================");
        int msgType = message.getHead().getType();

        switch (msgType) {
            case 5001://单聊消息回执
            case 5002://群聊消息回执
            case 5003://朋友圈消息回
            case 5004://系统通知回执

                System.out.println("收到服务端消息发送状态报告,msyType:" + msgType + "  ，message=" + Utils.format(message) + "，从超时管理器移除");
                imsClient.getMsgTimeoutTimerManager().remove(message.getHead().getMessageId());
                break;
            default:
                // 其它消息
                // 收到消息后，立马给服务端回一条消息接收状态报告
                MessageProtobuf.Msg receivedReportMsg = buildReceivedReportMsg(message);
                System.out.println("收到服务端发送过来的消息,type:" + msgType + "  ,发送消息回执：" + receivedReportMsg.getHead().getType());
                if (receivedReportMsg != null) {
                    imsClient.sendMsg(receivedReportMsg, false);
                }

        }
        // 接收消息，由消息转发器转发到应用层( 包含服务端的消息回执)
        imsClient.getMsgDispatcher().receivedMsg(message);
    }

    /**
     * 构建客户端消息接收状态报告
     *
     * @param msgId
     * @return
     */
    private MessageProtobuf.Msg buildReceivedReportMsg(String msgId) {
        if (StringUtil.isNullOrEmpty(msgId)) {
            return null;
        }

        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMessageId(UUID.randomUUID().toString());
        headBuilder.setType(imsClient.getClientReceivedReportMsgType());
        headBuilder.setTime(System.currentTimeMillis());


        return builder.build();
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
        if (msg.getHead().getType() == 1) {
            headBuilder.setType(5001);
        } else if (msg.getHead().getType() == 2) {
            headBuilder.setType(5002);
        } else if (msg.getHead().getType() == 3) {
            headBuilder.setType(5003);
        } else if (msg.getHead().getType() == 4) {
            headBuilder.setType(5004);
        }
        headBuilder.setId(msg.getHead().getId());
        headBuilder.setToken(msg.getHead().getToken());
        headBuilder.setMessageId(msg.getHead().getMessageId());

        builder.setHead(headBuilder.build());

        return builder.build();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state().equals(IdleState.WRITER_IDLE)){//如果写通道处于空闲状态,就发送心跳命令
//                //System.out.println("发送心跳包");
////                String token = ctx.channel().attr(Constant.TOKEN).get();
//                MessageProtobuf.Head head = MessageProtobuf.Head.newBuilder().setType(0).setToken("1475ae4964f9497c85f63f22c5a255ee").build();
//                MessageProtobuf.Msg msg = MessageProtobuf.Msg.newBuilder().setHead(head).build();
//                ctx.channel().writeAndFlush(msg);
//            }

        }
    }
}
