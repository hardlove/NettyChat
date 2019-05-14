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
        Log.d(TAG, "action: 收到系统通知消息：" + message);
    }
}
