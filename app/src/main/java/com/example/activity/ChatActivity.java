package com.example.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;


import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.R;
import com.example.dialog.MyAlertDialog;
import com.example.event.Event;
import com.example.event.EventType;
import com.example.message.DefaultUser;
import com.example.message.MyMessage;
import com.example.mystudyapp.MainActivity;
import com.example.util.MessageComparator;
import com.example.util.SharePreferenceManager;
import com.example.util.StringUtils;
import com.example.util.TimeUtils;
import com.example.view.ChatView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.jiguang.api.JCoreInterface;
import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.listener.RecordVoiceListener;
import cn.jiguang.imui.chatinput.model.FileItem;
import cn.jiguang.imui.commons.BitmapLoader;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.api.BasicCallback;

import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_IMAGE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_VOICE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_CUSTOM;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_FILE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_IMAGE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_LOCATION;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_VIDEO;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_VOICE;
import static cn.jpush.im.android.JMessage.mContext;

public class ChatActivity extends AppCompatActivity  implements ChatView.OnSizeChangedListener
,ChatView.OnKeyboardChangedListener, View.OnTouchListener,ChatView.OnCloseListener{
    private InputMethodManager mImm;
    private Window mWindow;
    private ChatView mChatView;
    /*int heightDifference = 0;*/
    private MsgListAdapter<MyMessage> mAdapter;
    private ImageLoader imageLoader;
    //撤回消息的视图msgid
    private String msgID = "";
    private UserInfo userInfo;
    private Conversation mConv;//会话
    private String name;
    private String imgSend = "R.drawable.ironman";
    private String imgRecrive = "R.drawable.ironman";
    private final int REQUEST_RECORD_VOICE_PERMISSION = 0x0001;//录音请求码
    private final int REQUEST_PHOTO_PERMISSION = 0x0002;//图册请求码
    private final int REQUEST_CAMERA_PERMISSION = 0x0003;//相机请求码
    /*private int mOffset = 18;//一页的消息总数
    private int mStart;//下一页加载更多的起始位置*/
    private List<Message> mMsgList = new ArrayList<>();
    private List<MyMessage> mData = new ArrayList<>();//列表数据源
    private UserInfo targetUserInfo;//会话对象
    /*boolean mLast=true;*/
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 1:
                    Log.d("cc", "initMsgAdapter: "+mData.size());
                    //由于图片和语音代码块存在异步操作，可能导致mData没有顺序排序
                    Collections.sort(mData,new MessageComparator());
                    mAdapter.addToEnd(mData);//加载历史数据的最新18条
                    mAdapter.notifyDataSetChanged();
                    mAdapter.getLayoutManager().scrollToPosition(0);
                    break;
                default:
                    break;
            }
        }
    };
    //初始化adapter
    private void initMsgAdapter() {
        imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                Glide.with(getApplicationContext())
                        .load(string)
                        .placeholder(R.mipmap.icon_user)
                        .centerCrop()
                        .dontAnimate()
                        .into(avatarImageView);
            }

            @Override
            public void loadImage(ImageView imageView, String string) {
                Glide.with(getApplicationContext())
                        .load(string)
                        .placeholder(R.mipmap.icon_user)
                        .override(500, 500)
                        .dontAnimate()
                        .into(imageView);
            }
        };
        /**
         * 1、Sender Id: 发送方 Id(唯一标识)。
         * 2、HoldersConfig，可以用这个对象来构造自定义消息的 ViewHolder 及布局界面。
         * 如果不自定义则使用默认的布局
         * 3、ImageLoader 的实例，用来展示头像。如果为空，将会隐藏头像。
         */
        final MsgListAdapter.HoldersConfig holdersConfig = new MsgListAdapter.HoldersConfig();
        mAdapter = new MsgListAdapter<MyMessage>(getIntent().getStringExtra("userid"),
                holdersConfig, imageLoader);
        //单击消息事件
        mAdapter.setOnMsgClickListener(new MsgListAdapter.OnMsgClickListener<MyMessage>() {
            @Override
            public void onMessageClick(MyMessage message) {
                // do something
                Toast.makeText(ChatActivity.this, "点击了消息", Toast.LENGTH_SHORT).show();
            }
        });
        //长按消息
        mAdapter.setMsgLongClickListener(new MsgListAdapter.OnMsgLongClickListener<MyMessage>() {
            @Override
            public void onMessageLongClick(final MyMessage message) {
                String[] strings;
                msgID = message.getMsgId();
                //判断消息类型
               /*if (message.getType() == SEND_TEXT
                        || message.getType() == SEND_CUSTOM
                        || message.getType() == SEND_FILE
                        || message.getType() == SEND_IMAGE
                        || message.getType() == SEND_LOCATION
                        || message.getType() == SEND_VIDEO) {
                    strings = new String[]{"复制", "撤回", "删除"};
                } else {
                   //接收的消息
                    strings = new String[]{"复制", "转发", "删除"};
                }*/
                strings = new String[]{"复制", "撤回", "删除"};
                final MyAlertDialog dialog = new MyAlertDialog(ChatActivity.this,
                        strings
                        , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //复制：当消息类型为文字的时候才可以复制
                                if (message.getType().equals(SEND_TEXT)
                                        || message.getType() == SEND_TEXT
                                        || message.getType() == RECEIVE_TEXT) {
                                    if (Build.VERSION.SDK_INT > 11) {
                                        //剪切板
                                        ClipboardManager clipboard = (ClipboardManager) mContext
                                                .getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Simple text", message.getText());
                                        clipboard.setPrimaryClip(clip);
                                    } else {
                                        android.text.ClipboardManager clip = (android.text.ClipboardManager) mContext
                                                .getSystemService(Context.CLIPBOARD_SERVICE);
                                        if (clip.hasText()) {
                                            clip.getText();
                                        }
                                    }
                                    Toast.makeText(ChatActivity.this, "复制成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChatActivity.this, "复制类型出错！", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case 1:
                                //撤回：发送方才可撤回
                                mConv.retractMessage(message.getMessage(), new BasicCallback() {
                                    @Override
                                    public void gotResult(int i, String s) {
                                        if (i == 0) {
                                            Toast.makeText(ChatActivity.this, "成功撤回了一条消息！", Toast.LENGTH_SHORT).show();
                                            //适配器数据源删除此消息
                                            mAdapter.deleteById(msgID);
                                            mAdapter.updateMessage(message);
                                        } else {
                                            Toast.makeText(ChatActivity.this, "撤回失败！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case 2:
                                //删除
                                //移除视图
                                mAdapter.deleteById(msgID);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(ChatActivity.this,"删除消息成功",Toast.LENGTH_SHORT);
                                break;
                            default:
                                break;
                        }
                    }
                });
                dialog.initDialog(Gravity.CENTER);
                dialog.dialogSize(200, 0, 0, 55);
            }
        });
        //点击头像
        mAdapter.setOnAvatarClickListener(new MsgListAdapter.OnAvatarClickListener<MyMessage>() {
            @Override
            public void onAvatarClick(MyMessage message) {
                /*DefaultUser userInfo = (DefaultUser) message.getFromUser();*/
                Intent intent;
                if (message.getType() == SEND_TEXT||message.getType() ==SEND_IMAGE||message.getType() ==SEND_VOICE) {
                    intent = new Intent(ChatActivity.this, UserActivity.class);
                } else{
                    intent = new Intent(ChatActivity.this, FriendInfoActivity.class);
                    intent.putExtra("flag", "1");
                    intent.putExtra("name", message.getFromUser().getId());
                }
                startActivity(intent);
            }
        });
        //重新发送
        mAdapter.setMsgResendListener(new MsgListAdapter.OnMsgResendListener<MyMessage>() {
            @Override
            public void onMessageResend(final MyMessage message) {
                // resend message here
                Message msg = message.getMessage();
                JMessageClient.sendMessage(msg);
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        mAdapter.updateMessage(message);
                    }
                });
            }
        });
        //加载更多
        /*
        * 滚动列表加载历史消息（注意：如果使用了 PullToRefreshLayout 跳过这个部分）
        * 设置监听 OnLoadMoreListener，当滚动列表时就会触发 onLoadMore 事件
        * */
        /*mAdapter.setOnLoadMoreListener(new MsgListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalCount) {
                //当滑到与mData.size()一样多的数据时，进行加载下一页
                if (totalCount == mData.size()) {
                    loadNextPage();
                }
            }
        });*/
        mChatView.setAdapter(mAdapter);
        handler.sendEmptyMessageDelayed(1,500);
    }
  /*  private  void loadNextPage() {
        if(mLast==false) return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLast = false;
                if (mConv != null) {
                    //会话中消息按时间降序排列，从其中的offset位置，获取limit条数的消息
                    Log.d("z", "run: " + "mStart" + mStart);
                    Log.d("z", "premdata: " + mData.size());
                    List<Message> msgList = mConv.getMessagesFromNewest(mStart, 18);
                    if (msgList != null) {
                        for (Message msg : msgList) {
                            messageinit(msg);
                        }
                        if (msgList.size() > 0) {
                            mOffset = msgList.size();
                        } else {
                            mOffset = 0;
                        }
                        Collections.sort(mData, new MessageComparator());
                        //补充加载出的数据到mData
                        if (mData.size() == mStart + mOffset) {
                            mAdapter.addToEnd(mData.subList(mStart, mStart + mOffset));
                        }
                        Log.d("c", "handleMessage: " + mData.size());
                        mStart += mOffset;
                        mLast = true;
                    }
                }
            }
        },1000);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindow = getWindow();
        name = getIntent().getStringExtra("userid");
        findAllViews();
        //注册接收事件
       JMessageClient.registerEventReceiver(this);
        init();
        mConv = JMessageClient.getSingleConversation(getIntent().getStringExtra("username"));
        if (mConv == null) {
            mConv = Conversation.createSingleConversation(getIntent().getStringExtra("username"));
        }
        targetUserInfo=(UserInfo)mConv.getTargetInfo();
        //通知会话列表添加会话
        EventBus.getDefault().post(new Event.Builder()
                .setType(EventType.createConversation)
                .setConversation(mConv)
                .build());
         /*
        * public abstract java.util.List<Message> getMessagesFromNewest(int offset,int limit)
        * 会话中消息按时间降序排列，从其中的offset位置，获取limit条数的消息.
        * */
       /* this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);//获取聊天室的消息，最新的18条*/
        this.mMsgList=mConv.getAllMessage();
        mData.clear();
        /*mStart = mOffset;*/
        initentermessage();
        initMsgAdapter();
        mChatView.setTitle(name);
        try {
            imgSend = userInfo.getAvatarFile().toURI().toString();
            imgRecrive = StringUtils.isNull(mConv.getAvatarFile().toURI().toString()) ? "R.mipmap.icon_user" : mConv.getAvatarFile().toURI().toString();
        } catch (Exception e) {
        }
    }
    private  void initentermessage(){
        if (mMsgList.size() > 0) {
            for (final Message message : mMsgList) {
                   messageinit(message);
            }
        }
    }
    private  void messageinit(final Message message){
        if(message.getContent()instanceof PromptContent){
            //撤回的消息,退出重新进来暂不处理
            return ;
        }
        if (message.getContent() instanceof TextContent) {
            if (message.getFromUser().getUserName() != userInfo.getUserName()) {
                //说明是收到的文本消息
                MyMessage jmuiMessage = new MyMessage(((TextContent) message.getContent()).getText(), RECEIVE_TEXT);
                jmuiMessage.setMessage(message);
                jmuiMessage.setText(((TextContent) message.getContent()).getText());
                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(), imgRecrive));
                //收到消息时，添加到集合
                mData.add(jmuiMessage);
            } else {
                //说明是自己发出的文本消息
                MyMessage jmuiMessage = new MyMessage(((TextContent) message.getContent()).getText(), SEND_TEXT);
                jmuiMessage.setMessage(message);
                jmuiMessage.setText(((TextContent) message.getContent()).getText());
                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                jmuiMessage.setUserInfo(new DefaultUser(userInfo.getUserName(), userInfo.getDisplayName(), imgSend));
                //收到消息时，添加到集合
                mData.add(jmuiMessage);
            }
        } else if (message.getContent() instanceof VoiceContent) {
            if (message.getFromUser().getUserName() != userInfo.getUserName()) {
                //说明是接收的语音消息
                final MyMessage jmuiMessage = new MyMessage("", RECEIVE_VOICE);
                jmuiMessage.setMessage(message);
                jmuiMessage.setDuration(((VoiceContent) message.getContent()).getDuration());
                    ((VoiceContent) message.getContent()).downloadVoiceFile(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                jmuiMessage.setMediaFilePath(file.getPath());
                                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                                jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(), imgRecrive));
                                //收到消息时，添加到集合
                                mData.add(jmuiMessage);
                            }
                        }
                    });
            }else {
                //说明是自己发出的语音消息
                final MyMessage jmuiMessage = new MyMessage("", SEND_VOICE);
                jmuiMessage.setMessage(message);
                jmuiMessage.setDuration(((VoiceContent) message.getContent()).getDuration());
                    ((VoiceContent) message.getContent()).downloadVoiceFile(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                jmuiMessage.setMediaFilePath(file.getPath());
                                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                                jmuiMessage.setUserInfo(new DefaultUser(userInfo.getUserName(), userInfo.getDisplayName(), imgSend));
                                //收到消息时，添加到集合
                                mData.add(jmuiMessage);
                            }
                        }
                    });
            }
        } else if (message.getContent() instanceof ImageContent) {
            if (message.getFromUser().getUserName() != userInfo.getUserName()) {
                //说明是接收的图片消息
                final MyMessage jmuiMessage = new MyMessage("", RECEIVE_IMAGE);
                jmuiMessage.setMessage(message);
                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(), imgRecrive));
                    //下载缩略图
                    ((ImageContent) message.getContent()).downloadOriginImage(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                jmuiMessage.setMediaFilePath(file.getPath());
                                mData.add(jmuiMessage);
                            }
                        }
                    });
            } else {
                //说明是自己发出的图片消息
                final MyMessage jmuiMessage = new MyMessage("", SEND_IMAGE);
                jmuiMessage.setMessage(message);
                jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message.getCreateTime()));
                jmuiMessage.setUserInfo(new DefaultUser(userInfo.getUserName(), userInfo.getDisplayName(), imgSend));
                    //下载缩略图
                    ((ImageContent) message.getContent()).downloadOriginImage(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                jmuiMessage.setMediaFilePath(file.getPath());
                                mData.add(jmuiMessage);
                            }
                        }
                    });
                }
        }
    }
    private void init() {
        mChatView.setMenuClickListener(new OnMenuClickListener() {
            @Override
            public boolean onSendTextMessage(CharSequence charSequence) {
                // 输入框输入文字后，点击发送按钮事件
              sendMessage(charSequence.toString());
                return false;
            }

            @Override
            public void onSendFiles(List<FileItem> list) {
                // 选中文件或者录制完视频后，点击发送按钮触发此事件
                sendFiles(list);
            }

            @Override
            public boolean switchToMicrophoneMode() {
                // 点击语音按钮触发事件，显示录音界面前触发此事件
                // 返回 true 表示使用默认的界面，若返回 false 应该自己实现界面

                if ((ActivityCompat.checkSelfPermission(ChatActivity.this,
                        "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatActivity.this,
                        "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatActivity.this,
                        "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                            "android.permission.RECORD_AUDIO",
                            "android.permission.WRITE_EXTERNAL_STORAGE",
                            "android.permission.READ_EXTERNAL_STORAGE"}, REQUEST_RECORD_VOICE_PERMISSION);
                }
               /* if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                } else {
                }*/
                return true;
            }

            @Override
            public boolean switchToGalleryMode() {
                // 点击图片按钮触发事件，显示图片选择界面前触发此事件
                // 返回 true 表示使用默认的界面
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PHOTO_PERMISSION);
                } else {
                }
                return true;
            }

            @Override
            public boolean switchToCameraMode() {
                // 点击拍照按钮触发事件，显示拍照界面前触发此事件
                // 返回 true 表示使用默认的界面
                /*if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                }*/
               /* if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
                       != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatActivity.this,
                       Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                   ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                           Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                   }, REQUEST_CAMERA_PERMISSION);
               }
                File rootDir = ChatActivity.this.getFilesDir();//getFilesDir()方法用于获取/data/data/<application package>/files目录
                String fileDir = rootDir.getAbsolutePath() + "/photo";
                //设置相机存放文件
                mChatView.setCameraCaptureFile(fileDir, "temp_photo");
                return true;*/
              /*  scrollToBottom();
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ChatActivity.this,
                                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION);
                }
                else{
                    File rootDir = ChatActivity.this.getFilesDir();//getFilesDir()方法用于获取/data/data/<application package>/files目录
                    String fileDir = rootDir.getAbsolutePath() + "/photo";
                    //设置相机存放文件
                    mChatView.setCameraCaptureFile(fileDir, "temp_photo");
                }*/
                //TODO
                // BUG:
                // java.lang.ClassCastException: android.widget.RelativeLayout$LayoutParams cannot be cast to android.widget.FrameLayout$LayoutParams
                return false;
            }
        });
        mChatView.setRecordVoiceListener(new RecordVoiceListener() {
            @Override
            public void onStartRecord() {
                //Show record voice interface
                        // 设置存放录音文件目录
                File rootDir = ChatActivity.this.getFilesDir();
                String fileDir = rootDir.getAbsolutePath() + "/voice";
                mChatView.setRecordVoiceFile(fileDir, new DateFormat().format("yyyy_MMdd_hhmmss",
                        Calendar.getInstance(Locale.CHINA)) + "");
            }
            @Override
            public void onFinishRecord(File voiceFile, int duration) {
                try {
                    VoiceContent content = new VoiceContent(voiceFile, duration);
                    Message msg = mConv.createSendMessage(content);
                    JMessageClient.sendMessage(msg);
                    final MyMessage jmuiMessage = new MyMessage("",SEND_VOICE);
                    jmuiMessage.setMessage(msg);
                    //必须设置路径
                    jmuiMessage.setMediaFilePath(voiceFile.getPath());
                    jmuiMessage.setDuration(duration);
                    jmuiMessage.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                    jmuiMessage.setUserInfo(new DefaultUser(JMessageClient.getMyInfo().getUserName(), userInfo.getDisplayName(), imgSend));
                    mAdapter.addToStart(jmuiMessage, true);
                    msg.setOnSendCompleteCallback(new BasicCallback() {
                        @Override
                        public void gotResult(int status, String s) {
                            mAdapter.updateMessage(jmuiMessage);
                            if (status == 0) {
                                //通知会话列表置顶此会话
                                EventBus.getDefault().post(new Event.Builder()
                                        .setType(EventType.sendmessage)
                                        .setConversation(mConv)
                                        .build());
                              Toast.makeText(ChatActivity.this,"成功发送语音",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatActivity.this,"发送语音失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelRecord() {

            }
        });
       /* mChatView.setOnCameraCallbackListener(new OnCameraCallbackListener() {
            @Override
            public void onTakePictureCompleted(final String photoPath) {
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(photoPath, 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            JMessageClient.sendMessage(msg);
                            final MyMessage jmuiMessage = new MyMessage("",SEND_IMAGE);
                            jmuiMessage.setMediaFilePath(photoPath);
                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.addToStart(jmuiMessage, true);
                                }
                            });
                            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                                @Override
                                public void onProgressUpdate(double v) {
                                    jmuiMessage.setProgress(Math.ceil(v * 100) + "%");
                                    mAdapter.updateMessage(jmuiMessage);
                                }
                            });
                            msg.setOnSendCompleteCallback(new BasicCallback() {
                                @Override
                                public void gotResult(int status, String desc) {
                                    mAdapter.updateMessage(jmuiMessage);
                                    if (status == 0) {
                                        Toast.makeText(ChatActivity.this,"图片发送成功",Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this,"图片发送失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            @Override
            public void onStartVideoRecord() {
            }
            @Override
            public void onFinishVideoRecord(String videoPath) {
            }
            @Override
            public void onCancelVideoRecord() {
            }
        });*/
        mChatView.setKeyboardChangedListener(this);
        mChatView.setOnSizeChangedListener(this);
        mChatView.setOnTouchListener(this);
        mChatView.setOnCloseListener(this);
        userInfo = JMessageClient.getMyInfo();
    }

    private void sendFiles(List<FileItem> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (final FileItem item : list) {
            if (item.getType() == FileItem.Type.Image) {
                //图片
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(item.getFilePath(), 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            //图片content创建成功
                            Message msg = mConv.createSendMessage(imageContent);
                            final MyMessage jmuiMessage = new MyMessage("",SEND_IMAGE);
                            jmuiMessage.setMessage(msg);
                            jmuiMessage.setMediaFilePath(item.getFilePath());
                            jmuiMessage.setUserInfo(new DefaultUser(JMessageClient.getMyInfo().getUserName(), userInfo.getDisplayName(), imgSend));
                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.addToStart(jmuiMessage, true);
                                }
                            });
                            //发送进度回调方法
                            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                                @Override
                                public void onProgressUpdate(double v) {
                                    //math.ceil(x)返回大于参数x的最小整数,即对浮点数向上取整.
                                    jmuiMessage.setProgress(Math.ceil(v * 100) + "%");
                                    Log.w("ChatActivity", "Uploading image progress" + Math.ceil(v * 100) + "%");
                                    mAdapter.updateMessage(jmuiMessage);
                                }
                            });
                            //发送完成回调
                            msg.setOnSendCompleteCallback(new BasicCallback() {
                                @Override
                                public void gotResult(int status, String desc) {
                                    mAdapter.updateMessage(jmuiMessage);
                                    if (status == 0) {
                                        //通知会话列表置顶此会话
                                        EventBus.getDefault().post(new Event.Builder()
                                                .setType(EventType.sendmessage)
                                                .setConversation(mConv)
                                                .build());
                                       Toast.makeText(ChatActivity.this,"发送文件成功",Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this,"发送文件失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            JMessageClient.sendMessage(msg);
                        }
                    }
                });
            } else if (item.getType() == FileItem.Type.Video) {
            } else {
                throw new RuntimeException("Invalid FileItem type. Must be Type.Image or Type.Video.");
            }
        }
    }
    /*
   权限授予返回处理
    */
  /*  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你已拒绝了授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_VOICE_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied
                Toast.makeText(ChatActivity.this, "你已拒绝语音权限授予，相关功能将受限",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied
                Toast.makeText(ChatActivity.this, "你已拒绝相机权限授予，相关功能将受限",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PHOTO_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied
                Toast.makeText(ChatActivity.this, "你已拒绝相册权限授予，相关功能将受限.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ChatInputView chatInputView = mChatView.getChatInputView();
                if (view.getId() == chatInputView.getInputView().getId()) {
                    if (chatInputView.getMenuState() == View.VISIBLE && !chatInputView.getSoftInputState()) {
                        chatInputView.dismissMenuAndResetSoftMode();
                        return false;
                    } else {
                        return false;
                    }
                }
                if (chatInputView.getMenuState() == View.VISIBLE) {
                    //用于收缩菜单项
                    chatInputView.dismissMenuLayout();
                }
                if (chatInputView.getSoftInputState()) {
                    View v = getCurrentFocus();
                    if (mImm != null && v != null) {
                        //隐藏软键盘
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        //设置软键盘处理
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        chatInputView.setSoftInputState(false);
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void onKeyBoardStateChanged(int state) {
        switch (state) {
            case ChatInputView.KEYBOARD_STATE_INIT:
                ChatInputView chatInputView = mChatView.getChatInputView();
               /* if (mImm != null) {
                    mImm.isActive();
                }*/
                if (chatInputView.getMenuState() == View.INVISIBLE || (!chatInputView.getSoftInputState()
                        && chatInputView.getMenuState() == View.GONE)) {

                    mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    chatInputView.dismissMenuLayout();
                }
                break;
        }
    }

   /* *//*获取键盘的高度*//*
    private void editTextHeight() {
        *//*
        *getViewTreeObserver：
        * 是一个注册监听视图树的观察者(observer)，
        * 在视图树种全局事件改变时得到通知。这个全局事件不仅还包括整个树的布局，从绘画过程开始，触摸模式的改变等
        * *//*
        mChatView.getChatInputView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //获取当前界面可视部分
                ChatActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = ChatActivity.this.getWindow().getDecorView().getRootView().getHeight();
                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
                if (screenHeight - r.bottom > 0) {
                    heightDifference = screenHeight - r.bottom;
                }
            }
        });
    }*/
    private void findAllViews() {
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.initModule();
       /* editTextHeight();
        mChatView.setMenuHeight(heightDifference);*/
    }

    @Override
    protected void onResume() {
        JCoreInterface.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        JCoreInterface.onPause(this);
        super.onPause();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh - h > 300) {
            if (SharePreferenceManager.getCachedKeyboardHeight() != oldh - h) {
                SharePreferenceManager.setCachedKeyboardHeight(oldh - h);
                mChatView.setMenuHeight(oldh - h);
            }
        }
        scrollToBottom();
    }

    /*滚动到底部*/
    private void scrollToBottom() {
        mAdapter.getLayoutManager().scrollToPosition(0);
    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        JMessageClient.exitConversation();
        super.onDestroy();
    }
    /*发送消息，文本*/
    private void sendMessage(String msg) {
        if (msg.length() == 0) {
            return ;
        }
        TextContent content = new TextContent(msg);
        Message message1 = mConv.createSendMessage(content);
        final MyMessage myMessage = new MyMessage(msg, SEND_TEXT);
        myMessage.setMessage(message1);
        //毫秒转化成日期
        myMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", message1.getCreateTime()));
        myMessage.setUserInfo(new DefaultUser(JMessageClient.getMyInfo().getUserName(), userInfo.getDisplayName(), imgSend));

        message1.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    //通知会话列表置顶此会话
                    EventBus.getDefault().post(new Event.Builder()
                            .setType(EventType.sendmessage)
                            .setConversation(mConv)
                            .build());
                    mAdapter.addToStart(myMessage, true);
                    mChatView.getChatInputView().getInputView().setText("");
                } else {
                    Toast.makeText(ChatActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        JMessageClient.sendMessage(message1);
    }

    @Override
    public void closeChatActivity() {
        returnBtn();
    }

    @Override
    public void clearHistory() {
        MyAlertDialog dialog = new MyAlertDialog(this,
                new String[]{"清空聊天记录", "清空并删除会话"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        //删除会话中的所有消息，但不会删除会话本身
                        if (mConv.deleteAllMessage()) {
                            mAdapter.clear();
                            mData.clear();
                            mAdapter.notifyDataSetChanged();
                           Toast.makeText(ChatActivity.this,"清除记录成功",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        //通知会话列表删除会话
                        EventBus.getDefault().post(new Event.Builder()
                                .setType(EventType.deleteConversation)
                                .setConversation(mConv)
                                .build());
                        //删除会话中的所有消息，同时删除会话本身
                        if (JMessageClient.deleteSingleConversation(getIntent().getStringExtra("username"))) {
                            startActivity(new Intent(ChatActivity.this, MainActivity.class));
                        }
                        break;
                }
            }
        });
        dialog.initDialog(Gravity.RIGHT | Gravity.TOP);
        dialog.dialogSize(200, 0, 0, 55);
    }

    /**
     * 接收消息类事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        Log.d("test", "onEvent: "+"收到");
        final Message msg = event.getMessage();
        Log.d("c", "onEvent: "+msg.getContent());
        //刷新消息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //收到消息的类型为单聊
                if (msg.getTargetType() == ConversationType.single) {
                    UserInfo userInfo = (UserInfo) msg.getTargetInfo();
                    String targetId = userInfo.getUserName();
                    String appKey = userInfo.getAppKey();
                    //判断消息是否在当前会话中
                    if (targetId.equals(targetUserInfo.getUserName()) && appKey.equals(targetUserInfo.getAppKey())) {
                        final MyMessage jmuiMessage;
                        if(msg.getContent()instanceof TextContent){
                            jmuiMessage = new MyMessage(((TextContent)msg.getContent()).getText(),RECEIVE_TEXT);
                            jmuiMessage.setMessage(msg);
                            jmuiMessage.setText(((TextContent)msg.getContent()).getText());
                            jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", msg.getCreateTime()));
                            jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                            mAdapter.addToStart(jmuiMessage, true);
                           //收到消息时，添加到集合
                            mData.add(jmuiMessage);
                            mAdapter.notifyDataSetChanged();
                            //通知会话列表置顶此会话
                            EventBus.getDefault().post(new Event.Builder()
                                    .setType(EventType.sendmessage)
                                    .setConversation(mConv)
                                    .build());
                        }else if(msg.getContent()instanceof VoiceContent){
                            jmuiMessage = new MyMessage("",RECEIVE_VOICE);
                            jmuiMessage.setMessage(msg);
                            jmuiMessage.setDuration(((VoiceContent)msg.getContent()).getDuration());
                            ((VoiceContent)msg.getContent()).downloadVoiceFile(msg, new DownloadCompletionCallback() {
                                @Override
                                public void onComplete(int i, String s, File file) {
                                    if(i==0){
                                        jmuiMessage.setMediaFilePath(file.getPath());
                                        jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", msg.getCreateTime()));
                                        jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                                        Log.d("c", "run: "+(jmuiMessage.getMediaFilePath()));
                                        mAdapter.addToStart(jmuiMessage, true);
                                        //收到消息时，添加到集合
                                        mData.add(jmuiMessage);
                                        mAdapter.notifyDataSetChanged();
                                        //通知会话列表置顶此会话
                                        EventBus.getDefault().post(new Event.Builder()
                                                .setType(EventType.sendmessage)
                                                .setConversation(mConv)
                                                .build());
                                    }
                                }
                            });
                        }else if(msg.getContent()instanceof ImageContent){
                            jmuiMessage = new MyMessage("",RECEIVE_IMAGE);
                            jmuiMessage.setMessage(msg);
                            jmuiMessage.setTimeString(TimeUtils.ms2date("MM-dd HH:mm", msg.getCreateTime()));
                            jmuiMessage.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                            //下载缩略图
                            ((ImageContent)msg.getContent()).downloadOriginImage(msg, new DownloadCompletionCallback() {
                                @Override
                                public void onComplete(int i, String s, File file) {
                                    if(i==0) {
                                        Log.d("c", "onComplete: ");
                                        jmuiMessage.setMediaFilePath(file.getPath());
                                        Log.d("ccc", "run: "+jmuiMessage.getMediaFilePath());
                                        mAdapter.addToStart(jmuiMessage, true);
                                        mAdapter.updateMessage(jmuiMessage);
                                       //收到消息时，添加到集合
                                        mData.add(jmuiMessage);
                                        mAdapter.notifyDataSetChanged();
                                        //通知会话列表置顶此会话
                                        EventBus.getDefault().post(new Event.Builder()
                                                .setType(EventType.sendmessage)
                                                .setConversation(mConv)
                                                .build());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    /*接收到撤回的消息*/
    public void onEvent(MessageRetractEvent event) {
        final Message message = event.getRetractedMessage();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mData.size(); i++) {
                    if (mData.get(i).getMessage().getServerMessageId()== message.getServerMessageId()) {
                        if(mData.get(i).getType()==RECEIVE_TEXT) {
                            mAdapter.delete(mData.get(i));
                            MyMessage message1 = new MyMessage("[对方撤回了一条文本消息]", IMessage.MessageType.RECEIVE_TEXT);
                            message1.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                            mAdapter.addToStart(message1, true);
                            mAdapter.notifyDataSetChanged();
                            mAdapter.updateMessage(message1);
                        }else if(mData.get(i).getType()==RECEIVE_IMAGE){
                            mAdapter.delete(mData.get(i));
                            MyMessage message1 = new MyMessage("[对方撤回了一条图片消息]", IMessage.MessageType.RECEIVE_TEXT);
                            message1.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                            mAdapter.addToStart(message1, true);
                            mAdapter.notifyDataSetChanged();
                            mAdapter.updateMessage(message1);
                        }else if(mData.get(i).getType()==RECEIVE_VOICE){
                            mAdapter.delete(mData.get(i));
                            MyMessage message1 = new MyMessage("[对方撤回了一条语音消息]", IMessage.MessageType.RECEIVE_TEXT);
                            message1.setUserInfo(new DefaultUser(targetUserInfo.getUserName(), targetUserInfo.getDisplayName(),imgRecrive));
                            mAdapter.addToStart(message1, true);
                            mAdapter.notifyDataSetChanged();
                            mAdapter.updateMessage(message1);
                        }
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnBtn();
    }
    private void returnBtn() {
        //重置会话的未读数—服务端
        mConv.resetUnreadCount();
        JMessageClient.exitConversation();
        finish();
    }
}