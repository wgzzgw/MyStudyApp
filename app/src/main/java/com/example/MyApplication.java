package com.example;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.activity.ChatActivity;
import com.example.db.UserEntry;
import com.example.mystudyapp.MainActivity;
import com.example.util.QnUploadHelper;
import com.example.util.SharePreferenceManager;

import org.litepal.LitePal;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.event.NotificationClickEvent;

import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_SILENCE;

/**
 * Created by yy on 2018/5/12.
 */

public class MyApplication extends Application {
    private static MyApplication app;
    private static Context appContext;
    public static String[] letter=new String[100];
    private static String SAMPLE_CONFIGS = "sample_configs";
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appContext = getApplicationContext();
        LitePal.initialize(appContext);
        //初始化SDK,参数二关闭消息漫游
        JMessageClient.init(appContext,false);
        //初始化七牛云
        QnUploadHelper.init("sS8WL_Q-GoCmGK2W9N74pNiqE-dJxIVEHG45WQCy",
                "1Vz-rd3j78oDvNlniTVoEcAveAB2aCSxu3uwWLER",
                "http://p7h60wv6m.bkt.clouddn.com/",
                "zhibotupian"
        );
        SharePreferenceManager.init(this, SAMPLE_CONFIGS);
        JMessageClient.setDebugMode(true);
        //通知管理,通知栏开启，其他关闭，震动
        JMessageClient.setNotificationFlag(FLAG_NOTIFY_SILENCE);
        JMessageClient.registerEventReceiver(this);
    }
    public static MyApplication getApplication() {
        return app;
    }
    public static Context getContext() {
        return appContext;
    }
    public void onTerminate(){
        JMessageClient.unRegisterEventReceiver(this);
        super.onTerminate();
    }
    public static UserEntry getUserEntry() {
        UserEntry userEntry=new UserEntry();
        userEntry.setUsername(JMessageClient.getMyInfo().getUserName());
        userEntry.setAppKey(JMessageClient.getMyInfo().getAppKey());
        return userEntry;
    }
    /**
     * 全局接收通知栏点击消息类事件
     *
     * @param event 消息事件
     */
    public void onEvent(NotificationClickEvent event) {
        Intent intent=new Intent();
        if(event.getMessage().getContent()instanceof PromptContent&&event.getMessage()
                .getFromUser().getUserName().equals(JMessageClient.getMyInfo().getUserName())){
            //点击了撤回消息不做处理，自己，对方还是会跳转
            return ;
        }
        //点击通知进入会话
        intent.putExtra("userid",event.getMessage().getFromUser().getDisplayName());
        intent.putExtra("username",event.getMessage().getFromUser().getUserName());
        intent.setClass(this,ChatActivity.class);
        startActivity(intent);
    }
}
