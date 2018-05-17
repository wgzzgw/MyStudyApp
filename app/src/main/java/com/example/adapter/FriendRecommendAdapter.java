package com.example.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MyApplication;
import com.example.R;
import com.example.activity.FriendInfoActivity;
import com.example.db.FriendRecommendEntry;
import com.example.util.ImgUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by yy on 2018/5/15.
 */
/*
* 推荐好友列表适配器
* */
public class FriendRecommendAdapter extends BaseAdapter {
    private Activity mContext;
    private List<FriendRecommendEntry> mList = new ArrayList<>();
    private LayoutInflater mInflater;
    private LinearLayout LayoutAdd;
    private LinearLayout Layoutdelete;
    private boolean isTrue=false;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(isTrue){
                        LayoutAdd.setVisibility(View.INVISIBLE);
                        Layoutdelete.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public FriendRecommendAdapter(Activity context, List<FriendRecommendEntry> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_friend_recomend, null);
        }
        final ImageView headicon = (ImageView) convertView.findViewById(R.id.item_head_icon);
        TextView name = (TextView) convertView.findViewById(R.id.friendname);
        final TextView addbutton = (TextView) convertView.findViewById(R.id.txt_add);
        TextView delbutton = (TextView) convertView.findViewById(R.id.txt_del);
        final FriendRecommendEntry item = mList.get(position);
        LayoutAdd=(LinearLayout)convertView.findViewById(R.id.layout_add);
        Layoutdelete=(LinearLayout)convertView.findViewById(R.id.layout_back);
        if (item.getAvatar() == null) {
            ImgUtils.load(R.drawable.rc_default_portrait, headicon);
        } else {
            JMessageClient.getUserInfo(item.getUsername(), null, new GetUserInfoCallback() {
                @Override
                public void gotResult(int i, String s, UserInfo userInfo) {
                    if (i == 0) {
                      /*  userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int i, String s, Bitmap bitmap) {
                                if (i == 0) {
                                    ImgUtils.loadbit(bitmap, headicon);
                                }
                            }
                        });*/
                        ImgUtils.loadbit(BitmapFactory.decodeFile(userInfo.getAvatarFile().getAbsolutePath()),headicon);
                    }
                }
            });
        }
        name.setText(item.getUsername());
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                * public static void acceptInvitation(java.lang.String targetUsername,
                * java.lang.String appKey,
                * BasicCallback callback)
                * 接受对方的好友请求，操作成功后，对方会出现在自己的好友列表中，双方建立起好友关系。
                * Parameters:
                * targetUsername - 邀请方用户名
                * appKey - 邀请方用户的appKey,如果为空则默认从本应用appKey下查找用户。
                * callback - 结果回调
                * */
                ContactManager.acceptInvitation(item.getUsername(), item.getAppkey(), new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            isTrue=true;
                            DataSupport.deleteAll(FriendRecommendEntry.class, "username=? and sendname=?", item.getUsername(),
                                    MyApplication.getUserEntry().getUsername());
                            Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
                            handler.sendEmptyMessageDelayed(1,2000);
                        }
                    }
                });
            }
        });
        delbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FriendRecommendEntry entry = mList.get(position);
                DataSupport.deleteAll(FriendRecommendEntry.class, "username=? and sendname=?", entry.getUsername(), MyApplication.getUserEntry().getUsername());
                mList.remove(position);
                notifyDataSetChanged();
            }
        });
        headicon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("name",item.getUsername());
                //表示不是通讯录点进来的
                intent.putExtra("flag","1");
                intent.setClass(mContext, FriendInfoActivity.class);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }
}


