package com.example.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.ContactNotifyEvent;

/**
 * Created by yy on 2018/5/17.
 */

public class BaseFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    public void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }
    public void onEvent(ContactNotifyEvent event){
    }
}
