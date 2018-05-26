package com.example.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MyApplication;
import com.example.R;
import com.example.controller.FriendInfoController;
import com.example.db.FriendEntry;
import com.example.dialog.EditMessageDialog;
import com.example.dialog.LoadingDialog;
import com.example.dialog.LogoutDialog;
import com.example.util.ScreenUtil;
import com.example.util.SharePreferenceManager;
import com.example.view.FriendInfoView;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by yy on 2018/5/17.
 */
/*
* 好友信息界面
* */
public class FriendInfoActivity extends BaseActivity  {
    private FriendInfoView mFriendInfoView;
    private UserInfo mUserInfo;
    private FriendInfoController mFriendInfoController;
    private boolean mIsGetAvatar = false;//缓存图片
    private Button btn_goToChat;
    private Button btn_deletefriend;
    private Button more;//布局bar 右边按钮
    private PopupWindow mPopupWindow;
    private View mPopupView;
    private LayoutInflater mLayoutInflater;
    private TextView settingnotename;
    private LogoutDialog logoutDialog;
    private int mWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mWidth=ScreenUtil.getScreenSize(this).widthPixels;
        mLayoutInflater = (LayoutInflater) getSystemService(
                LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(
                R.layout.popupview, null);
        settingnotename=(TextView)mPopupView.findViewById(R.id.settingnotename);
        mFriendInfoView = (FriendInfoView) findViewById(R.id.friend_info_view);
        mFriendInfoView.initModel(this);
        mFriendInfoController = new FriendInfoController(this);
        mFriendInfoView.setListeners(mFriendInfoController);
        initTitle(true,true,"详细资料","",true,"•••");
        if(getIntent().getStringExtra("flag").equals("1"));
        else if(getIntent().getStringExtra("flag").equals("2")){
            btn_goToChat=(Button)mFriendInfoView.findViewById(R.id.btn_goToChat);
            btn_goToChat.setVisibility(View.VISIBLE);
            btn_goToChat.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent();
                    intent.putExtra("userid",mUserInfo.getDisplayName());
                    intent.putExtra("username",mUserInfo.getUserName());
                    intent.setClass(FriendInfoActivity.this,ChatActivity.class);
                    startActivity(intent);
                }
            });
            btn_deletefriend=(Button)mFriendInfoView.findViewById(R.id.btn_deletefriend);
            btn_deletefriend.setVisibility(View.VISIBLE);
            btn_deletefriend.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //打开确认退出对话框
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.jmui_cancel_btn:
                                    logoutDialog.destroy();
                                    break;
                                case R.id.jmui_commit_btn:
                                    DataSupport.deleteAll(FriendEntry.class,"username=? and user=? and appKey=?",
                                            mUserInfo.getUserName(), MyApplication.getUserEntry().getUsername(),mUserInfo.getAppKey());
                                    mUserInfo.removeFromFriendList(new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                            if(i==0){
                                                //同时把会话也删除，下次登录见效
                                                JMessageClient.deleteSingleConversation(mUserInfo.getUserName());
                                                Toast.makeText(FriendInfoActivity.this,"删除好友成功！",Toast
                                                        .LENGTH_SHORT).show();
                                                logoutDialog.destroy();
                                                finish();
                                            }
                                        }
                                    });
                                    break;
                            }
                        }
                    };
                    logoutDialog=new LogoutDialog(FriendInfoActivity.this,listener);
                    logoutDialog.setWidthAndHeight((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                    logoutDialog.show();
                }
            });
        }
        updateUserInfo();
        more=(Button)findViewById(R.id.jmui_commit_btn);
        more.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popupShow(view);
            }
        });
        settingnotename.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
                final EditMessageDialog dialog = new EditMessageDialog(FriendInfoActivity.this);
                dialog.setOnOKListener(new EditMessageDialog.OnOKListener() {
                    @Override
                    public void onOk(final String content) {
                        Log.d("debug", "onOk: ");
                        //必须是好友调用才有效
                            mUserInfo.updateNoteName(content, new BasicCallback() {
                                @Override
                                public void gotResult(int i, String s) {
                                    if(i==0) {
                                        Log.d("c", "gotResult: ");
                                        dialog.destroy();
                                        updateUserInfo();
                                    }
                                }
                            });
                    }
                });
                if(mUserInfo.getNotename()!=null) {
                    dialog.show(mUserInfo.getNotename());
                }
                else{
                    dialog.show();
                }
            }
        });
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
    /**
     * 显示popup window
     *
     * @param view popup window
     */
    private void popupShow(View view) {
        //获取屏幕的密度
        int density = (int) ScreenUtil.getDeviceDensity(this);
        // 显示popup window
        mPopupWindow = new PopupWindow(mPopupView,
                200 * density, 50 * density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 透明背景
        Drawable transpent = new ColorDrawable(Color.parseColor("#99E4E4"));
        mPopupWindow.setBackgroundDrawable(transpent);
        // 获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //参数一：父view 参数二：没有设定对齐方向 参数三四：偏移量
        mPopupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                location[0] - 40 * density,
                location[1] + 63 * density);
    }
}
