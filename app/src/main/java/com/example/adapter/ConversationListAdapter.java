package com.example.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.view.ConversationListView;

import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/24.
 */
/*
* 会话列表适配器
* */
public class ConversationListAdapter extends BaseAdapter {
    private List<Conversation> mDatas;//数据源
    private Activity mContext;
    private UserInfo mUserInfo;//自己
    private ConversationListView mConversationListView;
    public ConversationListAdapter(Activity context, List<Conversation> data, ConversationListView convListView) {
        this.mContext = context;
        this.mDatas = data;
        this.mConversationListView = convListView;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
