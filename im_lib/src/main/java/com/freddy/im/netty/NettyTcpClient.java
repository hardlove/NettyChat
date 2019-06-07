package com.freddy.im.netty;


import android.text.TextUtils;

import com.freddy.im.ExecutorServiceFactory;
import com.freddy.im.HeartbeatHandler;
import com.freddy.im.IMSConfig;
import com.freddy.im.MsgDispatcher;
import com.freddy.im.MsgTimeoutTimerManager;
import com.freddy.im.constant.IMConstant;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.listener.IMSConnectStatusCallback;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       NettyTcpClient.java</p>
 * <p>@PackageName:     com.freddy.im.netty</p>
 * <b>
 * <p>@Description:     基于netty实现的tcp ims</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/03/31 20:41</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class NettyTcpClient implements IMSClientInterface {

    private static volatile NettyTcpClient instance;

    private Bootstrap bootstrap;
    private Channel channel;

    private boolean isClosed = true;// 标识ims是否已关闭
    private Vector<String> serverUrlList;// ims服务器地址组
    private OnEventListener mOnEventListener;// 与应用层交互的listener
    private IMSConnectStatusCallback mIMSConnectStatusCallback;// ims连接状态回调监听器
    private MsgDispatcher msgDispatcher;// 消息转发器
    private ExecutorServiceFactory loopGroup;// 线程池工厂

    private boolean isReconnecting = false;// 是否正在进行重连
    private int connectStatus = IMSConfig.CONNECT_STATE_FAILURE;// ims连接状态，初始化为连接失败
    // 重连间隔时长
    private int reconnectInterval = IMSConfig.DEFAULT_RECONNECT_BASE_DELAY_TIME;
    // 连接超时时长
    private int connectTimeout = IMSConfig.DEFAULT_CONNECT_TIMEOUT;
    // 心跳间隔时间
    private int heartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    // 应用在前台时心跳间隔时间
    private int foregroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    // 应用在后台时心跳间隔时间
    private int backgroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_BACKGROUND;
    // app前后台状态
    private int appStatus = IMSConfig.APP_STATUS_FOREGROUND;
    // 消息发送超时重发次数
    private int resendCount = IMSConfig.DEFAULT_RESEND_COUNT;
    // 消息发送失败重发间隔时长
    private int resendInterval = IMSConfig.DEFAULT_RESEND_INTERVAL;

    private String currentHost = null;// 当前连接host
    private int currentPort = -1;// 当前连接port

    private MsgTimeoutTimerManager msgTimeoutTimerManager;
    private boolean isAutoReconnect = true;//链路断开后是否需要自动重连;
    private ResetConnectRunnable resetConnectRunnable;

    public boolean isAutoReconnect() {
        return isAutoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        isAutoReconnect = autoReconnect;
    }


    private NettyTcpClient() {
    }

    public static NettyTcpClient getInstance() {
        if (null == instance) {
            synchronized (NettyTcpClient.class) {
                if (null == instance) {
                    instance = new NettyTcpClient();
                }
            }
        }

        return instance;
    }

    /**
     * 初始化
     *
     * @param serverUrlList 服务器地址列表
     * @param listener      与应用层交互的listener
     * @param callback      ims连接状态回调
     */
    @Override
    public void init(Vector<String> serverUrlList, OnEventListener listener, IMSConnectStatusCallback callback) {
        close();
        isClosed = false;//必须重置为false
        this.serverUrlList = serverUrlList;
        this.mOnEventListener = listener;
        this.mIMSConnectStatusCallback = callback;
        msgDispatcher = new MsgDispatcher();
        msgDispatcher.setOnEventListener(listener);
        loopGroup = new ExecutorServiceFactory();
        loopGroup.initBossLoopGroup();// 初始化重连线程组
        msgTimeoutTimerManager = new MsgTimeoutTimerManager(this);

        resetConnect(true);// 进行第一次连接
    }

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     */
    @Override
    public void resetConnect() {
        this.resetConnect(false);
    }

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     * 重载
     *
     * @param isFirst 是否首次连接
     */
    @Override
    public void resetConnect(boolean isFirst) {

        // 只有第一个调用者才能赋值并调用重连
        synchronized (this) {//多个线程同时调用时会出现安全问题，必须同步
            System.out.println("=========resetConnect(boolean isFirst) 第一次连接:" + isFirst + " ================");
            if (!isFirst) {//不是第一次连接，睡一段时间后开始连接
                try {
                    Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isClosed && !isReconnecting) {
                // 先关闭channel
                System.out.println("检查之前的channel是否关闭。。。");
                closeChannel();
                // 标识正在进行重连
                isReconnecting = true;
                // 回调ims连接状态
                onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                // 执行重连任务
                if (resetConnectRunnable != null) {
                    resetConnectRunnable.stopTask();
                    resetConnectRunnable = null;
                }
                resetConnectRunnable = new ResetConnectRunnable(isFirst);
                loopGroup.execBossTask(resetConnectRunnable);
            } else {
                System.out.println("无法重连，isClosed：" + isClosed + "  isReconnecting：" + isReconnecting);
            }
        }

    }

    /**
     * 关闭连接，同时释放资源
     * 重载
     */
    @Override
    public void close(boolean isAutoReconnect) {
        if (isClosed) {
            return;
        }
        isClosed = true;
        this.isAutoReconnect = isAutoReconnect;//close 后是否需要重新连接
        System.out.println("NettyTcpClient.close().....关闭连接，同时释放资源，关闭后是否需要重新连接：" + isAutoReconnect);

        // 关闭channel
        try {
            closeChannel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 关闭bootstrap
        try {
            if (bootstrap != null) {
                bootstrap.group().shutdownGracefully();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 释放线程池
            if (loopGroup != null) {
                loopGroup.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (serverUrlList != null) {
                    serverUrlList.clear();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            isReconnecting = false;
            channel = null;
            bootstrap = null;
            loopGroup = null;
            serverUrlList = null;
        }

    }

    /**
     * 关闭连接，同时释放资源
     */
    @Override
    public void close() {
        close(true);
    }

    /**
     * 标识ims是否已关闭
     *
     * @return
     */
    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * 发送消息
     * 默认加入发送超时管理器
     *
     * @param msg
     */
    @Override
    public void sendMsg(MessageProtobuf.Msg msg) {
        this.sendMsg(msg, true);
    }

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param isJoinTimeoutManager 是否加入发送超时管理器
     */
    @Override
    public void sendMsg(MessageProtobuf.Msg msg, boolean isJoinTimeoutManager) {
        if (isClosed) {
            System.out.println("发送消息失败，IM Client 已关闭。");
            return;
        }
        if (channel == null) {
            System.out.println("[发送消息失败，channel为空\tmessage=" + msg + "]");
            return;
        }

        if (msg == null || msg.getHead() == null) {
            System.out.println("[发送消息失败，消息为空\tmessage=" + msg + "]");
            return;
        }

        if (!StringUtil.isNullOrEmpty(msg.getHead().getMessageId())) {
            if (isJoinTimeoutManager) {
                msgTimeoutTimerManager.add(msg);
            }
        }

        try {
            int type = msg.getHead().getType();
            String messageId = msg.getHead().getMessageId();
            String typeName = Utils.getMessageTypeName(type);
            System.out.println("===========================");
            if (TextUtils.isEmpty(messageId)) {
                System.out.println(String.format("[发送 %s 消息]", typeName));
            } else {
                System.out.println(String.format("[发送 %s 消息 messageId:%s][%s]", typeName, messageId, Utils.format(msg)));

            }
            System.out.println("===========================");
            channel.writeAndFlush(msg);
        } catch (Exception ex) {
            System.out.println("[发送消息失败，reason:" + ex.getMessage() + "\tmessage=" + msg + "]");
        }
    }

    /**
     * 获取重连间隔时长
     *
     * @return
     */
    @Override
    public int getReconnectInterval() {
        if (mOnEventListener != null && mOnEventListener.getReconnectInterval() > 0) {
            return reconnectInterval = mOnEventListener.getReconnectInterval();
        }

        return reconnectInterval;
    }

    /**
     * 获取连接超时时长
     *
     * @return
     */
    @Override
    public int getConnectTimeout() {
        if (mOnEventListener != null && mOnEventListener.getConnectTimeout() > 0) {
            return connectTimeout = mOnEventListener.getConnectTimeout();
        }

        return connectTimeout;
    }

    /**
     * 获取应用在前台时心跳间隔时间
     *
     * @return
     */
    @Override
    public int getForegroundHeartbeatInterval() {
        if (mOnEventListener != null && mOnEventListener.getForegroundHeartbeatInterval() > 0) {
            return foregroundHeartbeatInterval = mOnEventListener.getForegroundHeartbeatInterval();
        }

        return foregroundHeartbeatInterval;
    }

    /**
     * 获取应用在前台时心跳间隔时间
     *
     * @return
     */
    @Override
    public int getBackgroundHeartbeatInterval() {
        if (mOnEventListener != null && mOnEventListener.getBackgroundHeartbeatInterval() > 0) {
            return backgroundHeartbeatInterval = mOnEventListener.getBackgroundHeartbeatInterval();
        }

        return backgroundHeartbeatInterval;
    }

    /**
     * 设置app前后台状态
     *
     * @param appStatus
     */
    @Override
    public void setAppStatus(int appStatus) {
        this.appStatus = appStatus;
        if (this.appStatus == IMSConfig.APP_STATUS_FOREGROUND) {
            heartbeatInterval = foregroundHeartbeatInterval;
        } else if (this.appStatus == IMSConfig.APP_STATUS_BACKGROUND) {
            heartbeatInterval = backgroundHeartbeatInterval;
        }

        addHeartbeatHandler();
    }

    /**
     * 获取由应用层构造的登录认证消息
     *
     * @return
     */
    @Override
    public MessageProtobuf.Msg getLoginAuthMsg() {
        if (mOnEventListener != null) {
            return mOnEventListener.getLoginAuthMsg();
        }

        return null;
    }

    /**
     * 获取登录状态报告的消息
     *
     * @param status 0:正在登录；1：登录成功；2：登录失败
     * @return
     */
    @Override
    public MessageProtobuf.Msg getLoginAuthStatusReportMsg(int status) {
        if (mOnEventListener != null) {
            return mOnEventListener.getLoginAuthStatusReportMsg(status);
        }

        return null;
    }

    /**
     * 获取由应用层构造的心跳消息
     *
     * @return
     */
    @Override
    public MessageProtobuf.Msg getHeartbeatMsg() {
        if (mOnEventListener != null) {
            return mOnEventListener.getHeartbeatMsg();
        }

        return null;
    }

    /**
     * 获取应用层消息发送状态报告消息类型
     *
     * @return
     */
    @Override
    public int getClientSendReportMsgType() {
        if (mOnEventListener != null) {
            return mOnEventListener.getClientSendReportMsgType();
        }

        return 0;
    }

    /**
     * 获取应用层消息接收状态报告消息类型
     *
     * @return
     */
    @Override
    public int getClientReceivedReportMsgType(MessageProtobuf.Msg msg) {
        if (mOnEventListener != null) {
            return mOnEventListener.getClientReceivedReportMsgType(msg);
        }

        return 0;
    }

    /**
     * 获取心跳间隔时间
     *
     * @return
     */
    public int getHeartbeatInterval() {
        return this.heartbeatInterval;
    }

    /**
     * 获取应用层消息发送超时重发次数
     *
     * @return
     */
    @Override
    public int getResendCount() {
        if (mOnEventListener != null && mOnEventListener.getResendCount() != 0) {
            return resendCount = mOnEventListener.getResendCount();
        }

        return resendCount;
    }

    /**
     * 获取应用层消息发送超时重发间隔
     *
     * @return
     */
    @Override
    public int getResendInterval() {
        if (mOnEventListener != null && mOnEventListener.getReconnectInterval() != 0) {
            return resendInterval = mOnEventListener.getResendInterval();
        }

        return resendInterval;
    }

    /**
     * 获取线程池
     *
     * @return
     */
    public ExecutorServiceFactory getLoopGroup() {
        return loopGroup;
    }

    /**
     * 获取消息转发器
     *
     * @return
     */
    @Override
    public MsgDispatcher getMsgDispatcher() {
        return msgDispatcher;
    }

    /**
     * 获取消息发送超时定时器
     *
     * @return
     */
    @Override
    public MsgTimeoutTimerManager getMsgTimeoutTimerManager() {
        return msgTimeoutTimerManager;
    }

    @Override
    public boolean isAutoReConnect() {
        return isAutoReconnect;
    }

    /**
     * 初始化bootstrap
     */
    private void initBootstrap() {
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // 设置禁用nagle算法
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        // 设置连接超时时长
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout());
        // 设置初始化Channel
        bootstrap.handler(new TCPChannelInitializerHandler(this));
    }

    /**
     * 回调ims连接状态
     *
     * @param connectStatus
     */
    private void onConnectStatusCallback(int connectStatus) {
        this.connectStatus = connectStatus;
        switch (connectStatus) {
            case IMSConfig.CONNECT_STATE_CONNECTING: {
                System.out.println("ims连接中...");
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnecting();
                }
                break;
            }

            case IMSConfig.CONNECT_STATE_SUCCESSFUL: {
                System.out.println(String.format("ims连接成功，host『%s』, port『%s』 [channel:%s]", currentHost, currentPort, channel.id().asLongText()));
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnected();
                }


                // ims连接成功，马上先发送一条心跳消息，至于心跳机制管理，交由HeartbeatHandler
                MessageProtobuf.Msg heartbeatMsg = getHeartbeatMsg();
                if (heartbeatMsg == null) {
                    return;
                }
                System.out.println("ims连接成功======》发送心跳消息：" + Utils.format(heartbeatMsg) + " 当前心跳间隔为：" + getHeartbeatInterval() + "ms\n");
                sendMsg(heartbeatMsg, false);
                // 添加心跳消息管理handler
                addHeartbeatHandler();

                // 连接成功，发送登录认证消息
                MessageProtobuf.Msg loginAuthMsg = getLoginAuthMsg();
                if (loginAuthMsg != null) {
                    System.out.println("发送登录认证消息，message=" + loginAuthMsg);
                    sendMsg(loginAuthMsg, false);

                    //通知应用层正在登录
                    getMsgDispatcher().receivedMsg(getLoginAuthStatusReportMsg(IMConstant.LOGIN_AUTH_PROGRESSING));

                } else {
                    System.err.println("请应用层构建登录认证消息！");
                }
                break;
            }

            case IMSConfig.CONNECT_STATE_FAILURE:
            default: {
                System.out.println("ims连接失败  " + connectStatus);
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnectFailed();
                }
                break;
            }
        }
    }

    /**
     * 添加心跳消息管理handler
     */
    public void addHeartbeatHandler() {
        //添加心跳消息管理handler前先判断
        if (channel == null || !channel.isActive() || channel.pipeline() == null) {
            return;
        }

        try {
            System.out.println("添加心跳消息管理handler");
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳没响应，代表连接已断开
            channel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (channel.pipeline().get(HeartbeatHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(HeartbeatHandler.class.getSimpleName());
            }
            if (channel.pipeline().get(TCPReadHandler.class.getSimpleName()) != null) {
                channel.pipeline().addBefore(TCPReadHandler.class.getSimpleName(), HeartbeatHandler.class.getSimpleName(),
                        new HeartbeatHandler(this));
            } else {
                channel.pipeline().addLast(HeartbeatHandler.class.getSimpleName(), new HeartbeatHandler(this));
            }
        } catch (Exception e) {
            System.err.println("添加心跳消息管理handler失败，reason：" + e.getMessage());
        }
    }

    /**
     * 移除指定handler
     *
     * @param handlerName
     */
    private void removeHandler(String handlerName) {
        try {
            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("移除handler失败，handlerName=" + handlerName);
        }
    }

    /**
     * 关闭channel
     */
    private void closeChannel() {
        try {
            if (channel != null) {
                System.out.println("关闭channel:" + channel.id().asLongText());
                channel.close();
                channel.eventLoop().shutdownGracefully();
            } else {
                System.out.println("channel已关闭");
            }
        } catch (Exception e) {
            System.err.println("关闭channel出错，reason:" + e.getMessage());
        } finally {
            channel = null;
        }
    }

    /**
     * 从应用层获取网络是否可用
     *
     * @return
     */
    private boolean isNetworkAvailable() {
        if (mOnEventListener != null) {
            return mOnEventListener.isNetworkAvailable();
        }

        return false;
    }

    /**
     * 真正连接服务器的地方
     */
    private void toServer() {
        try {
            System.out.println("服务器连接中。。。");
            channel = bootstrap.connect(currentHost, currentPort).sync().channel();
        } catch (Exception e) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.err.println(String.format("连接Server(ip[%s], port[%s])失败", currentHost, currentPort));
            channel = null;
        }
    }

    /**
     * 重连任务
     */
    private class ResetConnectRunnable implements Runnable {

        private boolean isFirst;
        private boolean stop = false;

        public ResetConnectRunnable(boolean isFirst) {
            this.isFirst = isFirst;
        }

        public void stopTask() {
            stop = true;
        }

        @Override
        public void run() {
            // 非首次进行重连，执行到这里即代表已经连接失败，回调连接状态到应用层
            if (!isFirst) {
                System.out.println("=====非首次进行重连，执行到这里即代表已经连接失败，回调连接状态到应用层====");
                onConnectStatusCallback(IMSConfig.CONNECT_STATE_FAILURE);
            }

            try {
                // 重连时，释放工作线程组，也就是停止心跳
                loopGroup.destroyWorkLoopGroup();

                while (!isClosed && !stop) {
                    if (!isNetworkAvailable()) {//检查网络，如果网络不可用，一直等待，直到网络可用时才执行后面的代码
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    // 网络可用才进行连接
                    int status;
                    if ((status = reConnect()) == IMSConfig.CONNECT_STATE_SUCCESSFUL) {
                        System.out.println("ims连接成功..跳出循环。");
                        onConnectStatusCallback(status);
                        // 连接成功，跳出循环
                        break;
                    }
                    System.out.println("ims连接......status：" + status);
                    if (status == IMSConfig.CONNECT_STATE_FAILURE) {
                        onConnectStatusCallback(status);
                        try {
                            Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                // 标识重连任务停止
                isReconnecting = false;
                System.out.println("重连结束。");
            }
        }

        /**
         * 重连，首次连接也认为是第一次重连
         *
         * @return
         */
        private int reConnect() {
            System.out.println("======reConnect===========");
            // 未关闭才去连接
            if (!isClosed && !stop) {
                try {
                    // 先释放EventLoop线程组
                    if (bootstrap != null) {
                        bootstrap.group().shutdownGracefully();
                    }
                } finally {
                    bootstrap = null;
                }

                // 初始化bootstrap
                initBootstrap();
                return connectServer();
            }
            return IMSConfig.CONNECT_STATE_FAILURE;
        }

        /**
         * 连接服务器
         *
         * @return
         */
        private int connectServer() {
            System.out.println("=========connectServer()==========");
            // 如果服务器地址无效，直接回调连接状态，不再进行连接
            // 有效的服务器地址示例：127.0.0.1 8860
            if (serverUrlList == null || serverUrlList.size() == 0) {
                return IMSConfig.CONNECT_STATE_FAILURE;
            }

            for (int i = 0; (!isClosed && !stop && i < serverUrlList.size()); i++) {
                String serverUrl = serverUrlList.get(i);
                // 如果服务器地址无效，直接回调连接状态，不再进行连接
                if (StringUtil.isNullOrEmpty(serverUrl)) {
                    return IMSConfig.CONNECT_STATE_FAILURE;
                }

                String[] address = serverUrl.split(" ");
                for (int j = 1; (!isClosed && !stop && j <= IMSConfig.DEFAULT_RECONNECT_COUNT); j++) {
                    // 如果ims已关闭，或网络不可用，直接回调连接状态，不再进行连接
                    if (isClosed || stop || !isNetworkAvailable()) {
                        return IMSConfig.CONNECT_STATE_FAILURE;
                    }

                    // 回调连接状态
                    if (connectStatus != IMSConfig.CONNECT_STATE_CONNECTING) {
                        onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                    }
                    System.out.println(String.format("正在进行『%s』的第『%d』次连接，当前重连延时时长为『%dms』", serverUrl, j, j * getReconnectInterval()));

                    currentHost = address[0];// 获取host
                    currentPort = Integer.parseInt(address[1]);// 获取port
                    toServer();// 连接服务器

                    // channel不为空，即认为连接已成功
                    if (channel != null) {
                        return IMSConfig.CONNECT_STATE_SUCCESSFUL;
                    } else {
                        // 连接失败，则线程休眠n * 重连间隔时长
                        System.out.println("连接失败，线程休眠后重连");
                        try {
                            Thread.sleep(j * getReconnectInterval());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // 执行到这里，代表连接失败
            return IMSConfig.CONNECT_STATE_FAILURE;
        }


    }
}
