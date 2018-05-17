package com.example.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.R;
import com.example.util.ImgUtils;

public class BrowserViewPagerActivity extends BaseActivity {
    private ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_view_pager);
        initTitle(true,true,"查看大图","",false,"");
        img=(ImageView)findViewById(R.id.img);
        ImgUtils.loadbit(BitmapFactory.decodeFile(getIntent().getStringExtra("avatarPath")),img);
    }
}
