package com.example.mystudyapp;

import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.example.R;
import com.example.controller.MainController;
import com.example.view.MainView;

public class MainActivity extends FragmentActivity {
    private MainView mMainView;//最外层布局
    private MainController mMainController;//主界面viewpager适配器
    private long mExitTime;//监听back键事件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainView = (MainView) findViewById(R.id.main_view);
        mMainView.initModule();
        mMainController = new MainController(mMainView, this);
        mMainView.setOnClickListener(mMainController);
        mMainView.setOnPageChangeListener(mMainController);

    }
    public FragmentManager getSupportFragmentManger() {
        return getSupportFragmentManager();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            Snackbar.make(mMainView, "再按一次退出！", Snackbar.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
