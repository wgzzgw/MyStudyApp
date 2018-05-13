package com.example.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by yy on 2018/5/13.
 */
/*
* 修改密码
* */
public class ResetPasswordActivity extends BaseActivity{
    private EditText mOld_password;//旧密码
    private EditText mNew_password;//新密码
    private EditText mRe_newPassword;//确认密码
    private Button mBtn_sure;//确认按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initView();
        initData();
    }
    private void initView() {
        initTitle(true, true, "修改密码", "", false, "保存");
        mOld_password = (EditText) findViewById(R.id.old_password);
        mNew_password = (EditText) findViewById(R.id.new_password);
        mRe_newPassword = (EditText) findViewById(R.id.re_newPassword);
        mBtn_sure = (Button) findViewById(R.id.btn_sure);
    }
    private void initData() {
        mBtn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPsw = mOld_password.getText().toString().trim();
                String newPsw = mNew_password.getText().toString().trim();
                String reNewPsw = mRe_newPassword.getText().toString().trim();
                //检测是否为空
                if (TextUtils.isEmpty(oldPsw) ||
                        TextUtils.isEmpty(newPsw) ||
                        TextUtils.isEmpty(reNewPsw)) {
                   Toast.makeText(ResetPasswordActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                /*
                * public static boolean isCurrentUserPasswordValid(java.lang.String password)
                判断输入的字符串是否与当前用户的密码匹配
                Parameters:
                password - 被匹配的字符串
                Returns:
                正确匹配返回true，其他情况返回false
                * */
                boolean passwordValid = JMessageClient.isCurrentUserPasswordValid(oldPsw);
                if (passwordValid) {
                    if (newPsw.equals(reNewPsw)) {
                        final ProgressDialog dialog = new ProgressDialog(ResetPasswordActivity.this);
                        dialog.setMessage(getString(R.string.modifying_hint));
                        dialog.show();
                        JMessageClient.updateUserPassword(oldPsw, newPsw, new BasicCallback() {
                            @Override
                            public void gotResult(int responseCode, String responseMessage) {
                                dialog.dismiss();
                                if (responseCode == 0) {
                                    //修改成功
                                   Toast.makeText(ResetPasswordActivity.this,"修改成功！",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(ResetPasswordActivity.this,"修改失败, 新密码要在4-128字节之间！",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ResetPasswordActivity.this,"两次输入不一致",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this,"原密码不正确",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
