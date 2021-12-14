package com.tojoy.musicplayer.utils;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tojoy.musicplayer.model.AudioInfo;

import java.util.ArrayList;
import java.util.List;


public class MediaUtils {
    private static final String TAG = "MediaUtils";
    private static volatile MediaUtils mInstance;
    private List<AudioInfo> mLocationMusic=null;
    private static boolean mLocalImageEnable;//本地音乐图片获取开关,默认关闭

    public static MediaUtils getInstance() {
        if(null==mInstance){
            synchronized (MediaUtils.class) {
                if (null == mInstance) {
                    mInstance = new MediaUtils();
                }
            }
        }
        return mInstance;
    }

    private MediaUtils(){}

    public int setDialogWidth(Dialog context) {
        Window window = context.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        WindowManager systemService = (WindowManager) context.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(displayMetrics);
        int hight = LinearLayout.LayoutParams.WRAP_CONTENT;
        attributes.height = hight;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int screenWidth = systemService.getDefaultDisplay().getWidth();
        if (screenWidth <= 720) {
            attributes.width = screenWidth - 100;
        } else if (screenWidth > 720 && screenWidth < 1100) {
            attributes.width = screenWidth - 200;
        } else if (screenWidth > 1100 && screenWidth < 1500) {
            attributes.width = screenWidth - 280;
        } else {
            attributes.width = screenWidth - 200;
        }
        attributes.gravity = Gravity.CENTER;
        return attributes.width;
    }

    /**
     * 获取SD卡所有音频文件
     * @return
     */
    public ArrayList<AudioInfo> queryLocationMusics(Context context) {
        ArrayList<AudioInfo> audioInfos=new ArrayList<>();
        if(null!=context.getContentResolver()){
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.YEAR,
                            MediaStore.Audio.Media.MIME_TYPE,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.DATA },
                    MediaStore.Audio.Media.MIME_TYPE + "=? or "
                            + MediaStore.Audio.Media.MIME_TYPE + "=?",
                    new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
            if (null!=cursor&&cursor.moveToFirst()) {
                do {
                    if(!TextUtils.isEmpty(cursor.getString(9))){
                        AudioInfo audioInfo = new AudioInfo();
                        if(!TextUtils.isEmpty(cursor.getString(0))){
                            audioInfo.setMediaId(cursor.getString(0));
                        }else{
                            audioInfo.setMediaId(String.valueOf(System.currentTimeMillis()));
                        }
                        // 文件名
                        //audioInfo.setaudioName(cursor.getString(1));
                        // 歌曲名
                        if(!TextUtils.isEmpty(cursor.getString(2))){
                            audioInfo.setTitle(cursor.getString(2));
                        }
//                song.setPinyin(Pinyin.toPinyin(title.charAt(0)).substring(0, 1).toUpperCase());
                        // 时长
                        if(!TextUtils.isEmpty(cursor.getString(3))){
                            audioInfo.setPlaybackDuration(cursor.getInt(3));
                        }
                        // 歌手名
                        if(!TextUtils.isEmpty(cursor.getString(4))){
                            audioInfo.setAuther(cursor.getString(4));
                        }
                        // 专辑名
                        if(!TextUtils.isEmpty(cursor.getString(5))){
                            audioInfo.setAudioAlbumName(cursor.getString(5));
                        }
                        // 年代 cursor.getString(6)
                        if(!TextUtils.isEmpty(cursor.getString(7))){
                            // 歌曲格式
                            if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                                audioInfo.setAudioType("mp3");
                            } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                                audioInfo.setAudioType("wma");
                            }
                        }
                        //文件大小 cursor.getString(8)
                        // 文件路径
                        //  /storage/emulated/0/Music/齐晨-咱们结婚吧.mp3
                        audioInfo.setMediaUrl(cursor.getString(9));
                        audioInfos.add(audioInfo);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            setLocationMusic(audioInfos);
            return audioInfos;
        }
        setLocationMusic(audioInfos);
        return audioInfos;
    }



    public void setLocationMusic(List<AudioInfo> locationMusic) {
        mLocationMusic = locationMusic;
    }

    public List<AudioInfo> getLocationMusic() {
        return mLocationMusic;
    }

    /**
     * 改变本地图片加载开关状态
     * @return
     */
    public boolean changeLocalImageEnable() {
        this.mLocalImageEnable=!mLocalImageEnable;
        return mLocalImageEnable;
    }

    public boolean isLocalImageEnable() {
        return mLocalImageEnable;
    }

    public void setLocalImageEnable(boolean localImageEnable) {
        mLocalImageEnable = localImageEnable;
    }


    public void onDestroy() {
        if(null!=mLocationMusic){
            mLocationMusic.clear();
            mLocationMusic=null;
        }
        mInstance=null;
    }
}
