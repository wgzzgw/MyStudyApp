package com.example.util;

/**
 * Created by yy on 2018/5/21.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期毫秒互换辅助类
 * dataForamt:yyyy-MM-dd //格式
 * yyyy-MM-dd HH:mm:ss
 */
public class TimeUtils {
    //时间转化毫秒
    public static long date2ms(String dateForamt,String time) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat(dateForamt).parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
    }

    //毫秒转化成日期
    public static String ms2date(String dateForamt,long ms){
        Date date = new Date(ms);
        SimpleDateFormat format = new SimpleDateFormat(dateForamt);
        return format.format(date);
    }

    /**时间戳转日期*/
    public static String unix2Date(String dateForamt, long ms) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateForamt);
        String sd = sdf.format(new Date(ms*1000));
        return sd;
    }
}

