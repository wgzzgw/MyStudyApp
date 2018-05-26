package com.example.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.activity.ChatActivity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;

/**
 * Created by yy on 2018/5/17.
 */

public class BaseFragment extends Fragment {
    private Context mContext;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    public void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }
    /**
     * 全局接收通知栏点击消息类事件
     *
     * @param notificationClickEvent 消息事件
     */
    public void onEvent(NotificationClickEvent notificationClickEvent) {
        Intent intent=new Intent();
        if(notificationClickEvent.getMessage().getContent()instanceof PromptContent &&notificationClickEvent.getMessage()
                .getFromUser().getUserName().equals(JMessageClient.getMyInfo().getUserName())){
            //点击了撤回消息不做处理，自己，对方还是会跳转
            return ;
        }
        //点击通知进入会话
        intent.putExtra("userid",notificationClickEvent.getMessage().getFromUser().getDisplayName());
        intent.putExtra("username",notificationClickEvent.getMessage().getFromUser().getUserName());
        intent.setClass(mContext,ChatActivity.class);
        mContext.startActivity(intent);
    }
}
