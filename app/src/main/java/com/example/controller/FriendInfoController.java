package com.example.controller;

import android.view.View;

import com.example.R;
import com.example.activity.FriendInfoActivity;


/**
 * Created by yy on 2018/5/17.
 */

public class FriendInfoController implements View.OnClickListener{
    private FriendInfoActivity mContext;
    public FriendInfoController(FriendInfoActivity context) {
        this.mContext = context;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_friendPhoto:
                //查看大图
                mContext.startBrowserAvatar();
                break;
            default:
                break;
        }
    }
}
