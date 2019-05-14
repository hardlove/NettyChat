package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.bean.AppMessage;

/**
 * /**
 *
 * @author CL
 *         群聊消息处理帮助类
 *         type为1，2的时候，contentType 0表示撤回，1表示文本，2表示音频，3表示视频
 */
public class GroupChatMessageHandler extends AbstractMessageHandler {

    private static final String TAG = GroupChatMessageHandler.class.getSimpleName();

    @Override
    protected void action(AppMessage message) {
        Log.d(TAG, "action: 收到群聊消息：" + message);
        int contentType = message.getHead().getContentType();
        switch (contentType) {
            case 0:
                Log.d(TAG, "action: 撤销群聊消息");
                break;
            case 1:
                Log.d(TAG, "action: 文本群聊消息");
                break;
            case 2:
                Log.d(TAG, "action: 音频群聊消息");
                break;
            case 3:
                Log.d(TAG, "action: 视频群聊消息");
                break;
        }
    }
}
