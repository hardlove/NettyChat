package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;

/**
 * @author CL
 * 朋友圈消息处理帮助类
 */
public class MomentsMessageHandler extends AbstractMessageHandler {

    private static final String TAG = MomentsMessageHandler.class.getSimpleName();

    @Override
    protected void action(AppMessage message) {
        Log.d(TAG, "收到朋友圈消息，message=" + message);
    }
}
