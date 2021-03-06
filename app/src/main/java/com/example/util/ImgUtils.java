package com.example.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.MyApplication;

import java.io.ByteArrayOutputStream;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

//图片处理工具
public class ImgUtils {
    public static void loadRound(String url, ImageView targetView) {
        //将对应url图片设置到targetView
        Glide.with(MyApplication.getContext())
                .load(url)
                .into(targetView);
    }
    //将默认图片设置到targetView
    public static void loadRound(int resId, ImageView targetView) {
        Glide.with(MyApplication.getContext())
                .load(resId)
                .into(targetView);
    }
    public static void load(String url, ImageView targetView) {
        //将对应url图片进行图片裁剪后，设置到targetView
        Glide.with(MyApplication.getContext())
                .load(url)
                .bitmapTransform(new CropCircleTransformation(MyApplication.getContext()))//圆形裁剪
                .into(targetView);
    }
    //将默认图片进行图片裁剪后设置到targetView
    public static void load(int resId, ImageView targetView) {
        Glide.with(MyApplication.getContext())
                .load(resId)
                .bitmapTransform(new CropCircleTransformation(MyApplication.getContext()))
                .into(targetView);
    }
    //将对应bitmap设置到目标targetView
    public static void loadbit(Bitmap bitmap,ImageView targetView){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes=baos.toByteArray();
        Glide.with(MyApplication.getContext())
                .load(bytes)
                .into(targetView);
    }
}
