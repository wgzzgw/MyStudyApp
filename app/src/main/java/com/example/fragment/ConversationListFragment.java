package com.example.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.R;
import com.example.controller.ConversationListController;
import com.example.event.Event;
import com.example.view.ConversationListView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;

/**
 * Created by yy on 2018/5/12.
 */

public class ConversationListFragment extends BaseFragment {
    private Activity mContext;
    private View mRootView;//根布局
    private ConversationListView mConvListView;
    private ConversationListController mConvListController;
    private BackgroundHandler mBackgroundHandler;
    private HandlerThread mThread;
    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //收到消息 将会话置顶
                    Conversation conv = (Conversation) msg.obj;
                    mConvListController.getAdapter().setToTop(conv);
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_conv_list,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mConvListView = new ConversationListView(mRootView, this);
        mConvListView.initModule();
        mConvListController = new ConversationListController(mConvListView, this);
        mConvListView.setItemListeners(mConvListController);
        mConvListView.setItemLongClickListeners(mConvListController);
        mConvListView.setSearchListener(mConvListController);
        mThread = new HandlerThread("MainActivity");
        mThread.start();
        mBackgroundHandler = new BackgroundHandler(mThread.getLooper());
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    /**
     * 收到消息
     */
    public void onEvent(MessageEvent event) {
        Log.d("p", "onEvent: 消息事件被执行了");
        Log.d("p", "onEvent: 此处可执行");
        Message msg = event.getMessage();
            final UserInfo userInfo = (UserInfo) msg.getTargetInfo();
            String targetId = userInfo.getUserName();
        Log.d("p", "onEvent: "+targetId);
            Conversation conv = JMessageClient.getSingleConversation(targetId);
            if(conv==null){
                conv=Conversation.createSingleConversation(targetId);
            }
            if (conv != null && mConvListController != null) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(userInfo.getAvatar())) {
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                                    if (responseCode == 0) {
                                        mConvListView.setUnReadMsg(JMessageClient.getAllUnReadMsgCount());
                                        mConvListController.getAdapter().notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(1, conv));
            }
    }
    /**
     * 消息撤回
     */
    public void onEvent(MessageRetractEvent event) {
        Log.d("c", "onEvent: "+"撤回消息可以被执行");
        Conversation conversation = event.getConversation();
        if(conversation!=null) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(1, conversation));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }
    public void onEventMainThread(Event event) {
        Log.d("c", "onEventMainThread: "+"自定义消息可以被执行");
        switch (event.getType()) {
            case createConversation:
                //每次进入chatactivity便认为创建一个会话（会话并不是总create,还有get)
                Conversation conv = event.getConversation();
                if (conv != null) {
                    mConvListController.getAdapter().addAndSort(conv);
                }
                break;
            case deleteConversation:
                conv = event.getConversation();
                if (null != conv) {
                    mConvListController.getAdapter().deleteConversation(conv);
                }
                break;
            case sendmessage:
                Conversation convv = event.getConversation();
                if(convv!=null){
                    //自己发送了消息，置顶
                    mConvListController.getAdapter().setToTop(convv);
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mConvListController.getAdapter().notifyDataSetChanged();
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mThread.getLooper().quit();
        super.onDestroy();
    }
}
