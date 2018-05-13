package com.example.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.dialog.LoadingDialog;
import com.example.util.PicChooserHelper;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class PersonalActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout mSign;//签名一栏布局
    private TextView mTv_sign;//签名
    private RelativeLayout mRl_nickName;//昵称一栏布局
    private TextView mTv_nickName;//昵称
    private ImageView mIv_photo;//头像
    private TextView mTv_userName;//头像下的昵称
    private LoadingDialog dialog;
    private UserInfo mMyInfo;
    private Intent intent;
    public static final int SIGN = 1;
    public static final int FLAGS_SIGN = 2;//签名flag
    public static final int FLAGS_NICK = 3;//昵称flag
    public static final String SIGN_KEY = "sign_key";//存放签名key
    public static final String NICK_NAME_KEY = "nick_name_key";//存放昵称key
    public static final int NICK_NAME = 4;
    private PicChooserHelper mPicChooserHelper;//图片选择工具类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        dialog=new LoadingDialog(this);
        initView();
        initListener();
        initData();
    }

    private void initData() {
        dialog.show();
        mMyInfo = JMessageClient.getMyInfo();
        if (mMyInfo != null) {
            mTv_nickName.setText(mMyInfo.getNickname());
            mTv_userName.setText("用户名:" + mMyInfo.getUserName());
            mTv_sign.setText(mMyInfo.getSignature());
        }
        mMyInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                if (responseCode == 0) {
                    //用户设置过头像
                    mIv_photo.setImageBitmap(avatarBitmap);
                } else {
                    //默认头像
                    mIv_photo.setImageResource(R.drawable.rc_default_portrait);
                }
            }
        });
        dialog.destroy();

    }

    @Override
    public void onClick(View view) {
        intent = new Intent(PersonalActivity.this, NickSignActivity.class);
        switch (view.getId()) {
            case R.id.rl_nickName:
                //昵称
                intent.setFlags(FLAGS_NICK);
                intent.putExtra("old_nick", mMyInfo.getNickname());
                startActivityForResult(intent, NICK_NAME);
                break;
            case R.id.sign:
                //签名
                intent.setFlags(FLAGS_SIGN);
                intent.putExtra("old_sign", mMyInfo.getSignature());
                startActivityForResult(intent, SIGN);
                break;
            case R.id.iv_photo:
                //头像
                choosePic();
                break;

        }
    }
    private void initView() {
        initTitle(true, true, "个人信息", "", false, "");
        mSign = (RelativeLayout) findViewById(R.id.sign);
        mTv_sign = (TextView) findViewById(R.id.tv_sign);
        mRl_nickName = (RelativeLayout) findViewById(R.id.rl_nickName);
        mTv_nickName = (TextView) findViewById(R.id.tv_nickName);
        mIv_photo = (ImageView) findViewById(R.id.iv_photo);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
    }
    private void initListener() {
        mSign.setOnClickListener(this);
        mRl_nickName.setOnClickListener(this);
        mIv_photo.setOnClickListener(this);
    }
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            switch (resultCode) {
                case SIGN:
                    final String sign = bundle.getString(SIGN_KEY);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMyInfo.setSignature(sign);
                            JMessageClient.updateMyInfo(UserInfo.Field.signature, mMyInfo, new BasicCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage) {
                                    if (responseCode == 0) {
                                        //更新签名成功
                                        mTv_sign.setText(sign);
                                        Toast.makeText(PersonalActivity.this,"更新签名成功",Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PersonalActivity.this,"更新签名失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                    break;
                case NICK_NAME:
                    final String nick = bundle.getString(NICK_NAME_KEY);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMyInfo.setNickname(nick);
                            JMessageClient.updateMyInfo(UserInfo.Field.nickname, mMyInfo, new BasicCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage) {
                                    if (responseCode == 0) {
                                        mTv_nickName.setText(nick);
                                        Toast.makeText(PersonalActivity.this,"更新昵称成功",Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PersonalActivity.this,"更新失败,请正确输入",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }
    private void choosePic() {
        if (mPicChooserHelper == null) {
            mPicChooserHelper = new PicChooserHelper(this);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    updateAvatar();
                }
                @Override
                public void onFail(String msg) {
                    Toast.makeText(PersonalActivity.this, "选择失败：" + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }
    private void updateAvatar() {
        mMyInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                if (responseCode == 0) {
                    //用户设置过头像
                    mIv_photo.setImageBitmap(avatarBitmap);
                } else {
                    //默认头像
                    mIv_photo.setImageResource(R.drawable.rc_default_portrait);
                }
            }
        });
    }
}
