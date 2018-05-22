package com.example.message;

import java.util.UUID;

import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.commons.models.IUser;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by yy on 2018/5/19.
 */
/*
* 展示和解析消息的类，必须要实现IMessage接口
* */
public class MyMessage implements IMessage {
    private long id;//消息ID
    private String text;//消息内容
    private String timeString;//消息时间 时间戳
    private MessageType type;//消息类型
    private IUser user;//获取消息发送者
    private String mediaFilePath;//媒体文件路径
    private long duration;//持续时间
    private String progress;//进度
    private Message message;//消息
    private int position;
    private long msgID;
    public MyMessage(String text, MessageType type) {
        this.text = text;
        this.type = type;
        //产生随机数，很少会重复
        this.id = UUID.randomUUID().getLeastSignificantBits();
    }
    public MyMessage(){
        this.id = UUID.randomUUID().getLeastSignificantBits();
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getMsgID() {
        return msgID;
    }

    public void setMsgID(long msgID) {
        this.msgID = msgID;
    }

    @Override
    public String getMsgId() {
        return String.valueOf(id);
    }

    @Override
    public IUser getFromUser() {
        if (user == null) {
            return new DefaultUser("0", "user1", null);
        }
        return user;
    }

    public void setUserInfo(IUser user) {
        this.user = user;
    }

    public void setMediaFilePath(String path) {
        this.mediaFilePath = path;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    @Override
    public String getProgress() {
        return progress;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    @Override
    public String getTimeString() {
        return timeString;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public MessageStatus getMessageStatus() {
        return MessageStatus.SEND_SUCCEED;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getMediaFilePath() {
        return mediaFilePath;
    }

    @Override
    public String toString() {
        return "MyMessage{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", timeString='" + timeString + '\'' +
                ", type=" + type +
                ", user=" + user +
                ", mediaFilePath='" + mediaFilePath + '\'' +
                ", duration=" + duration +
                ", progress='" + progress + '\'' +
                ", position=" + position +
                ", msgID=" + msgID +
                '}';
    }

}
