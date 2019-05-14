package com.freddy.chat.im.handler;

import android.util.SparseArray;

import com.freddy.im.MessageType;


/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       MessageHandlerFactory.java</p>
 * <p>@PackageName:     com.freddy.chat.im.handler</p>
 * <b>
 * <p>@Description:     消息处理handler工厂</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/10 03:44</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class MessageHandlerFactory {

    private MessageHandlerFactory() {

    }

    private static final SparseArray<IMessageHandler> HANDLERS = new SparseArray<>();

    static {
        /** 单聊消息处理handler */
        HANDLERS.put(MessageType.SINGLE_CHAT, new SingleChatMessageHandler());
        /** 群聊消息处理handler */
        HANDLERS.put(MessageType.GROUP_CHAT, new GroupChatMessageHandler());
        /**朋友圈消息处理handler*/
        HANDLERS.put(MessageType.MOMENTS, new MomentsMessageHandler());
        /**系统通知消息处理handler*/
        HANDLERS.put(MessageType.SYSTEM_NOTIFY, new SystemNotifyMessageHandler());
        /**好友添加通知消息处理handler*/
        HANDLERS.put(MessageType.ADD_FRIEND, new AddFriendMessageHandler());
        /**群邀请通知消息处理handler*/
        HANDLERS.put(MessageType.GROUP_INVITE, new GroupInviteMessageHandler());


    }

    /**
     * 根据消息类型获取对应的处理handler
     *
     * @param msgType
     * @return
     */
    public static IMessageHandler getHandlerByMsgType(int msgType) {
        return HANDLERS.get(msgType);
    }
}
