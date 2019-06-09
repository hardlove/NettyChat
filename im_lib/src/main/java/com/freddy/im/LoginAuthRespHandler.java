package com.freddy.im;

import com.freddy.im.constant.IMConstant;
import com.freddy.im.netty.NettyTcpClient;
import com.freddy.im.protobuf.MessageProtobuf;
import com.orhanobut.logger.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       LoginAuthRespHandler.java</p>
 * <p>@PackageName:     com.freddy.im</p>
 * <b>
 * <p>@Description:     登录认证消息响应处理handler</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/07 23:11</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    private NettyTcpClient imsClient;

    public LoginAuthRespHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProtobuf.Msg loginAuthMsg = (MessageProtobuf.Msg) msg;
        if (loginAuthMsg == null || loginAuthMsg.getHead() == null) {
            return;
        }

        //获取客户端构建的登录认证消息
        MessageProtobuf.Msg handshakeMsg = imsClient.getLoginAuthMsg();
        if (handshakeMsg == null || handshakeMsg.getHead() == null) {
            return;
        }

        if (MessageType.LOGIN_AUTH_SUCCEED_RECEIPT == loginAuthMsg.getHead().getType()||5==loginAuthMsg.getHead().getType()) {
            Logger.d("收到服务端登录认证响应消息，登录成功。");

            // 登录认证成功，检查消息发送超时管理器里是否有发送超时的消息，如果有，则全部重发
            Logger.d("检查是否有发送超时的消息，如果有，则全部重发");
            imsClient.getMsgTimeoutTimerManager().onResetConnected();

            //通知应用层登录成
            Logger.d("通知应用层登录成功。");
            imsClient.getMsgDispatcher().receivedMsg(imsClient.getLoginAuthStatusReportMsg(IMConstant.LOGIN_AUTH_SUCCEED));
        } else if (MessageType.LOGIN_AUTH_FAILED_RECEIPT == loginAuthMsg.getHead().getType()) {
            Logger.d("收到服务端登录认证响应消息，登录失败。 message=" + loginAuthMsg);
            //通知应用层登录失败
            Logger.d("通知应用层登录失败。");
            imsClient.getMsgDispatcher().receivedMsg(imsClient.getLoginAuthStatusReportMsg(IMConstant.LOGIN_AUTH_FAILED));

            Logger.d("登录认证失败，close Client");
            imsClient.close();
        } else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }
    }
}
