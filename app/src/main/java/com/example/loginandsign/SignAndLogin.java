package com.example.loginandsign;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.R;
import com.example.mystudyapp.MainActivity;
import com.example.util.ConvertUtil;
import com.example.util.ImgUtils;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class SignAndLogin extends AppCompatActivity {
    private EditText account;//登录用户名
    private EditText pass;//登录密码
    private EditText account2;//注册用户名
    private EditText pass2;//注册密码
    private EditText confirmPass;//注册确认密码
    private RelativeLayout relativeLayout;//登录布局
    private RelativeLayout relativeLayout2;//注册布局
    private LinearLayout mainLinear;//登录与注册父布局
    private LinearLayout img;//父布局背景图片布局
    private TextView signUp;//注册按钮
    private TextView login;//登录按钮
    private LinearLayout.LayoutParams params;//登录与注册父布局 子控件登录设置参数
    private LinearLayout.LayoutParams params2;//登录与注册父布局 子控件注册设置参数
    private FrameLayout.LayoutParams params3;//最外层布局 子控件logo设置参数
    private FrameLayout mainFrame;//最外层布局
    private ObjectAnimator animator2;
    private ObjectAnimator animator1;
    private ConvertUtil convertUtil;//转换工具类
    private ImageView logo;
    private ImageView back;//背景图片
    private ProgressDialog mProgressDialog = null;//进度条
    private long mExitTime;//监听back键事件
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;
    private ImageView qq;
    private ImageView wechat;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_and_login);
        pref= PreferenceManager.getDefaultSharedPreferences(this);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params3 = new FrameLayout.LayoutParams(convertUtil.inDp(50,this), convertUtil.inDp(50,this));

        back = (ImageView) findViewById(R.id.backImg);
        signUp = (TextView) findViewById(R.id.signUp);
        login = (TextView) findViewById(R.id.login);
        account = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.pass);
        img = (LinearLayout) findViewById(R.id.img);
        account2 = (EditText) findViewById(R.id.email2);
        pass2 = (EditText) findViewById(R.id.pass2);
        mainFrame = (FrameLayout) findViewById(R.id.mainFrame);
        confirmPass = (EditText) findViewById(R.id.pass3);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.relative2);
        mainLinear = (LinearLayout) findViewById(R.id.mainLinear);
        rememberPass=(CheckBox)findViewById(R.id.remember_pass);
        qq=(ImageView)findViewById(R.id.qq);
        wechat=(ImageView)findViewById(R.id.wechat);
        ImgUtils.loadRound(R.drawable.pp,back);
        ImgUtils.loadRound(R.drawable.qq,qq);
        ImgUtils.loadRound(R.drawable.wechat,wechat);
        boolean isRemember=pref.getBoolean("remember_password",false);
        if(isRemember){
            //将账号和密码都设置到文本框中
            String laccount=pref.getString("account","");
            String lpassword=pref.getString("password","");
            account.setText(laccount);
            pass.setText(lpassword);
            rememberPass.setChecked(true);
        }

        logo = new ImageView(this);
        /*logo.setImageResource(R.drawable.logo);*/
        ImgUtils.loadRound(R.drawable.logo,logo);
        logo.setLayoutParams(params3);

        relativeLayout.post(new Runnable() {
            @Override
            public void run() {

                logo.setX((relativeLayout2.getRight() / 2));
                logo.setY(convertUtil.inDp(50,SignAndLogin.this));
                mainFrame.addView(logo);
            }
        });

        params.weight = (float) 0.75;
        params2.weight = (float) 4.25;


        /*
        * OnGlobalLayoutListener 是ViewTreeObserver的内部类，当一个视图树的布局发生改变时，
        * 可以被ViewTreeObserver监听到，这是一个注册监听视图树的观察者(observer)，
        * 在视图树的全局事件改变时得到通知。ViewTreeObserver不能直接实例化，而是通过getViewTreeObserver()获得。
        * */
        mainLinear.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /*
                * 它接受一个Rect对象作为参数，执行过程中会根据当前窗口可视区域大小更新outRect的值，
                * 执行完毕后，就可以根据更新后的outRect来确定窗口可视区域的大小。
                * */
                Rect r = new Rect();
                mainLinear.getWindowVisibleDisplayFrame(r);
                int screenHeight = mainFrame.getRootView().getHeight();//得到屏幕高度


                int keypadHeight = screenHeight - r.bottom;//初始0


                if (keypadHeight > screenHeight * 0.15) {
                    // 键盘打开
                    if (params.weight == 4.25) {
                        animator1 = ObjectAnimator.ofFloat(back, "scaleX", (float) 1.95);
                        animator2 = ObjectAnimator.ofFloat(back, "scaleY", (float) 1.95);
                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(animator1, animator2);
                        set.setDuration(1000);//持续1秒
                        set.start();//启动动画

                    } else {
                        animator1 = ObjectAnimator.ofFloat(back, "scaleX", (float) 1.75);
                        animator2 = ObjectAnimator.ofFloat(back, "scaleY", (float) 1.75);
                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(animator1, animator2);
                        set.setDuration(500);
                        set.start();
                    }
                } else {
                    // 键盘关闭
                    animator1 = ObjectAnimator.ofFloat(back, "scaleX", 3);
                    animator2 = ObjectAnimator.ofFloat(back, "scaleY", 3);
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(animator1, animator2);
                    set.setDuration(500);
                    set.start();
                }
            }
        });


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (params.weight == 4.25) {
                    //此时注册页面主占屏幕
                    /*
                    * Snackbar是Android Support Design Library库中的一个控件，可以在屏幕底部快速弹出消息，比Toast更加好用。
                    * */
                    final String signaccount=account2.getText().toString();
                    final String signpassword=pass2.getText().toString();
                    String signconfirmword=confirmPass.getText().toString();
                    //检测是否为空
                    if (TextUtils.isEmpty(signaccount) ||
                            TextUtils.isEmpty(signpassword) ||
                            TextUtils.isEmpty(signconfirmword)) {
                        Snackbar.make(relativeLayout, "账号或密码不能为空", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    //检测两次密码是否一致
                    if (!signpassword.equals(signconfirmword)) {
                        Snackbar.make(relativeLayout, "两次密码输入不一致", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    //检测输入字符是否合法
                    if(signaccount.length()<8||
                            signpassword.length()<8||
                            signconfirmword.length()<8){
                        Snackbar.make(relativeLayout, "用户名或密码长度不够", Snackbar.LENGTH_SHORT).show();
                        return ;
                    }
                    mProgressDialog = ProgressDialog.show(SignAndLogin.this, "提示：", "正在加载中。。。");
                    //注册账号
                    JMessageClient.register(signaccount, signpassword, new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String registerDesc) {
                            if (responseCode == 0) {
                                //注册成功
                                mProgressDialog.dismiss();//关闭进度条
                                account.setText(signaccount);
                                pass.setText(signpassword);
                                Snackbar.make(relativeLayout, "注册成功", Snackbar.LENGTH_SHORT).show();
                            } else {
                                mProgressDialog.dismiss();
                                Snackbar.make(relativeLayout, "注册失败", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                    login.performClick();
                    return;
                }
                account2.setVisibility(View.VISIBLE);
                pass2.setVisibility(View.VISIBLE);
                confirmPass.setVisibility(View.VISIBLE);

                final ChangeBounds bounds = new ChangeBounds();
                bounds.setDuration(1500);
                bounds.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition)
                    {

                        /*
                        * 第一个参数用于指定这个动画要操作的是哪个控件
                          第二个参数用于指定这个动画要操作这个控件的哪个属性
                          第三个参数是可变长参数，指这个属性值是从哪变到哪
                        * */
                        ObjectAnimator animator1 = ObjectAnimator.ofFloat(signUp, "translationX", mainLinear.getWidth() / 2 - relativeLayout2.getWidth() / 2 - signUp.getWidth() / 2);
                        ObjectAnimator animator2 = ObjectAnimator.ofFloat(img, "translationX", -relativeLayout2.getX());
                        ObjectAnimator animator3 = ObjectAnimator.ofFloat(signUp, "rotation", 0);

                        ObjectAnimator animator4 = ObjectAnimator.ofFloat(account, "alpha", 1, 0);
                        ObjectAnimator animator5 = ObjectAnimator.ofFloat(pass, "alpha", 1, 0);
                        ObjectAnimator animator6 = ObjectAnimator.ofFloat(rememberPass, "alpha", 1, 0);

                        ObjectAnimator animator7 = ObjectAnimator.ofFloat(login, "rotation", 90);
                        ObjectAnimator animator8 = ObjectAnimator.ofFloat(login, "y", relativeLayout2.getHeight() / 2);
                        ObjectAnimator animator9 = ObjectAnimator.ofFloat(account2, "alpha", 0, 1);

                        ObjectAnimator animator10 = ObjectAnimator.ofFloat(confirmPass, "alpha", 0, 1);
                        ObjectAnimator animator11 = ObjectAnimator.ofFloat(pass2, "alpha", 0, 1);
                        ObjectAnimator animator12 = ObjectAnimator.ofFloat(signUp, "y", login.getY());

                        ObjectAnimator animator13 = ObjectAnimator.ofFloat(back, "translationX", img.getX());
                        ObjectAnimator animator14 = ObjectAnimator.ofFloat(signUp, "scaleX", 2);
                        ObjectAnimator animator15 = ObjectAnimator.ofFloat(signUp, "scaleY", 2);

                        ObjectAnimator animator16 = ObjectAnimator.ofFloat(login, "scaleX", 1);
                        ObjectAnimator animator17 = ObjectAnimator.ofFloat(login, "scaleY", 1);
                        ObjectAnimator animator18 = ObjectAnimator.ofFloat(logo, "x", relativeLayout2.getRight() / 2 - relativeLayout.getRight());

                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(animator1, animator2, animator3, animator4, animator5, animator6, animator7,
                                animator8, animator9, animator10, animator11, animator12, animator13, animator14, animator15, animator16, animator17, animator18);
                        set.setDuration(1500).start();


                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {


                        account.setVisibility(View.INVISIBLE);
                        pass.setVisibility(View.INVISIBLE);
                        rememberPass.setVisibility(View.INVISIBLE);

                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {


                    }
                });

                TransitionManager.beginDelayedTransition(mainLinear, bounds);

                params.weight = (float) 4.25;
                params2.weight = (float) 0.75;


                relativeLayout.setLayoutParams(params);
                relativeLayout2.setLayoutParams(params2);

            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (params2.weight == 4.25) {
                    //此时登陆页面主占屏幕
                    final String loginaccount=account.getText().toString();
                    final String loginpassword=pass.getText().toString();
                    //输入为空的检测
                    if(TextUtils.isEmpty(loginaccount)||TextUtils.isEmpty(loginpassword)){
                        Snackbar.make(relativeLayout2, "用户名或密码不能为空！", Snackbar.LENGTH_SHORT).show();
                        return ;
                    }
                    mProgressDialog = ProgressDialog.show(SignAndLogin.this, "提示：", "正在加载中。。。");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    //调用接口登陆
                    JMessageClient.login(loginaccount, loginpassword, new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String LoginDesc) {
                            if (responseCode == 0) {
                                //登陆成功
                                mProgressDialog.dismiss();
                                Snackbar.make(relativeLayout2, "登陆成功！", Snackbar.LENGTH_SHORT).show();
                                editor=pref.edit();
                                if(rememberPass.isChecked()){
                                    //检查复选框是否被选中
                                    editor.putBoolean("remember_password",true);
                                    editor.putString("account",loginaccount);
                                    editor.putString("password",loginpassword);
                                }else{
                                    editor.clear();
                                }
                                editor.apply();
                                Intent intent = new Intent();
                                intent.setClass(SignAndLogin.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                mProgressDialog.dismiss();
                                Snackbar.make(relativeLayout2, "登陆失败！", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                    return;
                }

                account.setVisibility(View.VISIBLE);
                pass.setVisibility(View.VISIBLE);
                rememberPass.setVisibility(View.VISIBLE);


                final ChangeBounds bounds = new ChangeBounds();
                bounds.setDuration(1500);
                bounds.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {


                        ObjectAnimator animator1 = ObjectAnimator.ofFloat(login, "translationX", mainLinear.getWidth() / 2 - relativeLayout.getWidth() / 2 - login.getWidth() / 2);
                        ObjectAnimator animator2 = ObjectAnimator.ofFloat(img, "translationX", (relativeLayout.getX()));
                        ObjectAnimator animator3 = ObjectAnimator.ofFloat(login, "rotation", 0);

                        ObjectAnimator animator4 = ObjectAnimator.ofFloat(account, "alpha", 0, 1);
                        ObjectAnimator animator5 = ObjectAnimator.ofFloat(pass, "alpha", 0, 1);
                        ObjectAnimator animator6 = ObjectAnimator.ofFloat(rememberPass, "alpha", 0, 1);

                        ObjectAnimator animator7 = ObjectAnimator.ofFloat(signUp, "rotation", 90);
                        ObjectAnimator animator8 = ObjectAnimator.ofFloat(signUp, "y", relativeLayout.getHeight() / 2);
                        ObjectAnimator animator9 = ObjectAnimator.ofFloat(account2, "alpha", 1, 0);

                        ObjectAnimator animator10 = ObjectAnimator.ofFloat(confirmPass, "alpha", 1, 0);
                        ObjectAnimator animator11 = ObjectAnimator.ofFloat(pass2, "alpha", 1, 0);
                        ObjectAnimator animator12 = ObjectAnimator.ofFloat(login, "y", signUp.getY());

                        ObjectAnimator animator13 = ObjectAnimator.ofFloat(back, "translationX", -img.getX());
                        ObjectAnimator animator14 = ObjectAnimator.ofFloat(login, "scaleX", 2);
                        ObjectAnimator animator15 = ObjectAnimator.ofFloat(login, "scaleY", 2);

                        ObjectAnimator animator16 = ObjectAnimator.ofFloat(signUp, "scaleX", 1);
                        ObjectAnimator animator17 = ObjectAnimator.ofFloat(signUp, "scaleY", 1);
                        ObjectAnimator animator18 = ObjectAnimator.ofFloat(logo, "x", logo.getX()+relativeLayout2.getWidth());


                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(animator1, animator2, animator3, animator4, animator5, animator6, animator7,
                                animator8, animator9, animator10, animator11, animator12, animator13, animator14, animator15, animator16, animator17,animator18);
                        set.setDuration(1500).start();

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {

                        account2.setVisibility(View.INVISIBLE);
                        pass2.setVisibility(View.INVISIBLE);
                        confirmPass.setVisibility(View.INVISIBLE);

                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });

                TransitionManager.beginDelayedTransition(mainLinear, bounds);

                params.weight = (float) 0.75;
                params2.weight = (float) 4.25;

                relativeLayout.setLayoutParams(params);
                relativeLayout2.setLayoutParams(params2);


            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Snackbar.make(mainFrame, "再按一次退出！", Snackbar.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
