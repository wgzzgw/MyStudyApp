package com.example.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.example.message.DefaultUser;
import com.example.message.MyMessage;
import com.example.util.SharePreferenceManager;
import com.example.util.StringUtils;
import com.example.util.TimeUtils;
import com.example.view.ChatView;

import java.io.File;
import java.util.List;

import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.OnCameraCallbackListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.model.FileItem;
import cn.jiguang.imui.commons.BitmapLoader;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_CUSTOM;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_FILE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_IMAGE;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_LOCATION;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_VIDEO;
import static cn.jpush.im.android.JMessage.mContext;


public class ChatActivity extends AppCompatActivity  implements ChatView.OnSizeChangedListener
,ChatView.OnKeyboardChangedListener, View.OnTouchListener{
    private InputMethodManager mImm;
    private Window mWindow;
    private ChatView mChatView;
    int heightDifference = 0;
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
    //初始化adapter
    private void initMsgAdapter() {
        imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                Glide.with(getApplicationContext())
                        .load(string)
                        .placeholder(R.mipmap.icon_user)
                        .into(avatarImageView);
            }

            @Override
            public void loadImage(ImageView imageView, String string) {
                Glide.with(getApplicationContext())
                        .load(string)
                        .placeholder(R.mipmap.icon_user)
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
                if (message.getType() == SEND_TEXT
                        || message.getType() == SEND_CUSTOM
                        || message.getType() == SEND_FILE
                        || message.getType() == SEND_IMAGE
                        || message.getType() == SEND_LOCATION
                        || message.getType() == SEND_VIDEO) {
                    strings = new String[]{"复制", "撤回", "转发", "删除"};
                } else {
                    strings = new String[]{"复制", "转发", "删除"};
                }
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
                                //转发
                                break;
                            default:
                                //移除视图
                                mAdapter.deleteById(msgID);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(ChatActivity.this,"删除消息成功",Toast.LENGTH_SHORT);
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
                if (message.getType() == SEND_TEXT) {
                    intent = new Intent(mContext, UserActivity.class);
                }else if(message.getType() ==SEND_IMAGE){
                    intent = new Intent(mContext, UserActivity.class);
                }  else{
                    intent = new Intent(mContext, FriendInfoActivity.class);
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
        mChatView.setAdapter(mAdapter);
        mAdapter.getLayoutManager().scrollToPosition(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindow = getWindow();
        name = getIntent().getStringExtra("userid");
        findAllViews();
        //设置软键盘的高度
        init();
        initMsgAdapter();
        mConv = JMessageClient.getSingleConversation(name);
        if (mConv == null) {
            mConv = Conversation.createSingleConversation(name);
        }
        try {
            imgSend = userInfo.getAvatarFile().toURI().toString();
            imgRecrive = StringUtils.isNull(mConv.getAvatarFile().toURI().toString()) ? "R.mipmap.icon_user" : mConv.getAvatarFile().toURI().toString();

        } catch (Exception e) {
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
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
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
                return true;
            }
        });
        mChatView.setOnCameraCallbackListener(new OnCameraCallbackListener() {
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
        });
        mChatView.setKeyboardChangedListener(this);
        mChatView.setOnSizeChangedListener(this);
        mChatView.setOnTouchListener(this);
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
        super.onResume();
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
        myMessage.setUserInfo(new DefaultUser(JMessageClient.getMyInfo().getUserName(), "DeadPool", imgSend));

        message1.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    mAdapter.addToStart(myMessage, true);
                    mChatView.getChatInputView().getInputView().setText("");
                } else {
                    Toast.makeText(ChatActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        JMessageClient.sendMessage(message1);
    }
}