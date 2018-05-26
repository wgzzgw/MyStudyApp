package com.example.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.util.SortConvList;
import com.example.util.TimeUtils;
import com.example.view.ConversationListView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import cn.jiguang.imui.commons.ViewHolder;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by yy on 2018/5/24.
 */
/*
* 会话列表适配器
* */
public class ConversationListAdapter extends BaseAdapter {
    private List<Conversation> mDatas;//数据源
    private Activity mContext;
    private UserInfo mUserInfo;
    private ConversationListView mConversationListView;
    private UIHandler mUIHandler = new UIHandler(this);//将自身适配器与handler关联起来
    public ConversationListAdapter(Activity context, List<Conversation> data, ConversationListView convListView) {
        this.mContext = context;
        this.mDatas = data;
        this.mConversationListView = convListView;
    }
    /**
     * 收到消息后将会话置顶
     *
     * @param conv 要置顶的会话
     */
    public void setToTop(Conversation conv) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //有会话
                mConversationListView.setNoNullConversation(true);
            }
        });
        //如果是旧的会话，即在mDatas数据中存在
        for (Conversation conversation : mDatas) {
            if (conv.getId().equals(conversation.getId())) {
                    //因为后面要改变排序位置,这里要删除
                    mDatas.remove(conversation);
                    //添加到第一位，其它顺序往后移动
                    mDatas.add(0, conv);
                    mUIHandler.sendEmptyMessageDelayed(1, 200);
                    return;
            }
        }
            if (mDatas.size() == 0) {
                mDatas.add(conv);
            } else {
                //如果是新的会话,就插入到list中
                //添加到第一位，其它顺序往后移动
                mDatas.add(0, conv);
            }
        mUIHandler.sendEmptyMessageDelayed(1,200);
    }

    /*
    * 弱引用，与强引用（我们常见的引用方式）相对；特点是：GC在回收时会忽略掉弱引用对象
    * （忽略掉这种引用关系），即：就算弱引用指向了某个对象，但只要该对象没有被强引用指向，该对象也会被GC检查时回收掉
    * */
    private static class UIHandler extends Handler {
        //对外持有对ConversationListAdapter的弱引用，防止内存泄漏
        private final WeakReference<ConversationListAdapter> mAdapter;

        public UIHandler(ConversationListAdapter adapter) {
            mAdapter = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ConversationListAdapter adapter = mAdapter.get();
            if (adapter != null) {
                switch (msg.what) {
                    case 1:
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }
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

    public void deleteConversation(Conversation conversation) {
        mDatas.remove(conversation);
        notifyDataSetChanged();
        Toast.makeText(mContext,"删除会话成功",Toast.LENGTH_SHORT).show();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final Conversation convItem = mDatas.get(position);
        final MyHolder viewHolder;
        mConversationListView.setUnReadMsg(JMessageClient.getAllUnReadMsgCount());
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
    public void addAndSort(Conversation conv) {
        boolean isExist=false;
        for(int i=0;i<mDatas.size();i++){
            if(mDatas.get(i).getId()==conv.getId())
            {
                //说明这个会话在会话列表已存在，无须添加，只需排序
                isExist=true;
            }
        }
        if(!isExist) {
            mDatas.add(conv);
        }
        SortConvList sortConvList = new SortConvList();
        Collections.sort(mDatas, sortConvList);
        notifyDataSetChanged();
    }
}
