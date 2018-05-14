package com.example.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.R;
import com.example.dialog.LoadingDialog;
import com.example.util.ImgUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/12.
 */
/*
* me 最外层布局
* */
public class MeView extends LinearLayout {
    private Context mContext;//view所在context
    private TextView mSignatureTv;//签名
    private TextView mNickNameTv;//昵称
    private ImageView mTakePhotoBtn;//头像
    private RelativeLayout mSet_pwd;//修改密码
    private RelativeLayout mBirth;//生日
    private RelativeLayout mGender;//性别
    private RelativeLayout mArea;//地区
    private RelativeLayout mExit;//退出
    private TextView setBirth;//设置生日
    private TextView setGender;//设置性别
    private TextView setArea;//设置地区
    private int mWidth;//宽
    private int mHeight;//高
    private RelativeLayout mRl_personal;//头像一栏布局
    public MeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }
    public void initModule(float density, int width) {
        mTakePhotoBtn = (ImageView) findViewById(R.id.take_photo_iv);
        mNickNameTv = (TextView) findViewById(R.id.nickName);
        mSignatureTv = (TextView) findViewById(R.id.signature);
        mSet_pwd = (RelativeLayout) findViewById(R.id.setPassword);
        mBirth = (RelativeLayout) findViewById(R.id.birth);
        mGender = (RelativeLayout) findViewById(R.id.gender);
        mArea = (RelativeLayout) findViewById(R.id.area);
        mRl_personal = (RelativeLayout) findViewById(R.id.rl_personal);
        mExit = (RelativeLayout) findViewById(R.id.exit);
        setBirth=(TextView)findViewById(R.id.setBirth);
        setGender=(TextView)findViewById(R.id.setGender);
        setArea=(TextView)findViewById(R.id.setArea);
        mWidth = width;
        mHeight = (int) (190 * density);
    }

    public void setListener(OnClickListener onClickListener) {
        mSet_pwd.setOnClickListener(onClickListener);
        mBirth.setOnClickListener(onClickListener);
        mGender.setOnClickListener(onClickListener);
        mArea.setOnClickListener(onClickListener);
        mExit.setOnClickListener(onClickListener);
        mRl_personal.setOnClickListener(onClickListener);
    }

    public void showPhoto(Bitmap avatarBitmap) {
        if (avatarBitmap != null) {
           /* mTakePhotoBtn.setImageBitmap(avatarBitmap);*/
            ImgUtils.loadbit(avatarBitmap,mTakePhotoBtn);
        }else {
            /*mTakePhotoBtn.setImageResource(R.drawable.rc_default_portrait);*/
            ImgUtils.load(R.drawable.rc_default_portrait,mTakePhotoBtn);
        }
    }
    public void showNickName(UserInfo myInfo) {
        if (!TextUtils.isEmpty(myInfo.getNickname().trim())) {
            mNickNameTv.setText(myInfo.getNickname());
        } else {
            mNickNameTv.setText(myInfo.getUserName());
        }
        mSignatureTv.setText(myInfo.getSignature());
    }
    public void showBirth(UserInfo myInfo) {
        if (myInfo.getBirthday()<0||myInfo.getBirthday()==0) {
            setBirth.setText("1990-01-01");
        } else {
            Date date = new Date(myInfo.getBirthday());
            setBirth.setText(getDataTime(date));
        }
    }
    public void showGender(UserInfo myInfo) {
       /* if(myInfo.getGender().toString().equalsIgnoreCase("unknown")){
            setGender.setText("保密");
        }else if(myInfo.getGender().toString().equalsIgnoreCase("female")){
            setGender.setText("女");
        }else
     setGender.setText("男");*/
        UserInfo.Gender gender = myInfo.getGender();
        if (gender != null) {
            if (gender.equals(UserInfo.Gender.male)) {
                setGender.setText("男");
            } else if (gender.equals(UserInfo.Gender.female)) {
                setGender.setText("女");
            } else {
                setGender.setText("保密");
            }
        }
    }
    public void showArea(UserInfo myInfo) {
        if (!TextUtils.isEmpty(myInfo.getAddress())) {
            setArea.setText(myInfo.getAddress());
        } else {
            setArea.setText("广东-广州-海珠");
        }
    }
    //格式化日期
    public String getDataTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}
