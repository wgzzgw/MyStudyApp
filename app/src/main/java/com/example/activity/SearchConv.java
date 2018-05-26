package com.example.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.R;
import com.example.adapter.ConSearchAdapter;
import com.example.adapter.ConversationListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

public class SearchConv extends AppCompatActivity  implements View.OnClickListener,AdapterView.OnItemClickListener {
    private EditText searchconv;
    private ImageView clear;
    private Button search;
    private List<Conversation> mDatas;
    private ListView mListView;
    private  List<Conversation> result;
    private ConSearchAdapter c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_conv);

        findAllViews();
        init();
        result=new ArrayList<Conversation>();
    }

    private void init() {
        searchconv.addTextChangedListener(new TextChange());
        clear.setOnClickListener(this);
        search.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_clear:
                searchconv.setText("");
                break;
            case R.id.btn_search:
                hintKbTwo();
                String message=searchconv.getText().toString();
                mDatas = JMessageClient.getConversationList();
                result.clear();
                for(int i=0;i<mDatas.size();i++){
                    /*Pattern pattern = Pattern.compile(message);
                    Matcher matcher = pattern.matcher(((UserInfo)(mDatas.get(i).getTargetInfo())).getDisplayName());
                    if(matcher.matches()){
                        Log.d("p", "onClick: "+(mDatas.get(i)==null));
                        Log.d("p", "onClick: "+(result==null));
                        result.add(mDatas.get(i));
                    }*/
                    if(Pattern.matches(".*"+message+".*",((UserInfo)(mDatas.get(i).getTargetInfo())).getDisplayName())){
                        Log.d("p", "onClick: "+(mDatas.get(i)==null));
                        Log.d("p", "onClick: "+(result==null));
                        result.add(mDatas.get(i));
                    }
                }
                c=new ConSearchAdapter(this,result);
                mListView.setAdapter(c);
                c.notifyDataSetChanged();
                break;
        }
    }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //点击会话条目
            Intent intent = new Intent();
            if (position >=0) {
                //拿到点击的会话
                Conversation conv = result.get(position);
                Log.d("c", "onItemClick: "+ ((UserInfo) conv.getTargetInfo()).getUserName());
                intent.putExtra("userid", ((UserInfo) conv.getTargetInfo()).getDisplayName());
                intent.putExtra("username", ((UserInfo) conv.getTargetInfo()).getUserName());
                intent.setClass(SearchConv.this, ChatActivity.class);
                startActivity(intent);
            }
    }

    /*
        * 内部类监听edittext的输入
        * */
    private class TextChange implements TextWatcher {
        @Override
        public void afterTextChanged(Editable arg0) {
        }
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }
        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            boolean feedback = searchconv.getText().length() > 0;
            if (feedback) {
                clear.setVisibility(View.VISIBLE);
                search.setEnabled(true);
            } else {
                clear.setVisibility(View.GONE);
                search.setEnabled(false);
            }
        }
    }
    private void findAllViews() {
        searchconv=(EditText)findViewById(R.id.et_searchConv);
        clear=(ImageView)findViewById(R.id.iv_clear);
        search=(Button)findViewById(R.id.btn_search);
        mListView=(ListView)findViewById(R.id.conv_search_list_view);
        //无内容时不可点击
        search.setEnabled(false);
    }
    /*
    隐藏软键盘
     */
    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            //getWindowToken()获取调用的view依附在哪个window的令牌
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
