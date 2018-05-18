package com.example.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.MyApplication;
import com.example.R;
import com.example.controller.ContactsController;
import com.example.db.FriendRecommendEntry;
import com.example.db.UserEntry;
import com.example.view.ContactsView;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.model.UserInfo;



/**
 * Created by yy on 2018/5/12.
 */
/*
* 通讯录 碎片
* */
public class ContactsFragment extends BaseFragment {
    private View mRootView;
    private ContactsView mContactsView;
    private ContactsController mContactsController;
    private Activity mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新控件
    private Timer timer=new Timer();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mContactsView.showContact();
                    mContactsController.initContacts();
                    break;
                default:
                    break;
            }
        }
    };
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_contacts,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mContactsView = (ContactsView) mRootView.findViewById(R.id.contacts_view);
        mContactsView.initModule();
        mContactsController = new ContactsController(mContactsView, this.getActivity());

        mContactsView.setOnClickListener(mContactsController);
        mContactsView.setListener(mContactsController);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉事件：请求极光接口
                mContactsController.initContacts();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mContactsController.timerefresh();
            }
        }, 0, 60000*2); //2分钟刷新一次
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
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

    @Override
    public void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(1,1500);
    }

    /*
    * 注册事件接收类之后，需要在消息接收类中实现如下方法来接收对应消息。
    * sdk将根据实现方法的方法名来区分不同的线程模式，
    * 常用的线程模式有onEvent(默认线程模式)和onEventMainThread(主线程模式)两种。
    * */
    //接收到好友事件
    public void onEvent(ContactNotifyEvent event) {
        Log.d("bb", "onEvent: ");
            final UserEntry user = MyApplication.getUserEntry();
            final String username = event.getFromUsername();//获取事件发送者名字
        if (event.getType() == ContactNotifyEvent.Type.invite_received) {
            int i=sharedPreferences.getInt("num",0);
            editor=sharedPreferences.edit();
            editor.putInt("num",i+1);
            editor.apply();
            JMessageClient.getUserInfo(username, null, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        String name = userInfo.getNickname();
                        if (TextUtils.isEmpty(name)) {
                            name = userInfo.getUserName();
                        }
                        List<FriendRecommendEntry> friendRecommendEntries = DataSupport.where(
                                "username=? and sendname=? "
                                , username, user.getUsername()
                        ).find(FriendRecommendEntry.class);
                        FriendRecommendEntry entry = new FriendRecommendEntry();
                        if (friendRecommendEntries.size() != 0) {
                            entry = friendRecommendEntries.get(0);
                        }
                        if (null == entry.getUsername()) {
                            entry.setSendname(user.getUsername());
                            entry.setUsername(username);
                            entry.setNickname(userInfo.getNickname());
                            entry.setDisplayname(name);
                            entry.setNotename(userInfo.getNotename());
                            if (null != userInfo.getAvatar()) {
                                entry.setAvatar(userInfo.getAvatarFile().getAbsolutePath());
                            } else {
                                entry.setAvatar(null);
                            }
                        }
                        entry.save();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
