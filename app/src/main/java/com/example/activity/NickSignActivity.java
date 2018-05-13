package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.R;

/**
 * Created by yy on 2018/5/13.
 */

public class NickSignActivity  extends BaseActivity{
    private EditText mEd_sign;//签名编辑区
    private LinearLayout mLl_nickSign;//签名布局
    private TextView mTv_count;//最大字数
    private Button mJmui_commit_btn;//布局bar 右边按钮
    private static final int SIGN_COUNT = 250;
    private static final int NICK_COUNT = 64;
    private int input;//输入长度
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_sign);

        initView();
        Intent intent = getIntent();
        if (intent.getFlags() == PersonalActivity.FLAGS_SIGN) {
            initViewSign("个性签名", SIGN_COUNT);
            initData(SIGN_COUNT);
        } else if (intent.getFlags() == PersonalActivity.FLAGS_NICK) {
            initViewNick("修改昵称", NICK_COUNT);
            initData(NICK_COUNT);
        }
        initListener(intent.getFlags());
    }
    private void initView() {
        mEd_sign = (EditText) findViewById(R.id.ed_sign);
        mLl_nickSign = (LinearLayout) findViewById(R.id.ll_nickSign);
        mTv_count = (TextView) findViewById(R.id.tv_count);
        mJmui_commit_btn = (Button) findViewById(R.id.jmui_commit_btn);
        if (getIntent().getStringExtra("old_nick") != null) {
            mEd_sign.setText(getIntent().getStringExtra("old_nick"));
        }
        if (getIntent().getStringExtra("old_sign") != null) {
            mEd_sign.setText(getIntent().getStringExtra("old_sign"));
        }
        //设置光标所在处
        mEd_sign.setSelection(mEd_sign.getText().length());
    }
    /*
    * 参数一设置左边bar文字
    * */
    private void initViewSign(String str, int flag) {
        initTitle(true, true, str, "", true, "完成");
        //限制输入的最大长度
        mEd_sign.setFilters(new InputFilter[] {new MyLengthFilter(flag)});
        //如果初始有昵称/签名,控制右下字符数
        int length = mEd_sign.getText().toString().getBytes().length;
        mTv_count.setText(flag - length + "");
    }
    private void initViewNick(String str, int flag) {
        initTitle(true, true, str, "", true, "完成");
        mEd_sign.setFilters(new InputFilter[] {new MyLengthFilter(flag)});
        int length = mEd_sign.getText().toString().getBytes().length;
        mTv_count.setText(flag - length + "");
        //编辑区布局变窄
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mLl_nickSign.setLayoutParams(params);
    }
    /*
    * 内部字符过滤类
    * */
    public static class MyLengthFilter implements InputFilter {
        private final int mMax;//最大字数
        public MyLengthFilter(int max) {
            mMax = max;
        }
        /*
        * 参数
        * source    新输入的字符串
          start    新输入的字符串起始下标，一般为0
          end    新输入的字符串终点下标，一般为source长度-1
          dest    输入之前文本框内容
          dstart    原内容起始坐标，一般为0
          dend    原内容终点坐标，一般为dest长度-1
        * */
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                   int dstart, int dend) {
            int keep = mMax - (dest.toString().getBytes().length - (dend - dstart));
            if (keep <= 0) {
                return "";
            } else if (keep >= source.toString().getBytes().length) {
                return null; // keep original
            } else {
                return "";
            }
        }
        /**
         * @return the maximum length enforced by this input filter
         */
        public int getMax() {
            return mMax;
        }
    }
    private void initData(final int countNum) {
        //监听EditText编辑状态的变化
        mEd_sign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = s.toString().substring(start).getBytes().length;
            }
            @Override
            public void afterTextChanged(Editable s) {
                int num = countNum - s.toString().getBytes().length;
                mTv_count.setText(num + "");
            }
        });
    }
    private void initListener(final int flags) {
        mJmui_commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign = mEd_sign.getText().toString();
                Intent intent = new Intent();
                if (flags == PersonalActivity.FLAGS_NICK) {
                    //将输入内容返回上一界面处理，并加上结果码
                    intent.putExtra(PersonalActivity.NICK_NAME_KEY, sign);
                    setResult(PersonalActivity.NICK_NAME, intent);
                } else if (flags == PersonalActivity.FLAGS_SIGN) {
                    //将输入内容返回上一界面处理
                    intent.putExtra(PersonalActivity.SIGN_KEY, sign);
                    setResult(PersonalActivity.SIGN, intent);//1
                }
                //做更新动作
                finish();
            }
        });
    }
}
