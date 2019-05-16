package com.freddy.chat.bean;

import android.util.Log;

/**
 * Created by CL on 2019/5/8.
 *
 * @description: 消息头
 */
public class Head {
    /*类型*/
    private int type;
    /*内容类型*/
    private int contentType;
    /*登陆用户的token*/
    private String token;
    /*秘钥版本*/
    private int version;
    /*接收者*/
    private  String id;
    /*发送者*/
    private String sendUserId;
    /*消息id*/
    private String messageId;
    /*发送时间*/
    private Long time;
    /*来源, android，ios，windowPc, macPc*/
    private String source;
    /*消息发送状态标志（客户端自己扩展字段）*/
    private int status;// 1：正在发送；2：发送成功；3：发送失败


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Head{" +
                "type=" + type +
                ", contentType=" + contentType +
                ", token='" + token + '\'' +
                ", version=" + version +
                ", id='" + id + '\'' +
                ", sendUserId='" + sendUserId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", time=" + time +
                ", source='" + source + '\'' +
                ", status=" + status +
                '}';
    }
}
