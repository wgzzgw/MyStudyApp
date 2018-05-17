package com.example.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.R;
import com.example.controller.FriendInfoController;
import com.example.dialog.LoadingDialog;
import com.example.view.FriendInfoView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/17.
 */
/*
* 好友信息界面
* */
public class FriendInfoActivity extends BaseActivity{
    private FriendInfoView mFriendInfoView;
    private UserInfo mUserInfo;
    private FriendInfoController mFriendInfoController;
    private boolean mIsGetAvatar = false;//缓存图片
    private Button btn_goToChat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mFriendInfoView = (FriendInfoView) findViewById(R.id.friend_info_view);
        mFriendInfoView.initModel(this);
        mFriendInfoController = new FriendInfoController(this);
        mFriendInfoView.setListeners(mFriendInfoController);
        initTitle(true,true,"详细资料","",false,"");
        if(getIntent().getStringExtra("flag").equals("1"));
        else if(getIntent().getStringExtra("flag").equals("2")){
            btn_goToChat=(Button)mFriendInfoView.findViewById(R.id.btn_goToChat);
            btn_goToChat.setVisibility(View.VISIBLE);
        }
        updateUserInfo();
        }
    private void updateUserInfo() {
        final LoadingDialog loadingDialog=new LoadingDialog(this);
        loadingDialog.show();
        JMessageClient.getUserInfo(getIntent().getStringExtra("name"), null, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
               loadingDialog.destroy();
                if (responseCode == 0) {
                    //保存info,以便调用其他方法
                    mUserInfo = info;
                    mFriendInfoView.initInfo(info);
                }
            }
        });
    }
    //点击头像预览大图
    public void startBrowserAvatar() {
        Log.d("", "startBrowserAvatar: ");
        if (mUserInfo != null && !TextUtils.isEmpty(mUserInfo.getAvatar())) {
            if (mIsGetAvatar) {
                //如果缓存了图片，直接加载
                Bitmap bitmap = BitmapFactory.decodeFile(mUserInfo.getAvatarFile().getAbsolutePath());
                if (bitmap != null) {
                    Intent intent = new Intent();
                    intent.putExtra("avatarPath", mUserInfo.getAvatarFile().getAbsolutePath());
                    intent.setClass(this, BrowserViewPagerActivity.class);
                    startActivity(intent);
                }
            } else {
                final LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.show();
                JMessageClient.getUserInfo(mUserInfo.getUserName(), null, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        if (i == 0) {
                            loadingDialog.destroy();
                            BitmapFactory.decodeFile(mUserInfo.getAvatarFile().getAbsolutePath());
                            mIsGetAvatar = true;
                            Intent intent = new Intent();
                            intent.putExtra("avatarPath", mUserInfo.getAvatarFile().getAbsolutePath());
                            intent.setClass(FriendInfoActivity.this, BrowserViewPagerActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
 }
}
