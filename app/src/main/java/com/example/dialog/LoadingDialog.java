package com.example.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.R;

/**
 * Created by yy on 2018/5/12.
 */
/*
* 加载对话框
* */
public class LoadingDialog extends BaseDialog {
    private RelativeLayout layout;
    private ImageView mLoadImg;
    private TextView mLoadText;
    public LoadingDialog(Activity activity){
        super(activity);
        dialog=new Dialog(activity,R.style.loading_dialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view =inflater.inflate(R.layout.jmui_loading_view, null);
        setContentView(view);
        findAllViews(view);
        AnimationDrawable mDrawable = (AnimationDrawable) mLoadImg.getDrawable();
        mDrawable.start();
        dialog.setCancelable(false);
        //调用父类方法设置dialog宽为屏幕的80%，高为WRAP_CONTENT
        setWidthAndHeight(activity.getWindow().getDecorView().getWidth() * 90 / 100,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void findAllViews(View view) {
         layout = (RelativeLayout) view.findViewById(R.id.jmui_dialog_view);
         mLoadImg = (ImageView) view.findViewById(R.id.jmui_loading_img);
         mLoadText = (TextView) view.findViewById(R.id.jmui_loading_txt);
    }

}
