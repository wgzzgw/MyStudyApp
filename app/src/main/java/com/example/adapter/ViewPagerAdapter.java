package com.example.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by yy on 2018/5/12.
 */
/*
* 主界面viewpager适配器
* */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmList;
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.mFragmList = fragments;
    }
    @Override
    public Fragment getItem(int position) {
        return mFragmList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmList.size();
    }
}
