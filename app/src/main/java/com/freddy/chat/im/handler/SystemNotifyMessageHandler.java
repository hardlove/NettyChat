package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.NettyChatApp;
import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;

import java.util.Map;

/**
 * @author CL
 * 系统通知消息处理帮助类
 */
public class SystemNotifyMessageHandler extends AbstractMessageHandler {

    private static final String TAG = SystemNotifyMessageHandler.class.getSimpleName();

    @Override
    protected void action(AppMessage message) {

        //需要去重
        Map<String, AppMessage> msgContainer = NettyChatApp.instance.getMsgContainer();
        if (msgContainer.containsKey(message.getHead().getMessageId())) {
            Log.e(TAG, "收到重复单聊消息，messageId：" + message.getHead().getMessageId());
            return;
        }
        msgContainer.put(message.getHead().getMessageId(), message);
        Log.e(TAG, "添加单聊消息到msgContainer,messageId:" + message.getHead().getMessageId() + " 消息总数：" + msgContainer.size());


        SingleMessage msg = new SingleMessage();
        msg.setMsgId(message.getHead().getMessageId());
        msg.setMsgType(message.getHead().getType());
        msg.setMsgContentType(message.getHead().getContentType());
        msg.setFromId(message.getHead().getSendUserId());
        msg.setToId(message.getHead().getId());
        msg.setTimestamp(message.getHead().getTime());
        msg.setContent(message.getBody().toString());


        CEventCenter.dispatchEvent(Events.CHAT_SINGLE_MESSAGE, 0, 0, msg);
    }
}
