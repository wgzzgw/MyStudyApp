package com.example.util;


import android.util.Log;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.UrlSafeBase64;
import org.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
/*
* 上传图片到七牛云工具类
* */
public class QnUploadHelper {
    private static final String TAG = QnUploadHelper.class.getSimpleName();
    //七牛云后台的key
    private static String AccessKey;
    //七牛云后台的secret
    private static String SecretKey;
    //七牛云后台的存储区域
    private static String Domain;
    //七牛云后台的存储空间名称
    private static String BucketName;
    //七牛云的配置com.qiniu.android.storage.Configuration
    private static Configuration configuration;
    //有效时间
    private static long delayTimes = 3029414400l;
    /*
    * 编码格式UTF-8
    * */
    private static final String ENCODING = "UTF-8";
    /*
    * 密钥算法的名称
    * */
    private static final String MAC_NAME = "HmacSHA1";
    public static void init(String accessKey, String secretKey,String domain, String bucketName) {
        AccessKey = accessKey;
        SecretKey = secretKey;
        Domain = domain;
        BucketName = bucketName;
        configuration = new Configuration.Builder().build();
    }
    /**
     * 上传
     *
     //     * @param bucketName bucketName的名字
     * @param path   上传文件的路径地址
     */
    public static void uploadPic(final String path, final String names, final UploadCallBack callBack) {
        try {
            // 构造上传策略，将上传策略序列化成为JSON
            JSONObject _json = new JSONObject();
            /*
            * 上传凭证有效截止时间。Unix时间戳，单位为秒。该截止时间为上传完成后，
            * 在七牛空间生成文件的校验时间，而非上传的开始时间，一般建议设置为上传
            * 开始时间 + 3600s，用户可根据具体的业务场景对凭证截止时间进行调整。
            * */
            _json.put("deadline", delayTimes);
           /* 指定上传的目标资源空间 Bucket 和资源键 Key（最大为 750 字节）
            <bucket>，表示允许用户上传文件到指定的 bucket。在这种格式下文件只能新增，
            若已存在同名资源（且文件内容/etag不一致），上传会失败；若已存在资源的内容/etag一致，
            则上传会返回成功。*/
            _json.put("scope", BucketName);
            /*
            * 对 JSON 编码的上传策略进行URL 安全的 Base64 编码，得到待签名字符串
            * */
            String _encodedPutPolicy = UrlSafeBase64.encodeToString(_json
                    .toString().getBytes());
            /*
            * 使用访问密钥（AK/SK）对上一步生成的待签名字符串计算HMAC-SHA1签名
            * */
            byte[] _sign = HmacSHA1Encrypt(_encodedPutPolicy, SecretKey);
            /*对签名进行URL安全的Base64编码*/
            String _encodedSign = UrlSafeBase64.encodeToString(_sign);
            /*将访问密钥（AK/SK）、encodedSign 和 encodedPutPolicy 用英文符号 : 连接起来*/
            final String _uploadToken = AccessKey + ':' + _encodedSign + ':'
                    + _encodedPutPolicy;
            /*上传图片*/
            UploadManager uploadManager = new UploadManager(configuration);
            /*
            *data = <File对象、或 文件路径、或 字节数组>
            *String name = <指定七牛服务上的文件名，或 null>;
            *String token = <从服务端SDK获取>;
            * */
            uploadManager.put(path, names, _uploadToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info,
                                             JSONObject response) {
                            /* //res包含hash、key等信息，具体字段取决于上传策略的设置*/
                            Log.d(TAG,"response = " + response);
                            if (info.isOK()) {
                                String picUrl = Domain + names;
                                callBack.success(picUrl);
                            } else
                                callBack.fail(key, info);
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }}
        /**
         * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
         *
         * @param encryptText 被签名的字符串
         * @param encryptKey  密钥
         * @return
         * @throws Exception
         */
        public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey)
        throws Exception {
            byte[] data = encryptKey.getBytes(ENCODING);
            // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey  secretKey = new SecretKeySpec(data, MAC_NAME);
            // 生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(MAC_NAME);
            // 用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(ENCODING);
            // 完成 Mac 操作
            return mac.doFinal(text);
    }
    /*
    * 上传事件回调接口
    * */
    public interface UploadCallBack {
        void success(String url);
        void fail(String key, ResponseInfo info);
    }
}
