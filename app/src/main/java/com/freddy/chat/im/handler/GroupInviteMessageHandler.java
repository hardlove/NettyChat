package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;

/**
 * Created by CL on 2019/5/14.
 *
 * @description: 群邀请通知消息帮助类
 */

public class GroupInviteMessageHandler extends AbstractMessageHandler {
    private static final String TAG = GroupInviteMessageHandler.class.getSimpleName();
    @Override
    protected void action(AppMessage message) {
        Log.d(TAG, "action: 收到群邀请通知消息：" + message);
    }
}
