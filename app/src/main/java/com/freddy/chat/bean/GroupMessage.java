package com.freddy.chat.bean;

import com.freddy.chat.utils.StringUtil;

/**
 * Created by CL on 2019/5/16.
 *
 * @description:
 */

public class GroupMessage extends ContentMessage implements Cloneable {

    /*表示该条群消息是谁发送的*/
    private String sendUserId;

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof SingleMessage)) {
            return false;
        }

        return StringUtil.equals(this.msgId, ((SingleMessage) obj).getMsgId());
    }
}
