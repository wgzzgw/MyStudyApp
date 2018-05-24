package com.example.view;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.R;
import com.example.fragment.ConversationListFragment;

/**
 * Created by yy on 2018/5/24.
 */
/*
* 会话列表视图
* */
public class ConversationListView {
    private View mConvListFragment;//根控件
    private ListView mConvListView = null;//列表
    private TextView mTitle;//标题
    private TextView mNull_conversation;//暂无会话
    private TextView mAllUnReadMsg;//未读消息
    private ConversationListFragment mFragment;

    public ConversationListView(View view, ConversationListFragment fragment) {
        this.mConvListFragment = view;
        this.mFragment = fragment;
    }
    public void initModule() {
        mConvListView = (ListView) mConvListFragment.findViewById(R.id.conv_list_view);
        mNull_conversation = (TextView) mConvListFragment.findViewById(R.id.null_conversation);
        mTitle=(TextView) mConvListFragment.findViewById(R.id.main_title_bar_title);
        mAllUnReadMsg = (TextView) mFragment.getActivity().findViewById(R.id.all_unread_number);
    }

    //对外提供适配器设置
    public void setConvListAdapter(ListAdapter adapter) {
        mConvListView.setAdapter(adapter);
    }
    //对方提供listview子项点击事件监听回调
    public void setItemListeners(AdapterView.OnItemClickListener onClickListener) {
        mConvListView.setOnItemClickListener(onClickListener);
    }
    public void setTitle(String title){
        mTitle.setText(title);
    }
    //是否暂无会话
    public void setNoNullConversation(boolean isHaveConv) {
        if (isHaveConv) {
            mNull_conversation.setVisibility(View.GONE);
        } else {
            mNull_conversation.setVisibility(View.VISIBLE);
        }
    }
    //设置左下角消息数量
    public void setUnReadMsg(final int count) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mAllUnReadMsg != null) {
                    if (count > 0) {
                        mAllUnReadMsg.setVisibility(View.VISIBLE);
                        if (count < 100) {
                            mAllUnReadMsg.setText(count + "");
                        } else {
                            mAllUnReadMsg.setText("99+");
                        }
                    } else {
                        mAllUnReadMsg.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
