package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.R;
import com.example.activity.FriendInfoActivity;
import com.example.db.FriendEntry;
import com.example.util.ImgUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/15.
 */
/*
* 通讯录listview适配器
* */
public class ListAdapter extends BaseAdapter {
    private static class ViewHolder {
        LinearLayout itemLl;//好友一栏布局
        TextView displayName;//好友展示名
        ImageView avatar;//好友头像
    }
    private Context mContext;
    private LayoutInflater mInflater;
    private List<FriendEntry> mData;//数据源
    public ListAdapter(Context context, List<FriendEntry> list) {
        this.mContext = context;
        this.mData = list;
        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.friend_item, parent, false);
            holder.itemLl = (LinearLayout) convertView.findViewById(R.id.frienditem);
            holder.avatar = (ImageView) convertView.findViewById(R.id.friend_photo);
            holder.displayName = (TextView) convertView.findViewById(R.id.friendname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //所有好友列表
        final FriendEntry friend = mData.get(position);
        final String user = friend.username;
        if (friend.avatar != null) {
            if (new File(friend.avatar).exists()) {
                //本地有此好友的头像，直接获取设置
                ImgUtils.loadbit(BitmapFactory.decodeFile(friend.avatar),holder.avatar);
            } else {
                //本地无好友的头像，服务端下载获取
                JMessageClient.getUserInfo(user, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        if (i == 0) {
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int i, String s, Bitmap bitmap) {
                                    if (i == 0) {
                                        //成功获取好友头像
                                        ImgUtils.loadbit(bitmap,holder.avatar);
                                    } else {
                                        ImgUtils.load(R.drawable.jmui_head_icon,holder.avatar);
                                    }
                                }
                            });
                        } else {
                            ImgUtils.load(R.drawable.jmui_head_icon,holder.avatar);
                        }
                    }
                });
            }
        } else {
            ImgUtils.load(R.drawable.jmui_head_icon,holder.avatar);
        }
        final long[] uid = new long[1];
        uid[0] = friend.uid;
        String nickName = friend.nickName;
        holder.displayName.setText(nickName);
        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("name",user);
                //表示是通讯录点进来的
                intent.putExtra("flag","2");
                intent.setClass(mContext, FriendInfoActivity.class);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }
}
