package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       SingleChatMessageHandler.java</p>
 * <p>@PackageName:     com.freddy.chat.im.handler</p>
 * <b>
 * <p>@Description:     类描述</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/10 03:43</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class SingleChatMessageHandler extends AbstractMessageHandler {

    private static final String TAG = SingleChatMessageHandler.class.getSimpleName();

    @Override
    protected void action(AppMessage message) {
        Log.d(TAG, "收到单聊消息，message=" + message);

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
