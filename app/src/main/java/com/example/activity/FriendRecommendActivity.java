package com.example.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.MyApplication;
import com.example.R;
import com.example.adapter.FriendRecommendAdapter;
import com.example.db.FriendRecommendEntry;
import com.example.db.UserEntry;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.model.UserInfo;

/*
 * 通讯录界面.验证消息
 */
public class FriendRecommendActivity extends BaseActivity  {
    private ListView mListView;//推荐的好友消息
    private FriendRecommendAdapter mAdapter;
    private List<FriendRecommendEntry> mList=new ArrayList<FriendRecommendEntry>();//数据源 好友推荐
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_recommend);

        initView();
        UserEntry user = MyApplication.getUserEntry();
        if (null != user) {
           final List<FriendRecommendEntry> friendRecommendEntries=
                   DataSupport.where("sendname=?",user.getUsername()).find(FriendRecommendEntry.class);
            if(friendRecommendEntries.size()!=0) {
                for (int i = 0; i < friendRecommendEntries.size(); i++) {
                    FriendRecommendEntry friendRecommendEntry = friendRecommendEntries.get(i);
                    mList.add(friendRecommendEntry);
                }
                mAdapter = new FriendRecommendAdapter(this, mList);
                mListView.setAdapter(mAdapter);
            }
        } else {
            Log.e("FriendRecommendActivity", "Unexpected error: User table null");
        }

    }
    private void initView() {
        initTitle(true, true, "新的朋友", "", false, "");
        mListView = (ListView) findViewById(R.id.friend_recommend_list_view);
    }
}
