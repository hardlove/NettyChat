package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.NettyChatApp;
import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.im.constant.IMConstant;

import java.util.Map;

/**
 * @author CL
 *单聊消息处理帮助类
 * type为1，2的时候，contentType 0表示撤回，1表示文本，2表示图片，3表示音频，4表示视频
 */
public class SingleChatMessageHandler extends AbstractMessageHandler {

    private static final String TAG = SingleChatMessageHandler.class.getSimpleName();

    @Override
    protected void handleNewMessageReceive(AppMessage appMessage) {
        Log.d(TAG, "action: 收到单聊消息：" + appMessage);
        int contentType = appMessage.getHead().getContentType();
        switch (contentType) {
            case 0:
                Log.d(TAG, "action: 撤销单聊消息");
                break;
            case 1:
                Log.d(TAG, "action: 文本单聊消息");
                break;
            case 2:
                Log.d(TAG, "action:图片单聊消息");
                break;
            case 3:
                Log.d(TAG, "action: 音频单聊消息");
                break;
            case 4:
                Log.d(TAG, "action: 视频单聊消息");
                break;
        }

        //需要去重
        Map<String, String> msgContainer = NettyChatApp.instance.getMsgContainer();
        if (msgContainer.containsKey(appMessage.getHead().getMessageId())) {
            Log.e(TAG, "收到重复单聊消息，messageId：" + appMessage.getHead().getMessageId());
            return;
        }
        msgContainer.put(appMessage.getHead().getMessageId(), appMessage.getHead().getMessageId());
        Log.e(TAG, "添加单聊消息到msgContainer,messageId:" + appMessage.getHead().getMessageId() + " 消息总数：" + msgContainer.size());


        SingleMessage msg = new SingleMessage();
        msg.setMsgId(appMessage.getHead().getMessageId());
        msg.setMsgType(appMessage.getHead().getType());
        msg.setMsgContentType(appMessage.getHead().getContentType());
        msg.setFromId(appMessage.getHead().getId());//id:接收消息时，表示发送者id
        msg.setToId(appMessage.getHead().getToken());//token:接收消息时表示接收者
        msg.setTimestamp(appMessage.getHead().getTime());
        msg.setContent(appMessage.getBody().toString());


        CEventCenter.dispatchEvent(Events.CHAT_SINGLE_MESSAGE, 0, 0, msg);

    }

    @Override
    protected void handleMessageStatusChange(AppMessage appMessage, int status) {
        Log.d(TAG, "handleMessageStatusChange: 更新单聊消息状态： status：" + status);

    }


}
