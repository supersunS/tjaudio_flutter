package com.tojoy.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import androidx.collection.LruCache;

public class MusicImageCache {
    private static final String TAG = "MusicImageCache";

    private static MusicImageCache imageCache = null;
    private LruCache<String, Bitmap> cache = null;

    private MusicImageCache() {
        //最大使用容量为堆内存的1/8
        cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public static synchronized MusicImageCache getInstance() {
        if (imageCache == null) {
            imageCache = new MusicImageCache();
        }
        return imageCache;

    }


    /**
     * 讲Bitmap缓存起来
     * @param key
     * @param value
     * @return BITMAP
     */
    public Bitmap put(String key, Bitmap value){
        if(null==cache){
            cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }
        return cache.put(key, value);
    }

    /**
     * 根据路径获取Bitmap
     * @param key
     * @return bitmap
     */
    public Bitmap getBitmap(String key){
        if(TextUtils.isEmpty(key)) return null;
        if(null!=cache&&cache.size()>0){
            return cache.get(key);
        }
        return null;
    }

    /**
     * 清空缓存
     */
    public void onDestroy() {
        if(null!=cache){
            cache.evictAll();//清除缓存
            cache=null;
        }
        imageCache=null;
    }

    /**
     * 根据文件路径获取Bitmap
     * @param filePath
     * @return bitmap
     */
    public Bitmap createBitmap(String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            if(null!=embedPic&&embedPic.length>0){
                LogUtil.d(TAG,"createBitmap-->OK");
                bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG,"createBitmap-->e:"+e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
                LogUtil.d(TAG,"createBitmap-->e:"+e2.getMessage());
            }
        }
        if(null!=bitmap){
            MusicImageCache.getInstance().put(filePath,bitmap);
        }
        return bitmap;
    }
}
