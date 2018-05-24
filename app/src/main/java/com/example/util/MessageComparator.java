package com.example.util;

import com.example.message.MyMessage;

import java.util.Comparator;

/**
 * Created by yy on 2018/5/23.
 */
/*
* 消息时间比较器
* */
public class MessageComparator implements Comparator<MyMessage> {
    public int compare(MyMessage o1, MyMessage o2) {
        return (o2.getMessage().getCreateTime()+"").compareTo(
                (o1.getMessage().getCreateTime()+"")
        );
    }
}
