package com.example.util;

import java.util.Comparator;

import cn.jpush.im.android.api.model.Conversation;

/**
 * Created by yy on 2018/5/24.
 */
/*
* 比较两个会话时间上的新旧
* */
public class SortConvList implements Comparator<Conversation> {
    @Override
    public int compare(Conversation o, Conversation o2) {
        if (o.getLatestMessage().getCreateTime()> o2.getLatestMessage().getCreateTime()) {
            return -1;
        } else if (o.getLatestMessage().getCreateTime() < o2.getLatestMessage().getCreateTime()) {
            return 1;
        }
        return 0;
    }
}
