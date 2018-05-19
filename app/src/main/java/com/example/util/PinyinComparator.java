package com.example.util;

import com.example.db.FriendEntry;

import java.util.Comparator;

/**
 * Created by yy on 2018/5/19.
 */
public class PinyinComparator implements Comparator<FriendEntry> {

    public int compare(FriendEntry o1, FriendEntry o2) {
        if (o1.getLetter().equals("@")
                || o2.getLetter().equals("#")) {
            return -1;
        } else if (o1.getLetter().equals("#")
                || o2.getLetter().equals("@")) {
            return 1;
        } else {
            return o1.getLetter().compareTo(o2.getLetter());
        }
    }
}
