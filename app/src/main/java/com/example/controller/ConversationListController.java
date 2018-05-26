package com.example.controller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.activity.ChatActivity;
import com.example.activity.SearchConv;
import com.example.adapter.ConversationListAdapter;
import com.example.fragment.ConversationListFragment;
import com.example.util.ScreenUtil;
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
        AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,View.OnClickListener{
    private ConversationListView mConvListView;//控制视图
    private ConversationListFragment mContext;
    private ConversationListAdapter mListAdapter;
    private List<Conversation> mDatas = new ArrayList<Conversation>();//会话数据源
    private PopupWindow mPopupWindow;//长按弹窗
    private View mPopupView;//popupwindow view
    private TextView del;
    private  Conversation delconv=null;//待删除的会话
    private int delposition;//待删除的位置
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
        mPopupView = LayoutInflater.from(mContext.getContext()).inflate(
                R.layout.popupdelview, null);
        del=(TextView)mPopupView.findViewById(R.id.del_conv);
        del.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                JMessageClient.deleteSingleConversation(((UserInfo) delconv.getTargetInfo()).getUserName());
                mDatas.remove(delposition);
                if (mDatas.size() > 0) {
                    mConvListView.setNoNullConversation(true);
                } else {
                    mConvListView.setNoNullConversation(false);
                }
                mListAdapter.notifyDataSetChanged();
                mPopupWindow.dismiss();
                Toast.makeText(mContext.getContext(),"删除会话成功",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public ConversationListAdapter getAdapter() {
        return mListAdapter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击会话条目
        Intent intent = new Intent();
        if (position >=0) {
            //拿到点击的会话
            Conversation conv = mDatas.get(position);
            Log.d("c", "onItemClick: "+ ((UserInfo) conv.getTargetInfo()).getUserName());
            intent.putExtra("userid", ((UserInfo) conv.getTargetInfo()).getDisplayName());
            intent.putExtra("username", ((UserInfo) conv.getTargetInfo()).getUserName());
            intent.setClass(mContext.getActivity(), ChatActivity.class);
            mContext.getContext().startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
         delconv = mDatas.get(position);
        delposition=position;
        if (delconv != null) {
            popupShow(view);
        }
        return true;
    }
    /**
     * 显示popup window
     *
     * @param view popup window
     */
    private void popupShow(View view) {
        //获取屏幕的密度
        int density = (int) ScreenUtil.getDeviceDensity(mContext.getContext());
        // 显示popup window
        mPopupWindow = new PopupWindow(mPopupView,
                100 * density, 35* density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 透明背景
        Drawable transpent = new ColorDrawable(Color.parseColor("#99E4E4"));
        mPopupWindow.setBackgroundDrawable(transpent);
        // 获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int popupWidth = mPopupView.getMeasuredWidth();
        //参数一：父view 参数二：没有设定对齐方向 参数三四：偏移量
        mPopupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                (location[0]+view.getWidth()/2)-popupWidth/2,
                location[1]);
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent();
        intent.setClass(mContext.getContext(), SearchConv.class);
        mContext.startActivity(intent);
    }
}
