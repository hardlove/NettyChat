package com.freddy.chat.im.handler;

import android.util.Log;

import com.freddy.chat.NettyChatApp;
import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.im.MessageType;
import com.freddy.im.constant.IMConstant;
import com.orhanobut.logger.Logger;

import java.util.Map;

/**
 * @author CL
 * 系统通知消息处理帮助类
 * 当type为4的时候，contentType 0表示消息，1表示踢下线
 */
public class SystemNotifyMessageHandler extends AbstractMessageHandler {

    private static final String TAG = SystemNotifyMessageHandler.class.getSimpleName();



    /**
     * 处理其他系统通知消息
     * @param message
     */
    private void handleSystemNotify(AppMessage message) {
        Logger.d("action: 收到系统通知消息：" + message);
        int contentType = message.getHead().getContentType();
        if (contentType == 0) {//0:表示消息
            handleSystemNotify(message);
        } else if (contentType==1) {//1:表示被踢下线
            CEventCenter.dispatchEvent(Events.IM_LOGIN, MessageType.LOGIN_AUTH, IMConstant.LOGIN_AUTH_KICK_OUT, null);//3 :被踢下线
            Logger.d("被踢下线了。。。");
            IMSClientBootstrap.getInstance().closeImsClient();//关闭ImsClient，否则会进行重连
        }
    }

    @Override
    protected void handleNewMessageReceive(AppMessage appMessage) {
        Logger.d("action: 收到系统通知消息：" + appMessage);
        int contentType = appMessage.getHead().getContentType();
        if (contentType == 0) {//0:表示消息
            handleSystemNotify(appMessage);
        } else if (contentType==1) {//1:表示被踢下线
            CEventCenter.dispatchEvent(Events.IM_LOGIN, MessageType.LOGIN_AUTH, IMConstant.LOGIN_AUTH_KICK_OUT, null);//3 :被踢下线
            Logger.d("被踢下线了。。。");
            IMSClientBootstrap.getInstance().closeImsClient();//关闭ImsClient，否则会进行重连
        }
    }


}
