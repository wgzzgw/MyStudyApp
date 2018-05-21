package com.example.message;

import cn.jiguang.imui.commons.models.IUser;

/**
 * Created by yy on 2018/5/19.
 */

public class DefaultUser implements IUser {
    private String id;
    private String displayName;
    private String avatar;
    public DefaultUser(String id, String displayName, String avatar) {
        this.id = id;
        this.displayName = displayName;
        this.avatar = avatar;
    }
    @Override
    public String getAvatarFilePath() {
        return avatar;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
