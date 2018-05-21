package com.example;

import android.app.Application;
import android.content.Context;

import com.example.db.UserEntry;
import com.example.util.QnUploadHelper;
import com.example.util.SharePreferenceManager;

import org.litepal.LitePal;

import cn.jpush.im.android.api.JMessageClient;

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
    }
    public static MyApplication getApplication() {
        return app;
    }
    public static Context getContext() {
        return appContext;
    }
    public void onTerminate(){
        super.onTerminate();
    }
    public static UserEntry getUserEntry() {
        UserEntry userEntry=new UserEntry();
        userEntry.setUsername(JMessageClient.getMyInfo().getUserName());
        userEntry.setAppKey(JMessageClient.getMyInfo().getAppKey());
        return userEntry;
    }
}
