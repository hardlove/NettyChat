package com.freddy.im;

import com.freddy.im.netty.NettyTcpClient;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       HeartbeatHandler.java</p>
 * <p>@PackageName:     com.freddy.im</p>
 * <b>
 * <p>@Description:     心跳任务管理器</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/08 01:34</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private NettyTcpClient imsClient;
    public HeartbeatHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE: {//读超时. 即当在指定的事件间隔内没有从 Channel 读取到数据时, 会触发一个 READER_IDLE 的 IdleStateEvent 事件.
                    // 规定时间内没收到服务端心跳包响应，进行重连操作
                    System.out.println("规定时间内没收到服务端心跳包响应，进行重连操作。。。");
                    imsClient.resetConnect(false);
                    break;
                }

                case WRITER_IDLE: {//写超时. 即当在指定的事件间隔内没有数据写入到 Channel 时, 会触发一个 WRITER_IDLE 的 IdleStateEvent 事件.
                    // 规定时间内没向服务端发送心跳包，即发送一个心跳包
                    if (heartbeatTask == null) {
                        heartbeatTask = new HeartbeatTask(ctx);
                    }

                    imsClient.getLoopGroup().execWorkTask(heartbeatTask);
                    break;

                }
                case ALL_IDLE://读/写超时. 即当在指定的事件间隔内没有读或写操作时, 会触发一个 ALL_IDLE 的 IdleStateEvent 事件.
                    System.out.println("在指定的事件间隔内没有读或写操作时,触发IdleState.ALL_IDLE状态");
                    break;
            }
        }
    }

    private HeartbeatTask heartbeatTask;
    private class HeartbeatTask implements Runnable {

        private ChannelHandlerContext ctx;

        public HeartbeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (ctx.channel().isActive()) {
                MessageProtobuf.Msg heartbeatMsg = imsClient.getHeartbeatMsg();
                if (heartbeatMsg == null) {
                    return;
                }

                imsClient.sendMsg(heartbeatMsg,false);

            }
        }
    }
}
