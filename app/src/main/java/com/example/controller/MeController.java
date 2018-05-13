package com.example.controller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.example.R;
import com.example.activity.PersonalActivity;
import com.example.activity.ResetAreaActivity;
import com.example.activity.ResetGenderActivity;
import com.example.activity.ResetPasswordActivity;
import com.example.dialog.LogoutDialog;
import com.example.fragment.MeFragment;

import java.util.Date;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by yy on 2018/5/13.
 */
/*
* me 碎片顶层布局控制器
* */
public class MeController implements View.OnClickListener{
    public static final String PERSONAL_PHOTO = "personal_photo";
    private MeFragment mContext;
    private LogoutDialog mDialog;
    private int mWidth;
    private Bitmap mBitmap;
    public MeController(MeFragment context, int width) {
        this.mContext = context;
        this.mWidth = width;
    }
    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setPassword:
                //打开设置密码界面
                mContext.startActivity(new Intent(mContext.getContext(), ResetPasswordActivity.class));
                break;
            case R.id.gender:
                //打开修改性别界面
                mContext.startActivity(new Intent(mContext.getContext(),ResetGenderActivity.class));
                break;
            case R.id.exit:
                //打开确认退出对话框
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.jmui_cancel_btn:
                                mDialog.hide();
                                break;
                            case R.id.jmui_commit_btn:
                                mContext.Logout();
                                mDialog.hide();
                                mDialog.destroy();
                                mContext.getActivity().finish();
                                break;
                        }
                    }
                };
                mDialog=new LogoutDialog(mContext.getActivity(),listener);
                mDialog.setWidthAndHeight((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                break;
            case R.id.birth:
                //弹出时间选择器选择生日
                TimePickerView timePickerView = new TimePickerView.Builder(mContext.getContext(), new TimePickerView.OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(final Date date, View v) {
                        final UserInfo mMyInfo= JMessageClient.getMyInfo();
                        //设置生日时间戳
                        mMyInfo.setBirthday(date.getTime());
                        //更新生日时间戳
                        JMessageClient.updateMyInfo(UserInfo.Field.birthday, mMyInfo, new BasicCallback() {
                            @Override
                            public void gotResult(int responseCode, String responseMessage) {
                                if (responseCode == 0) {
                                    mContext.mMeView.showBirth(mMyInfo);
                                    Toast.makeText(mContext.getActivity(), "更新生日成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext.getActivity(), "更新生日失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setType(TimePickerView.Type.YEAR_MONTH_DAY).setCancelText("取消")
                        .setSubmitText("确定")
                        .setContentSize(20)//滚轮文字大小
                        .setTitleSize(20)//标题文字大小
                        .setOutSideCancelable(true)
                        .isCyclic(true)
                        .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                        .setSubmitColor(Color.GRAY)//确定按钮文字颜色
                        .setCancelColor(Color.GRAY)//取消按钮文字颜色
                        .isCenterLabel(false)
                        .build();
                timePickerView.show();
                break;
            case R.id.area:
                //打开设置地区界面
                mContext.startActivity(new Intent(mContext.getContext(), ResetAreaActivity.class));
                break;
            case R.id.rl_personal:
                Intent intent = new Intent(mContext.getContext(), PersonalActivity.class);
                intent.putExtra(PERSONAL_PHOTO, mBitmap);
                mContext.startActivity(intent);
                break;
        }
    }
}
