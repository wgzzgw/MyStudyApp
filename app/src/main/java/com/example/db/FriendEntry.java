package com.example.db;

import org.litepal.crud.DataSupport;

/**
 * Created by yy on 2018/5/14.
 */

public class FriendEntry extends DataSupport {
    public Long uid;//好友用户ID
    public String username;//好友用户名
    public String avatar;//好友头像
    public String nickName;//好友昵称
    public String noteName;//好友备注名
    public String displayName;//好友展示名
    public String user;//好友归属谁
    public String appKey;
    public String getAppKey() {
        return appKey;
    }
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getNoteName() {
        return noteName;
    }
    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public Long getUid() {
        return uid;
    }
    public void setUid(Long uid) {
        this.uid = uid;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
