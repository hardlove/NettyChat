package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;
import com.orhanobut.logger.Logger;

/**
 * @author CL
 * 朋友圈消息处理帮助类
 * 当type为3的时候，contentType 0表示：点赞，1表示评论
 */
public class MomentsMessageHandler extends AbstractMessageHandler {

    private static final String TAG = MomentsMessageHandler.class.getSimpleName();


    @Override
    protected void handleNewMessageReceive(AppMessage appMessage) {
        Logger.d("action: 收到朋友圈消息："+appMessage);
        int contentType = appMessage.getHead().getContentType();
        switch (contentType) {
            case 0:
                Logger.d("action: 点赞消息");
                break;
            case 1:
                Logger.d("action: 评论消息");
                break;

        }
    }

    @Override
    protected void handleMessageStatusChange(AppMessage appMessage, int status) {

    }
}
