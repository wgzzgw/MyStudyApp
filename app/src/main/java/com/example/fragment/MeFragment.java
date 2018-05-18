package com.example.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.R;
import com.example.controller.MeController;
import com.example.loginandsign.SignAndLogin;
import com.example.util.ScreenUtil;
import com.example.view.MeView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/12.
 */

public class MeFragment extends Fragment {
    private Context mContext;//me 碎片所在context
    private View mRootView;
    public MeView mMeView;//me 顶层父布局
    private MeController mMeController;//me view 控制器
    protected float mDensity;
    protected int mWidth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_me,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mMeView = (MeView) mRootView.findViewById(R.id.me_view);
        mDensity= ScreenUtil.getDeviceDensity(getContext());
        mWidth=ScreenUtil.getScreenSize(getContext()).widthPixels;
        mMeView.initModule(mDensity, mWidth);
        mMeController = new MeController(this, mWidth);
        mMeView.setListener(mMeController);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            //ViewGroup的子类调用，移除自身的子视图
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        UserInfo myInfo = JMessageClient.getMyInfo();
        myInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    //获取头像成功
                    mMeView.showPhoto(bitmap);
                    mMeController.setBitmap(bitmap);
                }else {
                    //获取头像失败
                    mMeView.showPhoto(null);
                    mMeController.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rc_default_portrait));
                }
            }
        });
        //设置昵称，签名
        mMeView.showNickName(myInfo);
        mMeView.showBirth(myInfo);
        mMeView.showGender(myInfo);
        mMeView.showArea(myInfo);
        super.onResume();
    }
    //退出登录
    public void Logout() {
        final Intent intent = new Intent();
        UserInfo info = JMessageClient.getMyInfo();
        if (null != info) {
            JMessageClient.logout();
            intent.setClass(mContext, SignAndLogin.class);
            startActivity(intent);
        } else {
            Toast.makeText(mContext,"退出失败！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        mMeController.destorydialog();
        super.onDestroy();
    }
}
