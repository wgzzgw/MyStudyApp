package com.example.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.R;
import com.example.activity.FriendRecommendActivity;
import com.example.activity.SearchForAddFriendActivity;
import com.example.adapter.ListAdapter;
import com.example.db.FriendEntry;
import com.example.db.UserEntry;
import com.example.view.ContactsView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/14.
 */
/*
* 通讯录布局事件控制器
* */
public class ContactsController implements View.OnClickListener {
    private ContactsView mContactsView;//所控制的view
    private Activity mContext;//所在activity
    private List<FriendEntry> mList = new ArrayList<>();//好友列表数据
    private ListAdapter mAdapter;
    private UserEntry user;
    private SharedPreferences sharedPreferences;
    public ContactsController(ContactsView mContactsView, FragmentActivity context) {
        this.mContactsView = mContactsView;
        this.mContext = context;
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public void initContacts() {
        mContactsView.showNewFriends(sharedPreferences.getInt("num",0));
        //数据库查询
         List<UserEntry> users= DataSupport.where("username=? and appKey=?",
                JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey()).find(UserEntry.class);
         user =users.get(0);
        //联系人管理接口入口类。提供联系人管理接口
        /*
        * public static void getFriendList(GetUserInfoListCallback callback)
        * 好友列表，异步返回结果
        * Parameters:
        * callback - 结果回调
        * */
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                if (responseCode == 0) {
                    //成功获取好友列表
                    if (userInfoList.size() != 0) {
                        //有好友
                        mList.clear();
                            for (UserInfo userInfo : userInfoList) {
                                //获取好友的展示名
                                String displayName = userInfo.getDisplayName();
                                //避免重复请求时导致数据重复
                                List<FriendEntry> friendEntrys=DataSupport.where("username=? and user=? and " +
                                        "appKey=?",userInfo.getUserName(),user.getUsername(),userInfo.getAppKey()).find(FriendEntry.class);
                                FriendEntry friend=null;
                                if(friendEntrys.size()!=0) {
                                     friend = friendEntrys.get(0);
                                }
                                if (null == friend) {
                                    //数据库无此好友
                                    if (TextUtils.isEmpty(userInfo.getAvatar())) {
                                        friend=new FriendEntry();
                                        friend.setUid(userInfo.getUserID());
                                        friend.setUsername(userInfo.getUserName());
                                        friend.setNoteName(userInfo.getNotename());
                                        friend.setNickName(userInfo.getNickname());
                                        friend.setAppKey(userInfo.getAppKey());
                                        friend.setDisplayName(displayName);
                                        friend.setUser(user.getUsername());
                                        friend.setAvatar(null);
                                    } else {
                                        friend=new FriendEntry();
                                        friend.setUid(userInfo.getUserID());
                                        friend.setUsername(userInfo.getUserName());
                                        friend.setNoteName(userInfo.getNotename());
                                        friend.setNickName(userInfo.getNickname());
                                        friend.setAppKey(userInfo.getAppKey());
                                        friend.setDisplayName(displayName);
                                        friend.setUser(user.getUsername());
                                        /*
                                        * public abstract java.io.File getAvatarFile()
                                        * 从本地获取用户头像缩略图文件，头像缩略图会在调用
                                        * JMessageClient.getUserInfo(String, GetUserInfoCallback) 时自动下载。
                                        * 当用户未设置头像，或者自动下载失败时此接口返回Null。
                                        * Returns:
                                        * 用户缩略头像文件对象，若未设置头像或者未下载完成则返回null*/
                                        JMessageClient.getUserInfo(userInfo.getUserName(), null, new GetUserInfoCallback() {
                                            @Override
                                            public void gotResult(int i, String s, UserInfo userInfo) {
                                            }
                                        });
                                        friend.setAvatar(userInfo.getAvatarFile().getAbsolutePath());
                                    }
                                    friend.save();
                                    mList.add(friend);
                                }
                                mList.add(friend);
                            }
                        }else{

                    }
                    mAdapter = new ListAdapter(mContext, mList);
                    mContactsView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ib_goToAddFriend://标题栏加号添加好友
                intent.setClass(mContext, SearchForAddFriendActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.verify_ll://验证消息
                intent.setClass(mContext, FriendRecommendActivity.class);
                mContext.startActivity(intent);
                mContactsView.dismissNewFriends();
                break;
            default:
                break;
        }
    }
    public void timerefresh(){
        //数据库查询
        List<UserEntry> users= DataSupport.where("username=? and appKey=?",
                JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey()).find(UserEntry.class);
        user =users.get(0);
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                if (responseCode == 0) {
                    //成功获取好友列表
                    if (userInfoList.size() != 0) {
                        //有好友
                        mList.clear();
                        for (UserInfo userInfo : userInfoList) {
                            //获取好友的展示名
                            String displayName = userInfo.getDisplayName();
                            //避免重复请求时导致数据重复
                            DataSupport.deleteAll(FriendEntry.class,"username=? and user=? and appKey=?",
                                    userInfo.getUserName(),user.getUsername(),userInfo.getAppKey());
                           /* List<FriendEntry> friendEntrys=DataSupport.where("username=? and user=? and " +
                                    "appKey=?",userInfo.getUserName(),user.getUsername(),userInfo.getAppKey()).find(FriendEntry.class);*/
                            FriendEntry friend;
                            /*if(friendEntrys.size()!=0) {
                                friend = friendEntrys.get(0);
                            }*/
                                //数据库无此好友
                                if (TextUtils.isEmpty(userInfo.getAvatar())) {
                                    friend=new FriendEntry();
                                    friend.setUid(userInfo.getUserID());
                                    friend.setUsername(userInfo.getUserName());
                                    friend.setNoteName(userInfo.getNotename());
                                    friend.setNickName(userInfo.getNickname());
                                    friend.setAppKey(userInfo.getAppKey());
                                    friend.setDisplayName(displayName);
                                    friend.setUser(user.getUsername());
                                    friend.setAvatar(null);
                                } else {
                                    friend=new FriendEntry();
                                    friend.setUid(userInfo.getUserID());
                                    friend.setUsername(userInfo.getUserName());
                                    friend.setNoteName(userInfo.getNotename());
                                    friend.setNickName(userInfo.getNickname());
                                    friend.setAppKey(userInfo.getAppKey());
                                    friend.setDisplayName(displayName);
                                    friend.setUser(user.getUsername());
                                        /*
                                        * public abstract java.io.File getAvatarFile()
                                        * 从本地获取用户头像缩略图文件，头像缩略图会在调用
                                        * JMessageClient.getUserInfo(String, GetUserInfoCallback) 时自动下载。
                                        * 当用户未设置头像，或者自动下载失败时此接口返回Null。
                                        * Returns:
                                        * 用户缩略头像文件对象，若未设置头像或者未下载完成则返回null*/
                                    JMessageClient.getUserInfo(userInfo.getUserName(), null, new GetUserInfoCallback() {
                                        @Override
                                        public void gotResult(int i, String s, UserInfo userInfo) {
                                        }
                                    });
                                    friend.setAvatar(userInfo.getAvatarFile().getAbsolutePath());
                                }
                                friend.save();
                                mList.add(friend);
                        }
                    }else{

                    }
                    mAdapter = new ListAdapter(mContext, mList);
                    mContactsView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}