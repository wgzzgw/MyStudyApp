package com.example.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.R;
import com.example.adapter.ListAdapter;
import com.example.controller.ContactsController;

/**
 * Created by yy on 2018/5/14.
 */
/*
* 通讯录最外层布局
* */
public class ContactsView extends LinearLayout {
    private ListView mListView;//好友列表
    private ImageButton mIb_goToAddFriend;//添加好友
    private LayoutInflater mInflater;
    private Context mContext;
    private LinearLayout mVerify_ll ;//验证消息布局

    public ContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }
    public void initModule() {
        mIb_goToAddFriend = (ImageButton) findViewById(R.id.ib_goToAddFriend);
        mListView = (ListView) findViewById(R.id.listview);
        mVerify_ll = (LinearLayout)findViewById(R.id.verify_ll);

    }
    public void setListener(ContactsController contactsController) {
        mIb_goToAddFriend.setOnClickListener(contactsController);
        mVerify_ll.setOnClickListener(contactsController);
    }
    public void setSelection(int position) {
        mListView.setSelection(position);
    }
    public void setAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }
    public void showContact() {
        mListView.setVisibility(VISIBLE);
    }
}
