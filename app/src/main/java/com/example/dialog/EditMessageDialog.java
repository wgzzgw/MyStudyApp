package com.example.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.R;

/**
 * Created by yy on 2018/5/18.
 */

public class EditMessageDialog extends BaseDialog {
    private EditText contentView;
    private OnOKListener onOKListener;
    /*
   dialog按钮回调接口
    */
    public interface OnOKListener {
        void onOk(String content);
    }
    public void setOnOKListener(OnOKListener l) {
        onOKListener = l;
    }
    public EditMessageDialog(Activity activity) {
        super(activity);
        dialog=new Dialog(activity,R.style.dialog_nodim);
        //加载dialog布局
        View mainView = LayoutInflater.from(activity).inflate(R.layout.edit_message, null, false);
        contentView = (EditText) mainView.findViewById(R.id.content);
        //调用父类方法设置布局
        setContentView(mainView);
        //调用父类方法设置dialog宽为屏幕的70%，高为WRAP_CONTENT
        setWidthAndHeight(activity.getWindow().getDecorView().getWidth() * 70 / 100,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mainView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取修改内容
                String content = contentView.getText().toString();
                Log.d("c", "onClick: "+content);
                if (onOKListener != null) {
                    onOKListener.onOk(content);
                }
                destroy();
            }
        });
    }
    /*
   重写父类show方法，外部调用以修改dialog适应场景
    */
    public void show(String defaultContent) {
        contentView.setText(defaultContent);
        show();
    }
}
