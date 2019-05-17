package com.freddy.chat.im;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.BaseMessage;
import com.freddy.chat.bean.ContentMessage;
import com.freddy.chat.bean.Head;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.im.handler.IMessageHandler;
import com.freddy.chat.im.handler.MessageHandlerFactory;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.chat.utils.encry.AESUtil;
import com.freddy.chat.utils.encry.HttpEncryptUtil;
import com.freddy.chat.utils.encry.KeyUtil;
import com.freddy.im.MessageType;
import com.freddy.im.constant.IMConstant;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
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
     *
     * @param appMessage
     */
    @Override
    public void receiveMsg(final AppMessage appMessage) {
        CThreadPoolExecutor.runInBackground(new Runnable() {

            @Override
            public void run() {
                try {

                    int type = appMessage.getHead().getType();
                    int contentType = -1;
                    String messageId = null;
                    IMessageHandler messageHandler = null;
                    switch (type) {
                        case MessageType.LOGIN_AUTH_STATUS_REPORT://登录状态变更报告 1000
                            handleLoginStatusChange(appMessage);
                            break;
                        //消息发送状态报告（单聊、群聊、朋友圈）
                        case MessageType.MSG_SENT_STATUS_REPORT:
                            handleMessageRecipt(type, contentType, messageId, appMessage);
                            break;
                        // 接收到新消息
                        default:
                            handleNewMessageReceive(appMessage);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "消息处理出错，reason=" + e.getMessage());
                }
            }
        });
    }

    /**
     * 处理收到的新消息（（单聊、群聊、朋友圈等）
     *
     * @param appMessage
     */
    private void handleNewMessageReceive(AppMessage appMessage) throws Exception {

        Log.d(TAG, "解密前的消息：" + appMessage);
        String prk = HttpEncryptUtil.getDecrptyPrk(appMessage.getBody().getPrk());
        String data = HttpEncryptUtil.decrptyData(prk, appMessage.getBody().getData());
        appMessage.getBody().setPrk(prk);//设置解密后的AES
        appMessage.getBody().setData(data);//设置解密后的数据
        Log.d(TAG, "解密后的消息：" + appMessage);


        IMessageHandler messageHandler;
        messageHandler = MessageHandlerFactory.getHandlerByMsgType(appMessage.getHead().getType());
        if (messageHandler != null) {
            messageHandler.execute(appMessage);
        } else {
            Log.e(TAG, "未找到消息处理handler，msgType=" + appMessage.getHead().getType());
        }
    }

    /**
     * 处理消息回执
     *
     * @param type
     * @param contentType
     * @param messageId
     * @param appMessage
     * @throws JSONException
     */
    private void handleMessageRecipt(int type, int contentType, String messageId, AppMessage appMessage) throws JSONException {
        IMessageHandler messageHandler;
        String json = appMessage.getBody().getData();
        JSONObject reportObj = new JSONObject(json);

        int status = -1;
        String id = null;
        if (reportObj.has(IMConstant.STATUS))
            status = reportObj.getInt(IMConstant.STATUS);
        if (reportObj.has(IMConstant.TYPE))
            type = reportObj.getInt(IMConstant.TYPE);//更换type
        if (reportObj.has(IMConstant.CONTENT_TYPE))
            contentType = reportObj.getInt(IMConstant.CONTENT_TYPE);
        if (reportObj.has(IMConstant.MESSAGE_ID))
            messageId = reportObj.getString(IMConstant.MESSAGE_ID);
        if (reportObj.has(IMConstant.ID))
            id = reportObj.getString(IMConstant.ID);

        String statusTip = "【Unknown】";
        if (status == IMConstant.SEND_MSG_SUCCEED) {
            statusTip = "【发送成功】";
        } else if (status == IMConstant.SEND_MSG_FAILED) {
            statusTip = "【发送失败】";
        } else if (status == IMConstant.SEND_MSG_PROGRESSING) {
            statusTip = "【正在发送】";
        }
        Log.d(TAG, String.format("接收都消息发送状态报告" + statusTip + "【type:%s  contentType:%s  messageId:%s】", type, contentType, messageId));
        // TODO: 2019/5/13 更新数据库中对应的消息状态改为
        Log.d(TAG, "更新数据库中对应的消息状态 ");

        AppMessage newAppMessage = new AppMessage();
        Head head = new Head();
        head.setStatus(status);////添加消息发送状态标示
        head.setType(type);
        head.setContentType(contentType);
        head.setMessageId(messageId);
        head.setId(id);
        newAppMessage.setHead(head);

        messageHandler = MessageHandlerFactory.getHandlerByMsgType(type);
        if (messageHandler != null) {//更新消息状态
            messageHandler.execute(newAppMessage);
        } else {
            Log.e(TAG, "未找到消息处理handler，msgType=" + newAppMessage.getHead().getType());
        }
    }

    /**
     * 处理登录状态变更
     *
     * @param appMessage
     * @throws JSONException
     */
    private void handleLoginStatusChange(AppMessage appMessage) throws JSONException {
        JSONObject loginObj = new JSONObject(appMessage.getBody().getData());
        //登录状态报告（status：0 正在登录，1 登录成功，2 登录失败）
        int loginStatus = loginObj.getInt(IMConstant.STATUS);
        CEventCenter.dispatchEvent(Events.IM_LOGIN, MessageType.LOGIN_AUTH, loginStatus, null);//0 :登录失败
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


                try {
                    String prk = HttpEncryptUtil.getEncrptyPrk();
                    String data = HttpEncryptUtil.encrptyData(message.getBody().getData());
                    Log.d(TAG, "加密前的消息：" + message);
                    message.getBody().setPrk(prk);
                    message.getBody().setData(data);
                    Log.e(TAG, "加密后的消息：" + message);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean isActive = IMSClientBootstrap.getInstance().isActive();
                if (isActive) {
                    IMSClientBootstrap.getInstance().sendMessage(MessageBuilder.getProtoBufMessageBuilderByAppMessage(message).build());
                } else {
                    Log.e(TAG, "发送消息失败,imsClient未启动。");
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
