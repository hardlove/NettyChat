package com.freddy.chat.im;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.BaseMessage;
import com.freddy.chat.bean.ContentMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.im.handler.IMessageHandler;
import com.freddy.chat.im.handler.MessageHandlerFactory;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.im.MessageType;
import com.freddy.im.constant.IMConstant;
import com.freddy.im.protobuf.MessageProtobuf;
import com.freddy.im.protobuf.Utils;

import org.json.JSONObject;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       MessageProcessor.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     消息处理器</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/10 03:27</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class MessageProcessor implements IMessageProcessor {

    private static final String TAG = MessageProcessor.class.getSimpleName();

    private MessageProcessor() {

    }

    private static class MessageProcessorInstance {
        private static final IMessageProcessor INSTANCE = new MessageProcessor();
    }

    public static IMessageProcessor getInstance() {
        return MessageProcessorInstance.INSTANCE;
    }

    /**
     * 接收消息
     * @param message
     */
    @Override
    public void receiveMsg(final AppMessage message) {
        CThreadPoolExecutor.runInBackground(new Runnable() {

            @Override
            public void run() {
                try {
                    String messageId = message.getHead().getMessageId();

                    int msgType = message.getHead().getType();
                    switch (msgType) {
                        //接收到回执（代表客户端发送的消息已经发送成功）
                        case MessageType.SINGLE_CHAT_RECEIPT://单聊消息回执 5001
                        case MessageType.GROUP_CHAT_RECEIPT://群聊消息回执  5002
                        case MessageType.MOMENTS_RECEIPT://朋友圈消息回  5003
                            Log.e(TAG, "收到服务器消息回执，消息发送成功,message:" + message);
                            // TODO: 2019/5/13 将数据库中对应的消息状态改为 成功
                            break;

                        case MessageType.SYSTEM_NOTIFY_RECEIPT://系统通知回执  5004
                            break;
                        case MessageType.LOGIN_AUTH_STATUS_REPORT://登录状态变更报告 1000
                            String json = message.getBody().getData();
                            JSONObject jsonObject = new JSONObject(json);
                            //登录状态报告（status：0 正在登录，1 登录成功，2 登录失败）
                            int status = jsonObject.getInt(IMConstant.STATUS);
                            CEventCenter.dispatchEvent(Events.IM_LOGIN, MessageType.LOGIN_AUTH, status, null);//0 :登录失败
                            break;
                        case MessageType.ADD_FRIEND_RECEIPT://好友添加回执  5008
                        case MessageType.GROUP_INVITE_RECEIPT://群邀请回执  5009
                            break;
                        case MessageType.PC_LOGIN_RECEIPT://pc登陆回执  5010
                        case MessageType.PC_KICK_OUT_RECEIPT://pc强退回执  5011
                            break;

                        case MessageType.MSG_SENT_FAILED_REPORT://消息发送失败
                            Log.e(TAG, "消息发送失败,message:" + message);
                            // TODO: 2019/5/13 将数据库中对应的消息状态改为 失败
                            break;

                        // 接收到消息
                        default:
                            IMessageHandler messageHandler = MessageHandlerFactory.getHandlerByMsgType(message.getHead().getType());
                            if (messageHandler != null) {
                                messageHandler.execute(message);
                            } else {
                                Log.e(TAG, "未找到消息处理handler，msgType=" + message.getHead().getType());
                            }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "消息处理出错，reason=" + e.getMessage());
                }
            }
        });
    }

    /**
     * 发送消息
     *
     * @param message
     */
    @Override
    public void sendMsg(final AppMessage message) {
        CThreadPoolExecutor.runInBackground(new Runnable() {

            @Override
            public void run() {
                boolean isActive = IMSClientBootstrap.getInstance().isActive();
                if (isActive) {
                    IMSClientBootstrap.getInstance().sendMessage(MessageBuilder.getProtoBufMessageBuilderByAppMessage(message).build());
                } else {
                    Log.e(TAG, "发送消息失败");
                }
            }
        });
    }

    /**
     * 发送消息
     *
     * @param message
     */
    @Override
    public void sendMsg(ContentMessage message) {
        this.sendMsg(MessageBuilder.buildAppMessage(message));
    }

    /**
     * 发送消息
     *
     * @param message
     */
    @Override
    public void sendMsg(BaseMessage message) {
        this.sendMsg(MessageBuilder.buildAppMessage(message));
    }
}
