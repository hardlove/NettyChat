package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;
import com.orhanobut.logger.Logger;

/**
 * Created by CL on 2019/5/14.
 *
 * @description: 好友添加通知消息帮助类
 */

public class AddFriendMessageHandler extends AbstractMessageHandler {
    private static final String TAG = AddFriendMessageHandler.class.getSimpleName();

    @Override
    protected void handleNewMessageReceive(AppMessage appMessage) {
        Logger.d("action: 收到好友添加通知消息：" + appMessage);

    }


}
