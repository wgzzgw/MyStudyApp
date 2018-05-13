package com.example.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.R;

/**
 * Created by yy on 2018/5/13.
 */
/*
* 头像图片选择底部对话框
* */
public class PicChooseDialog extends  BaseDialog{
    /*
        * 事件回调接口
        * */
    public interface OnDialogClickListener {
        void onCamera();//拍照逻辑
        void onAlbum();//相册逻辑
    }
    private OnDialogClickListener onDialogClickListener;
    public void setOnDialogClickListener(OnDialogClickListener l) {
        onDialogClickListener = l;
    }
    public PicChooseDialog(Activity activity) {
        super(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_bottom, null, false);
        setContentView(view);
        //设置dialog宽为MATCH_PARENT，高为WRAP_CONTENT
        setWidthAndHeight(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        View camera = view.findViewById(R.id.photographBtn);
        View picLib = view.findViewById(R.id.localPhotosBtn);
        View cancel = view.findViewById(R.id.cancelBtn);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                if (onDialogClickListener != null) {
                    onDialogClickListener.onCamera();//回调接口方法，具体实现写在调用方
                }
            }
        });
        picLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                if (onDialogClickListener != null) {
                    onDialogClickListener.onAlbum();//回调接口方法，具体实现写在调用方
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }
    @Override
    public void show() {
        /*
        * 底部弹出
        * */
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(lp);
        super.show();
    }
}
