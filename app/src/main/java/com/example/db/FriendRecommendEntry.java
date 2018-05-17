package com.example.db;

import org.litepal.crud.DataSupport;

/**
 * Created by yy on 2018/5/15.
 */
/*
* 申请的好友
* */
public class FriendRecommendEntry extends DataSupport{
    private long uid;
    private String username;//邀请者
    private String notename;//邀请者
    private String nickname;//邀请者
    private String appkey;//邀请者
    private String avatar;//邀请者
    private String displayname;//邀请者
    private String sendname;//被邀请者名字

    public String getSendname() {
        return sendname;
    }
    public void setSendname(String sendname) {
        this.sendname = sendname;
    }
    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNotename() {
        return notename;
    }

    public void setNotename(String notename) {
        this.notename = notename;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
