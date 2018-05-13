package com.example.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yy on 2018/5/12.
 */
/*
* 首页viewPager
* */
public class ScrollControlViewPager  extends ViewPager {
    private final String TAG = ScrollControlViewPager.class.getSimpleName();
    private boolean scroll = false;//禁止viewpager左右滑动,减少滑动冲突处理
    public ScrollControlViewPager(Context context) {
        super(context);
    }
    public ScrollControlViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     * @param scroll
     */
    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }
}
