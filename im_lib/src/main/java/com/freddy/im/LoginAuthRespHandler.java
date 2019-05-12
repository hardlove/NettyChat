package com.freddy.im;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.netty.NettyTcpClient;
import com.freddy.im.protobuf.MessageProtobuf;

import java.util.EventObject;
import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       LoginAuthRespHandler.java</p>
 * <p>@PackageName:     com.freddy.im</p>
 * <b>
 * <p>@Description:     握手认证消息响应处理handler</p>
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
        MessageProtobuf.Msg handshakeRespMsg = (MessageProtobuf.Msg) msg;
        if (handshakeRespMsg == null || handshakeRespMsg.getHead() == null) {
            return;
        }

        //获取客户端构建的握手（登录）消息
        MessageProtobuf.Msg handshakeMsg = imsClient.getHandshakeMsg();
        if (handshakeMsg == null || handshakeMsg.getHead() == null) {
            return;
        }

        int handshakeMsgType = handshakeMsg.getHead().getType();
        if (5006 == handshakeRespMsg.getHead().getType()) {
            System.out.println("收到服务端握手响应消息，登录成功。 message=" + handshakeRespMsg);
            // 握手成功，马上先发送一条心跳消息，至于心跳机制管理，交由HeartbeatHandler
            MessageProtobuf.Msg heartbeatMsg = imsClient.getHeartbeatMsg();
            if (heartbeatMsg == null) {
                return;
            }
            System.out.println("登录成功======》发送心跳消息：" + heartbeatMsg + "当前心跳间隔为：" + imsClient.getHeartbeatInterval() + "ms\n");
            imsClient.sendMsg(heartbeatMsg);
            // 添加心跳消息管理handler
            imsClient.addHeartbeatHandler();

            // 握手成功，检查消息发送超时管理器里是否有发送超时的消息，如果有，则全部重发
            imsClient.getMsgTimeoutTimerManager().onResetConnected();
            System.out.println("检查是否有发送超时的消息，如果有，则全部重发");


            //通知应用层登录成功
            imsClient.getMsgDispatcher().receivedMsg(handshakeRespMsg);
        } else if (5005 == handshakeRespMsg.getHead().getType()) {
            System.out.println("收到服务端握手响应消息，登录失败。 message=" + handshakeRespMsg);
            imsClient.resetConnect(false);// 握手失败，触发重连
            //通知应用层登录失败
            imsClient.getMsgDispatcher().receivedMsg(handshakeRespMsg);
        } else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }
    }
}
