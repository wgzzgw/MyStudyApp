package com.example.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.dialog.PicChooseDialog;
import com.qiniu.android.http.ResponseInfo;


import java.io.File;
import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

/*
* 图片选择帮助类
* */
public class PicChooserHelper {
    private Activity mActivity;
    private Uri mCameraFileUri;//相机返回uri
    private static final int FROM_CAMERA = 2;
    private static final int FROM_ALBUM = 1;
    private static final int CROP = 0;
    private Uri cropUri = null;//存放裁剪后的uri
    private OnChooseResultListener mOnChooserResultListener;//回调接口给调用者
    // Temp照片路径
    public static String TEMP_IMAGE_PATH;
    public PicChooserHelper(Activity activity) {
        mActivity = activity;
        TEMP_IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/temp.png";//图片输出地址
    }
    public void showPicChooserDialog() {
        if (mActivity != null) {
            PicChooseDialog dialog = new PicChooseDialog(mActivity);
            dialog.setOnDialogClickListener(new PicChooseDialog.OnDialogClickListener() {
                @Override
                public void onCamera() {
                    //拍照
                    PicFromCamera();
                }
                @Override
                public void onAlbum() {
                    //相册
                    PicFromAlbum();
                }
            });
            dialog.show();
        }
    }
    private void PicFromCamera() {
        /*
        * 相册选择进行运行时权限申请
        * */
        if(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},2);
        }else{
            takePicFromCamera();
        }
    }
    private void takePicFromCamera() {
        // 系统相机
        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        //获取设备的系统版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24) {
            //小于7.0的版本
            mCameraFileUri = createLowVersionUri();
        }else{
            //大于7.0的版本
            mCameraFileUri=createContentUri(createLowVersionUri());
        }
        //指定图片输出地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraFileUri);
        mActivity.startActivityForResult(intent, FROM_CAMERA);
    }
    /*
   返回系统版本低于7.0的uri
    */
    private Uri createLowVersionUri() {
        /*
        * 使用应用关联目录可以不进行运行时权限
        * */
        //创建File对象，用于存储拍照后的图片
        File img = new File(TEMP_IMAGE_PATH);
        try {
            if (img.exists()) {
                img.delete();
            }
            img.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(img);
    }
    /*
    返回系统版本高于7.0的uri
     */
    public Uri createContentUri(Uri uri) {
        //方法1.需要配置AndroidManifest.XML
        /*return FileProvider.getUriForFile(MainActivity.this,"com.example.hrdgame.fileprovider",
                new File(TEMP_IMAGE_PATH));*/
        //方法2.转换低版本uri为contenturi,不需要配置AndroidManifest.XML
        String filePath = uri.getPath();//得到图片的uri地址，uri统一资源标识符
        Cursor cursor = mActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            //拼接uri
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            return mActivity.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }
    private void PicFromAlbum() {
        /*
        * 相册选择进行运行时权限申请
        * */
        if(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            takePicFromAlbum();
        }
    }
    private void takePicFromAlbum() {
        Intent picIntent = new Intent("android.intent.action.GET_CONTENT");
        //设置MIME类型
        picIntent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        //打开相册
        if(mActivity!=null) {
            mActivity.startActivityForResult(picIntent, FROM_ALBUM);
        }
    }
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicFromAlbum();
                }else{
                    Toast.makeText(mActivity,"你已拒绝了授权",Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicFromCamera();
                }else{
                    Toast.makeText(mActivity,"你已拒绝了授权",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_CAMERA) {
            //从相机拍摄返回
            if (resultCode == Activity.RESULT_OK) {
                startCrop(mCameraFileUri);//裁剪图片
                uploadTo7Niu(mCameraFileUri.getPath());
            }
        } else if (requestCode == FROM_ALBUM) {
            //从相册选择返回
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                startCrop(uri);//裁剪图片
            }
        } else if (requestCode == CROP) {
            //裁剪结束
            if (resultCode == Activity.RESULT_OK) {
                //上传到服务器保存起来
                //七牛上传
                uploadTo7Niu(cropUri.getPath());
            }
        }
    }

    private Uri createCropUri() {
        String dirPath = Environment.getExternalStorageDirectory() + "/" + mActivity.getApplication().getApplicationInfo().packageName;
        File dir = new File(dirPath);
        if (!dir.exists()||dir.isFile()) {
            dir.mkdirs();
        }
        File picFile = new File(dirPath, "crop");
        if (picFile.exists()) {
            picFile.delete();
        }
        try {
            picFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(picFile);
    }
    private void startCrop(Uri uri) {
        cropUri = createCropUri();//返回crop uri
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        //设置输入大小与输出大小
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", false);
        //设置图片输出类型
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24) {
            //小于7.0的版本
            //设置裁剪数据和数据类型
            intent.setDataAndType(uri, "image/*");
            //设置图片输出地址
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            if(mActivity!=null){
                mActivity.startActivityForResult(intent,CROP);
            }
        } else {
            //大于7.0的版本
            {
                String scheme = uri.getScheme();//获取uri协议
                if (scheme.equals("content")) {
                    //设置裁剪数据和数据类型
                    intent.setDataAndType(uri, "image/*");
                } else {
                    Uri contentUri = createContentUri(uri);
                    //设置裁剪数据和数据类型
                    intent.setDataAndType(contentUri, "image/*");
                }
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            if (mActivity != null) {
                mActivity.startActivityForResult(intent, CROP);
            }
        }
    }
    private void uploadTo7Niu(String path) {
        String name="System.currentTimeMillis()_crop";
        /*
        * path:图片路径，name:七牛云Bucket，回调接口
        * */
        QnUploadHelper.uploadPic(path, name, new QnUploadHelper.UploadCallBack() {
            @Override
            public void success(String url) {
                //上传成功
                if (mOnChooserResultListener != null) {
                    mOnChooserResultListener.onSuccess(url);
                }
                JMessageClient.updateUserAvatar(new File(cropUri.getPath()), new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                           Toast.makeText(mActivity,"更新成功",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mActivity,"更新失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void fail(String key, ResponseInfo info) {
                //上传失败！
                if (mOnChooserResultListener != null) {
                    mOnChooserResultListener.onFail(info.error);
                }
            }
        });
    }
/*
* 上传图片最终回调接口
* */
    public interface OnChooseResultListener {
        void onSuccess(String url);
        void onFail(String msg);
    }
    public void setOnChooseResultListener(OnChooseResultListener l) {
        mOnChooserResultListener = l;
    }
}
