package com.example.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.R;
import com.example.util.SharePreferenceManager;

import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.OnCameraCallbackListener;
import cn.jiguang.imui.chatinput.listener.OnClickEditTextListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.listener.RecordVoiceListener;
import cn.jiguang.imui.chatinput.record.RecordVoiceButton;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;

import static cn.jiguang.imui.chatinput.ChatInputView.KEYBOARD_STATE_HIDE;
import static cn.jiguang.imui.chatinput.ChatInputView.KEYBOARD_STATE_INIT;
import static cn.jiguang.imui.chatinput.ChatInputView.KEYBOARD_STATE_SHOW;

/**
 * Created by yy on 2018/5/19.
 */

public class ChatView extends RelativeLayout {
    private ImageView mTitleBarBack;//bar 返回键
    private TextView mTitleBarTitle;//bar 标题
    private ImageView mTitleOptionsImg;//bar 右侧选项键
    private LinearLayout mTitle;//bar 布局
    private MessageList mMsgList;//消息接收与发送列表
    private ChatInputView mChatInput;//消息类型发送选择区
    private RecordVoiceButton mRecordVoiceBtn;//语音按钮

    private boolean mHasInit;
    private boolean mHasKeyboard;
    private int mHeight;

    private OnKeyboardChangedListener mKeyboardListener;
    private OnSizeChangedListener mSizeChangedListener;
    public ChatView(Context context) {
        super(context);
    }
    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void initModule() {
        mTitleBarBack=(ImageView)findViewById(R.id.title_bar_back);
        mTitleBarTitle=(TextView)findViewById(R.id.title_bar_title);
        mTitleOptionsImg=(ImageView)findViewById(R.id.title_options_img);
        mTitle=(LinearLayout)findViewById(R.id.title);
        mMsgList=(MessageList)findViewById(R.id.msg_list);
        mChatInput=(ChatInputView)findViewById(R.id.chat_input);
        mRecordVoiceBtn = mChatInput.getRecordVoiceButton();
        //固定大小
        mMsgList.setHasFixedSize(true);
        mChatInput.setMenuContainerHeight(SharePreferenceManager.getCachedKeyboardHeight());
    }
    public void setTitle(String title) {
        mTitleBarTitle.setText(title);
    }
    public void setMenuClickListener(OnMenuClickListener listener) {
        mChatInput.setMenuClickListener(listener);
    }
    public void setAdapter(MsgListAdapter adapter) {
        mMsgList.setAdapter(adapter);
    }
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mMsgList.setLayoutManager(layoutManager);
    }
    public void setRecordVoiceFile(String path, String fileName) {
        mRecordVoiceBtn.setVoiceFilePath(path, fileName);
    }
    public void setCameraCaptureFile(String path, String fileName) {
        mChatInput.setCameraCaptureFile(path, fileName);
    }
    public void setRecordVoiceListener(RecordVoiceListener listener) {
        mRecordVoiceBtn.setRecordVoiceListener(listener);
    }
    public void setOnCameraCallbackListener(OnCameraCallbackListener listener) {
        mChatInput.setOnCameraCallbackListener(listener);
    }
   public void setKeyboardChangedListener(OnKeyboardChangedListener listener) {
        mKeyboardListener = listener;
    }
    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mSizeChangedListener = listener;
    }
    public void setOnTouchListener(OnTouchListener listener) {
        mMsgList.setOnTouchListener(listener);
    }
    public void setOnTouchEditTextListener(OnClickEditTextListener listener) {
        mChatInput.setOnClickEditTextListener(listener);
    }
    //使用代码主动去调用控件的点击事件（模拟人手去触摸控件）
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }
    /*
    * 1）参数changed表示view有新的尺寸或位置；
    * 2）参数l表示相对于父view的Left位置；
    * 3）参数t表示相对于父view的Top位置；
    * 4）参数r表示相对于父view的Right位置；
    * 5）参数b表示相对于父view的Bottom位置。.
    * */
    /*
    * 在view给其孩子设置尺寸和位置时被调用。子view，包括孩子在内，
    * 必须重写onLayout(boolean, int, int, int, int)方法，
    * 并且调用各自的layout(int, int, int, int)方法。
    * */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
           /* mHeight = b;*/
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_INIT);
            }
        } else {
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_INIT);
            }
          /*  mHeight = mHeight < b ? b : mHeight;*/
        }

       /* if (mHasInit && mHeight > b) {
            mHasKeyboard = true;
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_SHOW);
            }
        }
        if (mHasInit && mHasKeyboard && mHeight == b) {
            mHasKeyboard = false;
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_HIDE);
            }
        }*/
    }

    public ChatInputView getChatInputView() {
        return mChatInput;
    }

    public MessageList getMessageListView() {
        return mMsgList;
    }

    public void setMenuHeight(int height) {
        mChatInput.setMenuContainerHeight(height);
    }

    public interface OnKeyboardChangedListener {
        public void onKeyBoardStateChanged(int state);
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
