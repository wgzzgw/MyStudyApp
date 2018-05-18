package com.example.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.R;
import com.example.activity.FriendInfoActivity;
import com.example.mystudyapp.MainActivity;

/**
 * Created by yy on 2018/5/13.
 */

public class LogoutDialog extends BaseDialog {
    private Button cancel;//取消
    private Button comfirm;//确认
    public LogoutDialog(Activity activity,View.OnClickListener listener) {
        super(activity);
        dialog = new Dialog(activity, R.style.jmui_default_dialog_style);
        View view = LayoutInflater.from(activity).inflate(R.layout.jmui_dialog_base_with_button, null);
        setContentView(view);
        findAllViews(view);
        setListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
    }

    private void setListener(View.OnClickListener listener) {
        cancel.setOnClickListener(listener);
        comfirm.setOnClickListener(listener);
    }
    private void findAllViews(View view){
       TextView title = (TextView) view.findViewById(R.id.jmui_title);
       cancel = (Button) view.findViewById(R.id.jmui_cancel_btn);
       comfirm = (Button) view.findViewById(R.id.jmui_commit_btn);
        if(activity instanceof FriendInfoActivity)
        title.setText("确认删除好友?");
        else if(activity instanceof MainActivity)
            title.setText("确认退出登录?");
   }
}
