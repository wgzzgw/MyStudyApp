package com.example.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by yy on 2018/5/11.
 */
/*
单位转换工具类
 */
public class ConvertUtil {
    /*
    * dp转px
    * */
    public static int inDp(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
