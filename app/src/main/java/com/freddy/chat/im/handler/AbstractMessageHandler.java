package com.freddy.chat.im.handler;

import com.freddy.chat.bean.AppMessage;
import com.freddy.im.constant.IMConstant;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       AbstractMessageHandler.java</p>
 * <p>@PackageName:     com.freddy.chat.im.handler</p>
 * <b>
 * <p>@Description:     抽象的MessageHandler</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/10 03:41</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public abstract class AbstractMessageHandler implements IMessageHandler {

    @Override
    public void execute(AppMessage appMessage) {
        action(appMessage);
    }

    protected  void action(AppMessage message) {
        int status = message.getHead().getStatus();
        switch (status) {
            case IMConstant.SEND_MSG_PROGRESSING:
            case IMConstant.SEND_MSG_SUCCEED:
            case IMConstant.SEND_MSG_FAILED:
                handleMessageStatusChange(message,status);
                break;

            default:
                handleNewMessageReceive(message);

        }

    }

    /***
     * 处理消息状态变更通知
     */
    protected abstract void handleNewMessageReceive(AppMessage appMessage);

    /**
     * 处理收到新消息的通知
     *
     * @param status
     */
    protected  void handleMessageStatusChange(AppMessage appMessage, int status){

    };
}
