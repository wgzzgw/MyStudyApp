package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.R;
import com.example.activity.FriendInfoActivity;
import com.example.controller.ContactsController;
import com.example.db.FriendEntry;
import com.example.util.HanziToPinyin;
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
public class ListAdapter extends BaseAdapter implements SectionIndexer {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<FriendEntry> mData;//数据源
    private int[] mSectionIndices;
    private String[] mSectionLetters;
    @Override
    public Object[] getSections() {
        return  mSectionLetters;
    }
    /*
    * 根据分类列的索引号获得该序列的首个位置
    * */
    @Override
    public int getPositionForSection(int sectionIndex) {
        if (null == mSectionIndices || mSectionIndices.length == 0) {
            return 0;
        }

        if (sectionIndex >= mSectionIndices.length) {
            sectionIndex = mSectionIndices.length - 1;
        } else if (sectionIndex < 0) {
            sectionIndex = 0;
        }
        return mSectionIndices[sectionIndex];
    }

    /*
    * 通过该项的位置，获得所在分类组的索引号
    * */
    @Override
    public int getSectionForPosition(int position) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (position < mSectionIndices[i]) {
                    return i - 1;
                }
            }
            return mSectionIndices.length - 1;
        }
        return -1;
    }

    private static class ViewHolder {
        LinearLayout itemLl;//好友一栏布局
        TextView displayName;//好友展示名
        ImageView avatar;//好友头像
        TextView tv;//最上面的导航字母
    }
    public ListAdapter(Context context, List<FriendEntry> list) {
        this.mContext = context;
        this.mData = list;
        mInflater = LayoutInflater.from(mContext);
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
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
            holder.tv=(TextView)convertView.findViewById(R.id.tv);
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
        String pinyin;
        if(!TextUtils.isEmpty(friend.getNoteName())&&!friend.getNoteName().trim().equals("")){
            holder.displayName.setText(friend.getNoteName());
        }else
        if(!TextUtils.isEmpty(friend.getNickName())) {
            holder.displayName.setText(friend.getNickName());
        }else{
            holder.displayName.setText(friend.getUsername());
        }
        pinyin=holder.displayName.getText().toString();
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
        String letter=getLetter(pinyin);
        int p=-1;
        for(int a = 0; a<ContactsController.letter.length; a++){
            if(ContactsController.letter[a].equals(letter)){
                p=a;
                break;
            }
        }
        holder.tv.setVisibility(p==position? View.VISIBLE
                : View.GONE);
        holder.tv.setText(letter);
        return convertView;
    }

    /*
    * 基于某个字段的分组，这个数据源必须是在这个字段上是有序的！
    * */
    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        if (mData.size() > 0) {
            char lastFirstChar = mData.get(0).getLetter().charAt(0);
            sectionIndices.add(0);
            for (int i = 1; i < mData.size(); i++) {
                if (mData.get(i).letter.charAt(0) != lastFirstChar) {
                    lastFirstChar = mData.get(i).letter.charAt(0);
                    sectionIndices.add(i);
                }
            }
            int[] sections = new int[sectionIndices.size()];
            for (int i = 0; i < sectionIndices.size(); i++) {
                sections[i] = sectionIndices.get(i);
            }
            return sections;
        }
        return null;
    }

    private String[] getSectionLetters() {
        if (null != mSectionIndices) {
            String[] letters = new String[mSectionIndices.length];
            for (int i = 0; i < mSectionIndices.length; i++) {
                letters[i] = mData.get(mSectionIndices[i]).getLetter();
            }
            return letters;
        }
        return null;
    }
    public int getSectionForLetter(String letter) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (mSectionLetters[i].equals(letter)) {
                    return mSectionIndices[i];
                }
            }
        }
        return -1;
    }
    private String getLetter(String name) {
        String letter;
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                .get(name);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (token.type == HanziToPinyin.Token.PINYIN) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        String sortString = sb.toString().substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase();
        } else {
            letter = "#";
        }
        return letter;
    }
}
