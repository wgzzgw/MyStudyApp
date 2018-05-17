package com.example.db;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by yy on 2018/5/14.
 */
/*
* 用户
* */
public class UserEntry extends DataSupport{
    private int id;
    public String username;//用户名
    public String appKey;
    public List<FriendEntry> friendEntryList;
    public List<FriendEntry> getFriendEntryList() {
        return friendEntryList;
    }
    public void setFriendEntryList(List<FriendEntry> friendEntryList) {
        this.friendEntryList = friendEntryList;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getAppKey() {
        return appKey;
    }
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
