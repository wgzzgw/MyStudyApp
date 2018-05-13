package com.example.controller;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.R;
import com.example.adapter.ViewPagerAdapter;
import com.example.fragment.ContactsFragment;
import com.example.fragment.ConversationListFragment;
import com.example.fragment.MeFragment;
import com.example.mystudyapp.MainActivity;
import com.example.view.MainView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yy on 2018/5/12.
 */
/*
* 主界面pager控制器，监听类
* */
public class MainController implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private MainView mMainView;//控制器所在view
    private MainActivity mContext;//控制器所在context
    private ConversationListFragment mConvListFragment;//会话 碎片
    private MeFragment mMeFragment;//我 碎片
    private ContactsFragment mContactsFragment;//通讯录 碎片

    public MainController(MainView mMainView, MainActivity context) {
        this.mMainView = mMainView;
        this.mContext = context;
        setViewPager();
    }
    private void setViewPager() {
        final List<Fragment> fragments = new ArrayList<>();
        // init Fragment
        mConvListFragment = new ConversationListFragment();
        mContactsFragment = new ContactsFragment();
        mMeFragment = new MeFragment();

        fragments.add(mConvListFragment);
        fragments.add(mContactsFragment);
        fragments.add(mMeFragment);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mContext.getSupportFragmentManager(),
                fragments);
        //viewpager设置适配器
        mMainView.setViewPagerAdapter(viewPagerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_msg_btn:
                mMainView.setCurrentItem(0, false);
                break;
            case R.id.actionbar_contact_btn:
                mMainView.setCurrentItem(1, false);
                break;
            case R.id.actionbar_me_btn:
                mMainView.setCurrentItem(2, false);
                break;
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mMainView.setButtonColor(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
