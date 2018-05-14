package com.example.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class ResetGenderActivity extends BaseActivity{
    private LinearLayout man;//男 布局
    private LinearLayout women;//女 布局
    private LinearLayout baomi;//保密 布局
    private ImageView mangou;//男 打钩
    private ImageView womengou;//女 打钩
    private ImageView baomigou;//保密 打钩
    private Button mBtn_sure;//确认按钮
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_gender);

        initView();
        initData();
    }
    private void initView() {
        initTitle(true, true, "", "修改性别", false, "完成");
        man = (LinearLayout) findViewById(R.id.man);
        women = (LinearLayout) findViewById(R.id.women);
        baomi = (LinearLayout) findViewById(R.id.baomi);
        mangou=(ImageView)findViewById(R.id.mangou);
        womengou=(ImageView)findViewById(R.id.womengou);
        baomigou=(ImageView)findViewById(R.id.baomigou);
        mBtn_sure = (Button) findViewById(R.id.btn_sure);

        man.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mangou.setVisibility(View.VISIBLE);
                womengou.setVisibility(View.INVISIBLE);
                baomigou.setVisibility(View.INVISIBLE);
            }
        });
        women.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mangou.setVisibility(View.INVISIBLE);
                womengou.setVisibility(View.VISIBLE);
                baomigou.setVisibility(View.INVISIBLE);
            }
        });
        baomi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mangou.setVisibility(View.INVISIBLE);
                womengou.setVisibility(View.INVISIBLE);
                baomigou.setVisibility(View.VISIBLE);
            }
        });
    }
    private void initData() {
        mBtn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               UserInfo userInfo= JMessageClient.getMyInfo();
                if(mangou.getVisibility()==View.VISIBLE){
                    userInfo.setGender(UserInfo.Gender.male);
                }
                else if(womengou.getVisibility()==View.VISIBLE){
                    userInfo.setGender(UserInfo.Gender.female);
                }else{
                    userInfo.setGender(UserInfo.Gender.unknown);
                }
                //更新性别字段
                JMessageClient.updateMyInfo(UserInfo.Field.gender, userInfo, new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            //修改成功
                            Toast.makeText(ResetGenderActivity.this,"修改性别成功！",Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ResetGenderActivity.this,"修改性别失败！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
