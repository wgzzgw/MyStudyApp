package com.example.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.R;
import com.example.activity.ChatActivity;
import com.example.util.TimeUtils;

import java.util.List;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/26.
 */
/*
* 会话搜索适配器
* */
public class ConSearchAdapter extends BaseAdapter{
    private List<Conversation> mDatas;//数据源
    private Activity mContext;
    private UserInfo mUserInfo;
    public ConSearchAdapter(Activity context, List<Conversation> data) {
        this.mContext = context;
        this.mDatas = data;
    }
    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        }
        return mDatas.size();
    }
    @Override
    public Conversation getItem(int position) {
        if (mDatas == null) {
            return null;
        }
        return mDatas.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final Conversation convItem = mDatas.get(position);
        final MyHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_conv_list, null);
            viewHolder=new MyHolder();
            viewHolder.conv_img=(ImageView)convertView.findViewById(R.id.conv_friend_photo);
            viewHolder.conv_name=(TextView)convertView.findViewById(R.id.conv_friendname);
            viewHolder.conv_message=(TextView)convertView.findViewById(R.id.conv_message);
            viewHolder.conv_time=(TextView)convertView.findViewById(R.id.conv_time);
            viewHolder.conv_verification_num=(TextView)convertView.findViewById(R.id.conv_verification_num);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(MyHolder)convertView.getTag();
        }
        mUserInfo = (UserInfo) convItem.getTargetInfo();
        if (mUserInfo != null) {
            mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int status, String desc, Bitmap bitmap) {
                    if (status == 0) {
                        viewHolder.conv_img.setImageBitmap(bitmap);
                    } else {
                        viewHolder.conv_img.setImageResource(R.drawable.jmui_head_icon);
                    }
                }
            });
        } else {
            viewHolder.conv_img.setImageResource(R.drawable.jmui_head_icon);
        }
        viewHolder.conv_name.setText(mUserInfo.getDisplayName());
        if(convItem.getLatestMessage()!=null) {
            viewHolder.conv_time.setText(TimeUtils.ms2date("MM-dd HH:mm", convItem.getLatestMessage().getCreateTime()));
            if (convItem.getLatestMessage().getContent() instanceof TextContent) {
                viewHolder.conv_message.setText(((TextContent) convItem.getLatestMessage().getContent()).getText());
            } else if (convItem.getLatestMessage().getContent() instanceof VoiceContent) {
                viewHolder.conv_message.setText("[语音]");
            } else if (convItem.getLatestMessage().getContent() instanceof ImageContent) {
                viewHolder.conv_message.setText("[图片]");
            }else if(convItem.getLatestMessage().getContent()instanceof PromptContent){
                viewHolder.conv_message.setText(((PromptContent)(convItem.getLatestMessage().getContent())).getPromptText());
            }
        }else{
            viewHolder.conv_time.setVisibility(View.INVISIBLE);
            viewHolder.conv_message.setText("");
        }
        //获取会话包含的未读消息数
        if (convItem.getUnReadMsgCnt() != 0) {
            viewHolder.conv_verification_num.setVisibility(View.VISIBLE);
            if (convItem.getUnReadMsgCnt() < 99)
                viewHolder.conv_verification_num.setText(convItem.getUnReadMsgCnt()+"");
            else
                viewHolder.conv_verification_num.setText("99+");
        }else{
            viewHolder.conv_verification_num.setVisibility(View.GONE);
        }
        return convertView;
    }
    class MyHolder{
        ImageView conv_img;
        TextView conv_name;
        TextView conv_message;
        TextView conv_time;
        TextView conv_verification_num;
    }
}
