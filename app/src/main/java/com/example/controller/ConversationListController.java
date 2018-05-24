package com.example.controller;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.example.activity.ChatActivity;
import com.example.adapter.ConversationListAdapter;
import com.example.fragment.ConversationListFragment;
import com.example.util.SortConvList;
import com.example.view.ConversationListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/24.
 */
/*
* 会话控制器
* */
public class ConversationListController  implements
        AdapterView.OnItemClickListener{
    private ConversationListView mConvListView;//控制视图
    private ConversationListFragment mContext;
    private ConversationListAdapter mListAdapter;
    private List<Conversation> mDatas = new ArrayList<Conversation>();//会话数据源
    public ConversationListController(ConversationListView listView, ConversationListFragment context) {
        this.mConvListView = listView;
        this.mContext = context;
        initConvListAdapter();
    }
    private void initConvListAdapter() {
        //获取会话列表
        mDatas = JMessageClient.getConversationList();
        if (mDatas != null && mDatas.size() > 0) {
            //会话不为空
            mConvListView.setNoNullConversation(true);
            //排序会话的新旧
            Collections.sort(mDatas, new SortConvList());
        } else {
            //会话为空
            mConvListView.setNoNullConversation(false);
        }
        mListAdapter = new ConversationListAdapter(mContext.getActivity(), mDatas, mConvListView);
        mConvListView.setConvListAdapter(mListAdapter);
    }
    public ConversationListAdapter getAdapter() {
        return mListAdapter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击会话条目
        Intent intent = new Intent();
        if (position > 0) {
            //拿到点击的会话
            Conversation conv = mDatas.get(position);
            intent.putExtra("userid", ((UserInfo) conv.getTargetInfo()).getDisplayName());
            intent.putExtra("username", ((UserInfo) conv.getTargetInfo()).getUserName());
        }
        intent.setClass(mContext.getActivity(), ChatActivity.class);
        mContext.getContext().startActivity(intent);
    }
}
