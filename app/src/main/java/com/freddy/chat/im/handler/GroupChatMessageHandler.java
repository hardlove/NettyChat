package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.NettyChatApp;
import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.GroupMessage;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;

import java.util.Map;

/**
 * /**
 *
 * @author CL
 * 群聊消息处理帮助类
 * type为1，2的时候，contentType 0表示撤回，1表示文本，2表示图片，3表示音频，4表示视频
 */
public class GroupChatMessageHandler extends AbstractMessageHandler {

    private static final String TAG = GroupChatMessageHandler.class.getSimpleName();



    @Override
    protected void handleNewMessageReceive(AppMessage appMessage) {
        Log.d(TAG, "action: 收到群聊消息：" + appMessage);
        int contentType = appMessage.getHead().getContentType();
        switch (contentType) {
            case 0:
                Log.d(TAG, "action: 撤销群聊消息");
                break;
            case 1:
                Log.d(TAG, "action: 文本群聊消息");
                break;
            case 2:
                Log.d(TAG, "action:图片群聊消息");
                break;
            case 3:
                Log.d(TAG, "action: 音频群聊消息");
                break;
            case 4:
                Log.d(TAG, "action: 视频群聊消息");
                break;
        }

        //需要去重
        Map<String, AppMessage> msgContainer = NettyChatApp.instance.getMsgContainer();
        if (msgContainer.containsKey(appMessage.getHead().getMessageId())) {
            Log.e(TAG, "收到重复群聊消息，messageId：" + appMessage.getHead().getMessageId());
            return;
        }
        msgContainer.put(appMessage.getHead().getMessageId(), appMessage);
        Log.e(TAG, "添加群聊消息到msgContainer,messageId:" + appMessage.getHead().getMessageId() + " 消息总数：" + msgContainer.size());


        GroupMessage msg = new GroupMessage();
        msg.setMsgId(appMessage.getHead().getMessageId());
        msg.setMsgType(appMessage.getHead().getType());
        msg.setMsgContentType(appMessage.getHead().getContentType());

        msg.setFromId(appMessage.getHead().getId());//获取该消息是重哪个群发过来的
        msg.setSendUserId(appMessage.getHead().getSendUserId());//获取该条消息是群中哪个成员发送的
        msg.setToId(appMessage.getHead().getToken());//token:接收消息时表示接收者
        msg.setTimestamp(appMessage.getHead().getTime());
        msg.setContent(appMessage.getBody().toString());


        CEventCenter.dispatchEvent(Events.CHAT_GROUP_MESSAGE, 0, 0, msg);
    }

    @Override
    protected void handleMessageStatusChange(AppMessage appMessage, int status) {

    }
}
