package com.freddy.chat.im;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.BaseMessage;
import com.freddy.chat.bean.Body;
import com.freddy.chat.bean.ContentMessage;
import com.freddy.chat.bean.Head;
import com.freddy.chat.utils.StringUtil;
import com.freddy.im.protobuf.MessageProtobuf;

/**
 * <p>@ProjectName:     BoChat</p>
 * <p>@ClassName:       MessageBuilder.java</p>
 * <p>@PackageName:     com.bochat.app.message</p>
 * <b>
 * <p>@Description:     消息转换</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/02/07 17:26</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class MessageBuilder {

    /**
     * 根据聊天消息，生成一条可以能够传输通讯的消息
     *
     * @param msgId
     * @param type
     * @param subType
     * @param fromId
     * @param toId
     * @param extend
     * @param content
     * @return
     */
    public static AppMessage buildAppMessage(String msgId, int type, int subType, String fromId,
                                             String toId, String extend, String content) {
        AppMessage message = new AppMessage();
        Head head = new Head();
        head.setMessageId(msgId);
        head.setType(type);
        head.setContentType(subType);
        head.setSendUserId(fromId);
        head.setId(toId);
        head.setTime(System.currentTimeMillis());

        message.setHead(head);

        Body body = new Body();
        body.setData(content);
        body.setPrk("私钥");
        message.setBody(body);

        return message;
    }

    /**
     * 根据聊天消息，生成一条可以能够传输通讯的消息
     *
     * @param msg
     * @return
     */
    public static AppMessage buildAppMessage(ContentMessage msg) {
        AppMessage message = new AppMessage();
        Head head = new Head();
        head.setMessageId(msg.getMsgId());
        head.setType(msg.getMsgType());
        head.setContentType(msg.getMsgContentType());
        head.setSendUserId(msg.getFromId());
        head.setId(msg.getToId());
        head.setTime(msg.getTimestamp());
        head.setToken("1475ae4964f9497c85f63f22c5a255ee");

        message.setHead(head);

        Body body = new Body();
        body.setData(msg.getContent());
        body.setPrk("私钥");
        message.setBody(body);

        return message;
    }

    /**
     * 根据聊天消息，生成一条可以能够传输通讯的消息
     *
     * @param msg
     * @return
     */
    public static AppMessage buildAppMessage(BaseMessage msg) {
        AppMessage message = new AppMessage();
        Head head = new Head();
        head.setMessageId(msg.getMsgId());
        head.setType(msg.getMsgType());
        head.setContentType(msg.getMsgContentType());
        head.setSendUserId(msg.getFromId());
        head.setId(msg.getToId());
        head.setTime(msg.getTimestamp());

        message.setHead(head);

        Body body = new Body();
        body.setData(msg.getContent());
        body.setPrk("私钥");
        message.setBody(body);

        return message;
    }

    /**
     * 根据业务消息对象获取protoBuf消息对应的builder
     *
     * @param message
     * @return
     */
    public static MessageProtobuf.Msg.Builder getProtoBufMessageBuilderByAppMessage(AppMessage message) {
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();

        headBuilder.setId(message.getHead().getId());
        headBuilder.setMessageId(message.getHead().getMessageId());
        headBuilder.setTime(message.getHead().getTime());
        headBuilder.setToken(message.getHead().getToken());
        headBuilder.setSource(message.getHead().getSource());
        headBuilder.setContentType(message.getHead().getContentType());
        headBuilder.setType(message.getHead().getType());

        builder.setHead(headBuilder);

        MessageProtobuf.Body.Builder bodyBuilder = MessageProtobuf.Body.newBuilder();
        bodyBuilder.setPrk(message.getBody().getPrk());
        bodyBuilder.setData(message.getBody().getData());

        builder.setBody(bodyBuilder);

        return builder;
    }

    /**
     * 通过protobuf消息对象获取业务消息对象
     *
     * @param protobufMessage
     * @return
     */
    public static AppMessage getMessageByProtobuf(
            MessageProtobuf.Msg protobufMessage) {
        AppMessage message = new AppMessage();
        Head head = new Head();
        Body body = new Body();
        MessageProtobuf.Head protoHead = protobufMessage.getHead();
        MessageProtobuf.Body protoBody = protobufMessage.getBody();

        head.setType(protoHead.getType());
        head.setContentType(protoHead.getContentType());
        head.setMessageId(protoHead.getMessageId());
        head.setId(protoHead.getId());
        head.setTime(protoHead.getTime());
        head.setSendUserId(protoHead.getSendUserId());
        head.setToken(protoHead.getToken());
        head.setSource(protoHead.getSource());

        body.setPrk(protoBody.getPrk());
        body.setData(protoBody.getData());

        message.setHead(head);
        message.setBody(body);


        return message;
    }
}
