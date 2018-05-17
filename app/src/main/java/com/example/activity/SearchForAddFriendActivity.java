package com.example.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.db.FriendEntry;
import com.example.db.FriendRecommendEntry;
import com.example.db.UserEntry;
import com.example.dialog.LoadingDialog;
import com.example.util.ImgUtils;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class SearchForAddFriendActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEt_searchUser;//搜索用户名关键词
    private Button mBtn_search;//搜索按钮
    private LinearLayout mSearch_result;//搜索结果布局
    private ImageView mSearch_header;//搜索结果用户的头像
    private TextView mSearch_name;//搜索结果用户的名字
    private Button mSearch_addBtn;//搜索结果用户的加好友
    private ImageView mIv_clear;//清空搜索内容
    private FriendRecommendEntry friendRecommendEntry;
    private UserInfo meinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_add_friend);
        meinfo=JMessageClient.getMyInfo();
        initView();
        initListener();
    }
    private void initView() {
        mEt_searchUser = (EditText) findViewById(R.id.et_searchUser);
        mBtn_search = (Button) findViewById(R.id.btn_search);
        mSearch_result = (LinearLayout) findViewById(R.id.search_result);
        mSearch_header = (ImageView) findViewById(R.id.search_header);
        mSearch_name = (TextView) findViewById(R.id.search_name);
        mSearch_addBtn = (Button) findViewById(R.id.search_addBtn);
        mIv_clear = (ImageView) findViewById(R.id.iv_clear);
        //无内容时不可点击
        mBtn_search.setEnabled(false);
        initTitle(true, true, "添加好友", "", false, "");
    }
    private void initListener() {
        mEt_searchUser.addTextChangedListener(new TextChange());
        mBtn_search.setOnClickListener(this);
        mSearch_result.setOnClickListener(this);
        mSearch_addBtn.setOnClickListener(this);
        mIv_clear.setOnClickListener(this);
    }
    /*
    * 内部类监听edittext的输入
    * */
    private class TextChange implements TextWatcher {
        @Override
        public void afterTextChanged(Editable arg0) {
        }
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }
        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            boolean feedback = mEt_searchUser.getText().length() > 0;
            if (feedback) {
                mIv_clear.setVisibility(View.VISIBLE);
                mBtn_search.setEnabled(true);
            } else {
                mIv_clear.setVisibility(View.GONE);
                mBtn_search.setEnabled(false);
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                hintKbTwo();
                String searchUserName = mEt_searchUser.getText().toString();
                if (!TextUtils.isEmpty(searchUserName)) {
                    final LoadingDialog loadingDialog=new LoadingDialog(this);
                    loadingDialog.show();
                    JMessageClient.getUserInfo(searchUserName, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                           loadingDialog.destroy();
                            if (responseCode == 0) {
                                friendRecommendEntry=new FriendRecommendEntry();
                                friendRecommendEntry.setSendname(info.getUserName());
                                friendRecommendEntry.setUsername(meinfo.getUserName());
                                friendRecommendEntry.setNickname(meinfo.getNickname());
                                friendRecommendEntry.setDisplayname(meinfo.getDisplayName());
                                friendRecommendEntry.setNotename(meinfo.getNotename());
                                if(meinfo.getAvatar()!=null) {
                                    /*if(meinfo.getAvatarFile()!=null) {
                                        friendRecommendEntry.setAvatar(meinfo.getAvatarFile().getAbsolutePath());
                                    }else{
                                        JMessageClient.getUserInfo(meinfo.getUserName(), null, new GetUserInfoCallback() {
                                            @Override
                                            public void gotResult(int i, String s, UserInfo userInfo) {
                                            }
                                        });*/
                                        friendRecommendEntry.setAvatar(meinfo.getAvatarFile().getAbsolutePath());
                                }else{
                                    friendRecommendEntry.setAvatar(null);
                                }
                                //成功搜索到用户
                                mSearch_result.setVisibility(View.VISIBLE);
                                //已经是好友则不显示"加好友"按钮
                                if (info.isFriend()) {
                                    mSearch_addBtn.setVisibility(View.GONE);
                                }else{
                                    mSearch_addBtn.setVisibility(View.VISIBLE);
                                }
                                //这个接口会在本地寻找头像文件,不存在就异步拉取
                                File avatarFile = info.getAvatarFile();
                                if (avatarFile != null) {
                                    ImgUtils.loadbit(BitmapFactory.decodeFile(avatarFile.getAbsolutePath()),mSearch_header);
                                    /*mSearch_header.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getAbsolutePath()));*/
                                } else {
                                    ImgUtils.load(R.drawable.rc_default_portrait,mSearch_header);
                                    /*mSearch_header.setImageResource(R.drawable.rc_default_portrait);*/
                                }
                                mSearch_name.setText(TextUtils.isEmpty(info.getNickname()) ? info.getUserName() : info.getNickname());
                            } else {
                                Toast.makeText(SearchForAddFriendActivity.this,"不存在该用户！",Toast.LENGTH_SHORT).show();
                                mSearch_result.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                break;
            case R.id.search_result:
                //详细资料
                Intent intent=new Intent();
                intent.putExtra("name",mEt_searchUser.getText().toString());
                //表示不是通讯录点进来的
                intent.putExtra("flag","1");
                intent.setClass(this, FriendInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.search_addBtn:
                //添加申请
                /*
                * public static void sendInvitationRequest(java.lang.String targetUsername,
                * java.lang.String appKey,
                * java.lang.String reason,
                * BasicCallback callback)
                * 发送添加好友请求。在对方未做回应的前提下，允许重复发送添加好友的请求。
                * Parameters:
                * targetUsername - 被邀请方用户名
                * appKey - 被邀请方用户的appKey,如果为空则默认从本应用appKey下查找用户。
                * reason - 申请理由
                * callback - 结果回调
                * */
                ContactManager.sendInvitationRequest(mEt_searchUser.getText().toString(), null, null, new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            if(friendRecommendEntry.isSaved()){
                            Toast.makeText(SearchForAddFriendActivity.this,"已申请成功，不需重复申请",Toast.LENGTH_SHORT).show();
                            }else {
                                friendRecommendEntry.save();
                                Toast.makeText(SearchForAddFriendActivity.this, "申请成功", Toast.LENGTH_SHORT).show();
                            }
                        } else if (responseCode == 871317) {
                            Toast.makeText(SearchForAddFriendActivity.this,"不能添加自己为好友",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SearchForAddFriendActivity.this,"申请失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.iv_clear:
                mEt_searchUser.setText("");
                break;
        }
    }
    /*
    隐藏软键盘
     */
    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            //getWindowToken()获取调用的view依附在哪个window的令牌
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
