package com.example.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.R;
import com.example.activity.FriendInfoActivity;
import com.example.util.ImgUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/17.
 */
/*
* 好友信息界面顶层布局
* */
public class FriendInfoView extends LinearLayout {
    private ImageView mIv_friendPhoto;//好友头像
    private TextView mTv_noteName;//好友备注
    private TextView mTv_signature;//好友签名
    private TextView mTv_userName;//好友用户名
    private TextView mTv_gender;//好友性别
    private TextView mTv_birthday;//好友生日
    private TextView mTv_address;//好友地区
    private Context mContext;
    private RelativeLayout mRl_NickName;//昵称布局
    private TextView mTv_NickName;//好友昵称
    public FriendInfoView(Context context) {
        super(context);
    }

    public FriendInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initModel(FriendInfoActivity context) {
        this.mContext = context;
        mIv_friendPhoto = (ImageView) findViewById(R.id.iv_friendPhoto);
        mTv_noteName = (TextView) findViewById(R.id.tv_nickName);
        mTv_signature = (TextView) findViewById(R.id.tv_signature);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_address = (TextView) findViewById(R.id.tv_address);
        mRl_NickName = (RelativeLayout) findViewById(R.id.rl_nickName);
        mTv_NickName = (TextView) findViewById(R.id.tv_nick);
    }

    public void setListeners(OnClickListener listeners) {
        mIv_friendPhoto.setOnClickListener(listeners);
    }
    public void initInfo(UserInfo userInfo) {
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            ImgUtils.loadbit(bitmap, mIv_friendPhoto);
                        } else {
                            ImgUtils.load(R.drawable.rc_default_portrait, mIv_friendPhoto);
                        }
                    }
                });
            } else {
                ImgUtils.load(R.drawable.rc_default_portrait, mIv_friendPhoto);
            }
            String noteName = userInfo.getNotename();
            String nickName = userInfo.getNickname();
            String userName = userInfo.getUserName();
            //有备注 有昵称
            mTv_userName.setText(userName);
            if (!TextUtils.isEmpty(noteName) && !TextUtils.isEmpty(nickName)&&!noteName.trim().equals("")) {
                mRl_NickName.setVisibility(View.VISIBLE);
                mTv_NickName.setText(nickName);
                mTv_noteName.setText("备注名: " + noteName);
            }
            //没有备注 有昵称
            else if ((TextUtils.isEmpty(noteName)||noteName.trim().equals("")) && !TextUtils.isEmpty(nickName)) {
                mRl_NickName.setVisibility(View.GONE);
                mTv_noteName.setText("昵称: " + nickName);
            }
            //有备注 没有昵称
            else if (!TextUtils.isEmpty(noteName) &&!noteName.trim().equals("")&& TextUtils.isEmpty(nickName)) {
                mRl_NickName.setVisibility(View.GONE);
                mTv_noteName.setText("备注名: " + noteName);
            }
            //没有备注名 没有昵称
            else {
                mRl_NickName.setVisibility(View.GONE);
                mTv_noteName.setText("用户名: " + userName);
            }
            if (userInfo.getGender() == UserInfo.Gender.male) {
                mTv_gender.setText(R.string.man);
            } else if (userInfo.getGender() == UserInfo.Gender.female) {
                mTv_gender.setText(R.string.women);
            } else {
                mTv_gender.setText(R.string.unknow);
            }
            if (!TextUtils.isEmpty(userInfo.getAddress())) {
                mTv_address.setText(userInfo.getAddress());
            } else {
                mTv_address.setText("广东-广州-海珠");
            }
            if (!TextUtils.isEmpty(userInfo.getSignature())) {
                mTv_signature.setText(userInfo.getSignature());
            } else {
                mTv_signature.setText("它好像忘记签名了...");
            }
            mTv_birthday.setText(getBirthday(userInfo));
        }
    }
    public String getBirthday(UserInfo info) {
        long birthday = info.getBirthday();
        if (birthday == 0) {
            return "1990-01-01";
        }else {
            Date date = new Date(birthday);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        }
    }
}