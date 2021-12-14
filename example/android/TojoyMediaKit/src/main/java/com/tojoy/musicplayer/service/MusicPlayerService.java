package com.tojoy.musicplayer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.iinterface.MusicPlayerPresenter;
import com.tojoy.musicplayer.listener.MusicPlayerEventListener;
import com.tojoy.musicplayer.listener.MusicPlayerInfoListener;
import com.tojoy.musicplayer.listener.VedioPlayerEventListener;
import com.tojoy.musicplayer.manager.MusicAudioFocusManager;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.manager.MusicWindowManager;
import com.tojoy.musicplayer.model.AudioInfo;
import com.tojoy.musicplayer.model.MusicStatus;
import com.tojoy.musicplayer.utils.ButtonUtils;
import com.tojoy.musicplayer.utils.LogUtil;
import com.tojoy.musicplayer.utils.MusicNotificationUtils;
import com.tojoy.musicplayer.utils.MusicUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.R;

/**
 * 音频播放相关服务，通过该服务管理音频资源通知栏显示，音频播放，暂停，上一首，下一首等功能
 * 通过MusicPlayerBinder驱动对象传递事件信息
 *
 * @author houxianjun
 * @Date 2021/4/10
 */
public class MusicPlayerService extends Service implements MusicPlayerPresenter, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    private static final String TAG = "MusicPlayerService";
    //当前正在工作的播放器对象
    private static MediaPlayer mMediaPlayer;
    //Service委托代理人
    private static MusicPlayerBinder mPlayerBinder;
    //音频组件回调注册池子
    private static List<MusicPlayerEventListener> mOnPlayerEventListeners = new ArrayList<>();

    //微视组件回调注册池子
    private static List<VedioPlayerEventListener> mOnVedioPlayerEventListeners = new ArrayList<>();

    //播放对象监听
    private static MusicPlayerInfoListener sMusicPlayerInfoListener;
    //音频焦点Manager
    private static MusicAudioFocusManager mAudioFocusManager;
    //通知栏
    private NotificationManager mNotificationManager;
    //前台进程默认是开启的,通知交互默认是开启的
    private boolean mForegroundEnable = true, mNotificationEnable = true;
    //播放器绝对路径、锁屏绝对路径
    private String mPlayerActivityClass, mLockActivityClass;
    //待播放音频队列池子
    private static List<Object> mAudios = null;
    //缓存播放音频队列池子，如果正在播放音频，又加载数据，则缓存到该列表中
    private static List<Object> cachemAudios = new ArrayList<>();

    //当前播放播放器正在处理的对象位置
    private int mCurrentPlayIndex = 0;
    //循环模式
    private static boolean mLoop;
    //用户设定的内部播放器播放模式，默认MusicPlayModel.MUSIC_MODEL_LOOP
    private int mPlayModel = MusicPlayerManager.getInstance().getDefaultPlayModel();
    //播放器工作状态
    private int mMusicPlayerState = MusicConstants.MUSIC_PLAYER_INIT;
    //息屏下WIFI锁
    //private static WifiManager.WifiLock mWifiLock;
    //监听系统事件的广播
    private static HeadsetBroadcastReceiver mHeadsetBroadcastReceiver;
    //前台进程对象ID
    private int NOTIFICATION_ID = 10099;
    //播放器内部正在处理的对象所属的数据渠道,默认是来自网络
    private int mPlayChannel = MusicConstants.CHANNEL_NET;
    //MediaPlayer的缓冲进度走到100%就不再回调，此变量只纪录当前播放的对象缓冲的进度，方便播放器UI回显时还原缓冲进度
    private int mBufferProgress;
    //是否被动暂停，用来处理音频焦点失去标记
    private boolean mIsPassive;
    //是否是老板微视页面
    private boolean isBossVedioPage = false;
    /*该字段为了分发注册监听回调时，接收到的回调判断是否通知自己去改变状态。
     *用户进入到页面是上传当前的mtype类型
     */
    private int currentPageType;
    private long addTime;

    private MyHandler handler;
    public long getAddTime() {
        return addTime;
    }

    public int getCurrentPageType() {
        return currentPageType;
    }

    @Override
    public void setCurrentPageType(int mcurrentPageType, long tmpAddTime) {

        this.currentPageType = mcurrentPageType;
        this.addTime = tmpAddTime;
        LogUtil.d(MusicPlayerManager.TAG, MusicPlayerManager.getInstance().getMyTypeName(currentPageType) + "更新了当前页面的类型" + MusicPlayerManager.getInstance().getMyTypeName(mcurrentPageType));
    }

    public boolean isVedioPlayPage() {
        return isBossVedioPage;
    }

    public void setVedioPlayPage(boolean vedioPlayPage) {
        isBossVedioPage = vedioPlayPage;
    }

    @Override
    public void updateCurrentIndex(int index) {
        mCurrentPlayIndex = index;
    }

    ;
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            stopSelf(msg.what);
        }
    }

    private MusicNotificationUtils notificationUtils = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (null == mPlayerBinder) {
            mPlayerBinder = new MusicPlayerBinder(MusicPlayerService.this);
        }
        return mPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioFocusManager = new MusicAudioFocusManager(MusicPlayerService.this.getApplicationContext());
        MusicUtils.getInstance().initSharedPreferencesConfig(this.getApplication());
        initPlayerConfig();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_COLLECT);
        mHeadsetBroadcastReceiver = new HeadsetBroadcastReceiver();
        registerReceiver(mHeadsetBroadcastReceiver, intentFilter);
        handler = new MyHandler();
        notificationUtils = MusicNotificationUtils.getInstance(this, mNotificationEnable);
        notificationUtils.startForegroundService(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        notificationUtils.startForegroundService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int type = intent.getIntExtra("type",1);
        LogUtil.d(TAG, "the create notification type is " + type + "----" + (type == 1 ? "true" : "false"));
        if(type == 1){
            notificationUtils.startForegroundService(this);
        }else{
           // createErrorNotification();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopForeground(true);
            }
        }, 1000);

        return Service.START_NOT_STICKY;
    }
    private void createErrorNotification() {
        Notification notification = new Notification.Builder(this).build();
        startForeground(10099, notification);
    }
    /**
     * 初始化播放器配置
     */
    private void initPlayerConfig() {
        String value = MusicUtils.getInstance().getString(MusicConstants.SP_KEY_PLAYER_MODEL,
                MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP);
        int model = MusicPlayerManager.getInstance().getDefaultPlayModel();
        if (value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_SINGLE)) {
            mLoop = true;
            model = MusicConstants.MUSIC_MODEL_SINGLE;
        } else if (value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP)) {
            model = MusicConstants.MUSIC_MODEL_LOOP;
            mLoop = false;
        } else if (value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_ORDER)) {
            model = MusicConstants.MUSIC_MODEL_ORDER;
            mLoop = false;
        } else if (value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_RANDOM)) {
            model = MusicConstants.MUSIC_MODEL_RANDOM;
            mLoop = false;
        }
        this.mPlayModel = model;
    }


    /**
     * 播放全新的音频列表任务
     *
     * @param musicList 待播放的数据集，对象需要继承BaseaudioInfo
     * @param index     指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> musicList, int index) {
        if (null != musicList) {
            mAudios.clear();
            mAudios.addAll(musicList);
            startPlayMusic(index);
        }
    }

    /**
     * 播放指定位置的音乐
     *
     * @param index 指定的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(int index) {
        if (index < 0) {
            // throw new IndexOutOfBoundsException("start play index must > 0");
            LogUtil.d(MusicPlayerManager.TAG, "startPlayMusic" + "位置index小于0");
            return;
        }
        if (null != mAudios && mAudios.size() > index) {
            this.mCurrentPlayIndex = index;
            AudioInfo baseMusicInfo = (AudioInfo) mAudios.get(index);
            startPlay(baseMusicInfo);
        }
    }

    /**
     * 管理如果在老板微视页面播放时，只是更新通知栏状态
     *
     * @param index
     */
    @Override
    public void startPlayVedio(int index) {
        if (index < 0) {
            // throw new IndexOutOfBoundsException("start play index must > 0");
            LogUtil.d(MusicPlayerManager.TAG, "startPlayVedio" + "位置index小于0");
            return;
        }
        if (null != mAudios && mAudios.size() > index) {
            this.mCurrentPlayIndex = index;
            AudioInfo musicInfo = (AudioInfo) mAudios.get(index);
            if (null != musicInfo && !TextUtils.isEmpty(musicInfo.getMediaUrl())) {

                MusicPlayerService.this.mIsPassive = false;
                MusicPlayerService.this.mMusicPlayerState = MusicConstants.VEDIO_PLAYER_PLAYING;

            } else {
                MusicPlayerService.this.mMusicPlayerState = MusicConstants.VEDIO_PLAYER_PAUSE;
            }
//            if (null != mOnVedioPlayerEventListeners && mOnVedioPlayerEventListeners.size() > 0) {
//                for (VedioPlayerEventListener onPlayerEventListener : mOnVedioPlayerEventListeners) {
//                    onPlayerEventListener.onVedioPlayerState(mMusicPlayerState, null,getCurrentPageType(),getAddTime());
//                }
//            }
            notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
        }
    }

    /**
     * 开始一个新的播放，将其加入正在播放的队列顶部
     *
     * @param audioInfo 音频对象
     */
    @Override
    public void addPlayMusicToTop(AudioInfo audioInfo) {
        if (null == audioInfo) {
            return;
        }
        if (null == cachemAudios) {
            cachemAudios = new ArrayList<>();
        }
        int pisition = -1;
        if (cachemAudios.size() > 0) {
            for (int i = 0; i < cachemAudios.size(); i++) {
                AudioInfo musicInfo = (AudioInfo) cachemAudios.get(i);
                if (!TextUtils.isEmpty(audioInfo.getMediaId()) && !TextUtils.isEmpty(musicInfo.getMediaId())) {
                    if (audioInfo.getMediaId().equals(musicInfo.getMediaId())) {
                        pisition = i;
                        if (!TextUtils.isEmpty(audioInfo.getMediaUrl()) && !TextUtils.isEmpty(musicInfo.getMediaUrl())) {
                            //如果新增的音频ID相同，链接不同则跟新原位置
                            if (!audioInfo.getMediaUrl().equals(musicInfo.getMediaUrl())) {
                                cachemAudios.remove(pisition);
                                cachemAudios.add(pisition, audioInfo);
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (pisition > -1) {
            LogUtil.d(MusicPlayerManager.TAG, "addPlayMusicToTop方法" + "对象已存在");
            return;
        }

        cachemAudios.add(0, audioInfo);
        LogUtil.d(MusicPlayerManager.TAG, "addPlayMusicToTop方法" + "新资源向头部追加对象");
    }

    /**
     * 开始、暂停
     */
    @Override
    public synchronized void playOrPause() {
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerState()) {
                case MusicConstants.MUSIC_PLAYER_STOP:
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                case MusicConstants.MUSIC_PLAYER_PREPARE:
                    pause();
                    break;
                case MusicConstants.MUSIC_PLAYER_BUFFER:
                    pause();
                    break;
                case MusicConstants.MUSIC_PLAYER_PLAYING:
                    pause();
                    break;
                case MusicConstants.MUSIC_PLAYER_PAUSE:
                    if (null != mAudioFocusManager) {
                        mAudioFocusManager.requestAudioFocus(null);
                    }
                    play();
                    break;
                case MusicConstants.MUSIC_PLAYER_ERROR:
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        try {
            //如果是老板视频页面，不会启动音乐播放器，也不会在暂停
            if (!isVedioPlayPage()) {
                if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                    MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PAUSE;
                    mMediaPlayer.pause();
                }
            }

        } catch (RuntimeException e) {

        } finally {

            if (!isVedioPlayPage()) {
                if (null != mOnPlayerEventListeners) {
                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
                    }
                }
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_PAUSE));
                //最后更新通知栏
                //showNotification();
                notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
            }
        }
    }

    /**
     * 被动暂停播放，仅提供给失去焦点时内部调用
     */
    private void passivePause() {
        try {
            if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                MusicPlayerService.this.mIsPassive = true;
                mMediaPlayer.pause();
            }
        } catch (RuntimeException e) {

        } finally {
            MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PAUSE;
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
                }
            }
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_PAUSE));
            //最后更新通知栏
            //showNotification();
            notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void play() {
        if (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_PLAYING) {
            return;
        }
        try {
            if (null != mMediaPlayer) {
                MusicPlayerService.this.mIsPassive = false;
                //如果是老板卫视页面
                if (!isVedioPlayPage()) {
                    mMediaPlayer.start();
                }
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_START));
            } else {
                startPlayMusic(mCurrentPlayIndex);
            }
        } catch (RuntimeException e) {

        } finally {
            if (null != mMediaPlayer) {
                MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PLAYING;
                if (null != mOnPlayerEventListeners) {
                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
                    }
                }
                //最后更新通知栏
                //showNotification();
                notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
            }
        }
    }

    /**
     * 设置循环模式
     *
     * @param loop 为true循环播放
     */
    @Override
    public void setLoop(boolean loop) {
        this.mLoop = loop;
        try {
            if (null != mMediaPlayer) {
                mMediaPlayer.setLooping(loop);
            }
        } catch (RuntimeException e) {

        }
    }

    /**
     * 特殊场景调用，如播放对象URL为空，再次恢复上一次的播放任务
     *
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     */
    @Override
    public void continuePlay(String sourcePath) {
        if (TextUtils.isEmpty(sourcePath)) {
            return;
        }
        if (null != mAudios && mAudios.size() > mCurrentPlayIndex) {
            ((AudioInfo) mAudios.get(mCurrentPlayIndex)).setMediaUrl(sourcePath);
            startPlayMusic(mCurrentPlayIndex);
        }
    }

    /**
     * 特殊场景调用，如播放对象URL为空，再次恢复上一次的播放任务
     *
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     * @param index      期望重试播放的具体位置
     */
    @Override
    public void continuePlay(String sourcePath, int index) {
        if (TextUtils.isEmpty(sourcePath)) {
            return;
        }
        if (null != mAudios && mAudios.size() > index) {
            ((AudioInfo) mAudios.get(index)).setMediaUrl(sourcePath);
            startPlayMusic(index);
        }
    }

    /**
     * 设置播放器播放模式
     *
     * @param model 播放模式，参考MusicConstants定义
     * @return 成功设置的播放模式
     */
    @Override
    public int setPlayerModel(int model) {
        this.mPlayModel = model;
        mLoop = false;
        if (model == MusicConstants.MUSIC_MODEL_SINGLE) {
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_SINGLE);
            mLoop = true;
        } else if (model == MusicConstants.MUSIC_MODEL_LOOP) {
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP);
        } else if (model == MusicConstants.MUSIC_MODEL_ORDER) {
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_ORDER);
        } else if (model == MusicConstants.MUSIC_MODEL_RANDOM) {
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_RANDOM);
        }
        if (mLoop && null != mMediaPlayer) {
            mMediaPlayer.setLooping(mLoop);
        }
        return mPlayModel;
    }

    /**
     * 返回播放器播放模式
     *
     * @return 播放模式
     */
    @Override
    public int getPlayerModel() {
        return mPlayModel;
    }


    /**
     * 跳转到某个位置播放
     *
     * @param currentTime 时间位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime) {
        try {
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo((int) currentTime);
            }
        } catch (RuntimeException e) {
        }
    }

    /**
     * 播放上一首，内部维持上一首逻辑
     */
    @Override
    public synchronized void playLastMusic() {
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    mCurrentPlayIndex--;
                    if (mCurrentPlayIndex < 0) {
                        mCurrentPlayIndex = mAudios.size() - 1;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
        LogUtil.d(TAG, "playLastMusic--newPlayIndex:" + mCurrentPlayIndex + ",MODE:" + getPlayerModel());
    }

    /**
     * 播放下一首，内部维持下一首逻辑
     */
    @Override
    public synchronized void playNextMusic() {
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    if (mCurrentPlayIndex >= mAudios.size() - 1) {
                        mCurrentPlayIndex = 0;
                    } else {
                        mCurrentPlayIndex++;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
        LogUtil.d(TAG, "playNextMusic--newPlayIndex:" + mCurrentPlayIndex + ",MODE:" + getPlayerModel());
    }

    /**
     * 播放下一个视频
     */
    @Override
    public synchronized void playNextVedio() {
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    if (mCurrentPlayIndex >= mAudios.size() - 1) {
                        mCurrentPlayIndex = 0;
                    } else {
                        mCurrentPlayIndex++;
                    }
                    if(mAudios!=null&&mAudios.size()!=1){
                        //更新视频播放状态与通知栏内容
                        MusicPlayerManager.getInstance().setPlayerState(MusicConstants.VEDIO_PLAYER_PAUSE);
                        MusicPlayerManager.getInstance().startNotification(mCurrentPlayIndex);
                    }
                    //通知UI同步显示指定位置的视频
                    postVedioViewHandlerCurrentPosition(mCurrentPlayIndex);

                    break;
            }
        }
        LogUtil.d(MusicPlayerManager.TAG, "Service" + MusicPlayerManager.getInstance().getMyTypeName(currentPageType) + MusicPlayerManager.getInstance().getMyTypeName(currentPageType) + "通知栏播放下一首-playNextVedio--Index:" + mCurrentPlayIndex);
    }

    /**
     * 播放上一个视频
     */
    @Override
    public synchronized void playLastVedio() {
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    mCurrentPlayIndex--;
                    if (mCurrentPlayIndex < 0) {
                        mCurrentPlayIndex = mAudios.size() - 1;
                    }
                    if(mAudios!=null&&mAudios.size()!=1) {
                        //更新视频播放状态与通知栏内容
                        MusicPlayerManager.getInstance().setPlayerState(MusicConstants.VEDIO_PLAYER_PAUSE);
                        MusicPlayerManager.getInstance().startNotification(mCurrentPlayIndex);
                    }
                    //通知UI同步显示指定位置的视频
                    postVedioViewHandlerCurrentPosition(mCurrentPlayIndex);

                    break;
            }
        }
        LogUtil.d(MusicPlayerManager.TAG, "Service" + MusicPlayerManager.getInstance().getMyTypeName(currentPageType) + "通知栏播放上一首--index:" + mCurrentPlayIndex);
    }
    /**
     * 暂停视频
     */
    @Override
    public synchronized void playPauseVedio() {

        MusicPlayerService.this.mMusicPlayerState = MusicConstants.VEDIO_PLAYER_PAUSE;

        //通知UI处理暂停逻辑
        if (null != mOnVedioPlayerEventListeners && mOnVedioPlayerEventListeners.size() > 0) {
            for (VedioPlayerEventListener onPlayerEventListener : mOnVedioPlayerEventListeners) {
                onPlayerEventListener.onVedioPlayerState(mMusicPlayerState, null, getCurrentPageType(), getAddTime());
            }
        }
        //更新通知栏同步按钮显示暂停图标
        LogUtil.d(MusicPlayerManager.TAG, "myType" + MusicPlayerManager.getInstance().getMyTypeName(currentPageType) + "通知栏暂停播放-playPauseVedio:" + mCurrentPlayIndex);
    }
    /**
     * 开播视频
     */
    @Override
    public synchronized void playStartVedio() {
        MusicPlayerService.this.mMusicPlayerState = MusicConstants.VEDIO_PLAYER_PLAYING;
        //通知UI处理开始播放逻辑
        if (null != mOnVedioPlayerEventListeners && mOnVedioPlayerEventListeners.size() > 0) {
            for (VedioPlayerEventListener onPlayerEventListener : mOnVedioPlayerEventListeners) {
                onPlayerEventListener.onVedioPlayerState(mMusicPlayerState, null, getCurrentPageType(), getAddTime());
            }
        }
    }

    /**
     * 试探上一首的位置，不会启动播放任务,也不会改变正在播放的对象
     *
     * @return 上一首的位置
     */
    @Override
    public int playLastIndex() {
        int tempIndex = mCurrentPlayIndex;
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    tempIndex--;
                    if (tempIndex < 0) {
                        tempIndex = mAudios.size() - 1;
                    }
                    break;
            }
        }
         return tempIndex;
    }

    /**
     * 试探下一首的位置，不会启动播放任务,也不会改变正在播放的对象
     *
     * @return 下一首的位置
     */
    @Override
    public int playNextIndex() {
        int tempIndex = mCurrentPlayIndex;
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {

                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    if (tempIndex >= mAudios.size() - 1) {
                        tempIndex = 0;
                    } else {
                        tempIndex++;
                    }
                    break;

            }
        }
        LogUtil.d(TAG, "playNextIndex--NEWX_INDEX:" + tempIndex + ",MODE:" + getPlayerModel() + ",CURRENT_INDEX:" + mCurrentPlayIndex);
        return tempIndex;
    }

    /**
     * 随机试探下一首歌曲的位置
     *
     * @return 下一首的位置
     */
    @Override
    public int playRandomNextIndex() {
        if (null != mAudios && mAudios.size() > 0) {
            int index = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
            return index;
        }
        return -1;
    }

    /**
     * 播放器内部播放状态
     *
     * @return 为true正在播放
     */
    @Override
    public boolean isPlaying() {
        try {
            return null != mMediaPlayer && (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_PREPARE
                    || mMusicPlayerState == MusicConstants.MUSIC_PLAYER_PLAYING
                    || mMusicPlayerState == MusicConstants.MUSIC_PLAYER_BUFFER
                    || mMusicPlayerState == MusicConstants.VEDIO_PLAYER_PLAYING);
        } catch (RuntimeException e) {

        }
        return false;
    }

    /**
     * 返回播放器处理的对象的总时长
     *
     * @return 单位毫秒
     */
    @Override
    public long getDurtion() {
        try {
            if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                return mMediaPlayer.getDuration();
            }
        } catch (RuntimeException e) {

        }
        return 0;
    }

    /**
     * 返回播放器正在播放的对象，通知播放状态下，当做未开始播放处理
     *
     * @return 音频ID
     */
    @Override
    public String getCurrentPlayerID() {
        if (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_STOP) {
            return "";
        }
        if (null != mAudios && mAudios.size() > mCurrentPlayIndex) {
            return ((AudioInfo) mAudios.get(mCurrentPlayIndex)).getMediaId();
        }
        return "";
    }

    /**
     * 通过articleid 返回所在列表位置
     *
     * @param articleId
     * @return -1 标识没有改歌曲
     */
    public int getIndexById(String articleId) {
        int index = -1;

        if (null != mAudios && mAudios.size() > 0) {

            for (int i = 0; i < mAudios.size(); i++) {
                if (articleId.equals(((AudioInfo) mAudios.get(i)).getMediaId())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    /**
     * onStop
     * 返回正在播放的对象，若播放器停止，为空
     *
     * @return 音频对象
     */
    @Override
    public AudioInfo getCurrentPlayerMusic(int mCurrentPlayIndex) {
        if (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_STOP) {
            return null;
        }
        if (null != mAudios && mAudios.size() > mCurrentPlayIndex) {
            AudioInfo info = (AudioInfo) mAudios.get(mCurrentPlayIndex);
            //   LogUtil.e(MusicPlayerManager.TAG, "====getCurrentPlayerMusic info"+info.getAudioName()+";mCurrentPlayIndex"+mCurrentPlayIndex);

            return (AudioInfo) mAudios.get(mCurrentPlayIndex);
        }
//        if(mAudios!=null){
//           // LogUtil.e(MusicPlayerManager.TAG, "====getCurrentPlayerMusic 没有查找到对应对象 mAudios.size()"+mAudios.size()+";mCurrentPlayIndex="+mCurrentPlayIndex);
//        }
//        else{
//           // LogUtil.e(MusicPlayerManager.TAG, "====getCurrentPlayerMusic mAudios为空");
//        }
        return null;
    }

    /**
     * 返回正在播放的对象，若播放器停止，为空
     *
     * @return 音频对象
     */
    @Override
    public AudioInfo getCurrentPlayerMusic() {
        if (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_STOP) {
            return null;
        }
        if (null != mAudios && mAudios.size() > mCurrentPlayIndex) {
            return (AudioInfo) mAudios.get(mCurrentPlayIndex);
        }
        return null;
    }

    /**
     * 返回正在播放的第三方网络歌曲HASH KEY
     *
     * @return 音频文件HASH KEY
     */
    @Override
    public String getCurrentPlayerHashKey() {
        if (mMusicPlayerState == MusicConstants.MUSIC_PLAYER_STOP) {
            return "";
        }
        if (null != mAudios && mAudios.size() > mCurrentPlayIndex) {
            return ((AudioInfo) mAudios.get(mCurrentPlayIndex)).getAudioHashKey();
        }
        return "";
    }

    /**
     * 获取播放器正在处理的待播放队列
     *
     * @return 播放器内部持有的播放队列
     */
    @Override
    public List<?> getCurrentPlayList() {
        return mAudios;
    }

    @Override
    public List<Object> getCachemAudios() {
        return cachemAudios;
    }

    /**
     * 绑定播放器正在处理的数据渠道
     *
     * @param channel 参考 MusicConstants 定义
     */
    @Override
    public void setPlayingChannel(int channel) {
        mPlayChannel = channel;
    }

    /**
     * 返回放器内部正在处理的播放数据来源CHANNEL
     *
     * @return 数据来源CHANNEL, 详见MusicConstants定义
     */
    @Override
    public int getPlayingChannel() {
        return mPlayChannel;
    }

    /**
     * 返回播放器工作状态
     *
     * @return 播放状态，详见MusicPlayerState定义
     */
    @Override
    public int getPlayerState() {
        return mMusicPlayerState;
    }

    @Override
    public void setPlayerState(int playerState) {
        mMusicPlayerState = playerState;
    }

    /**
     * 检查播放器配置
     */
    @Override
    public void onCheckedPlayerConfig() {

    }

    /**
     * 检查播放器内部正在处理的音频对象
     * 回调：播放器内部播放状态、播放对象、缓冲进度、音频对象总时长、音频对象已播放时长、定时停止播放的剩余时长
     * 用处：回调至关心的UI组件还原播放状态
     */
    @Override
    public void onCheckedCurrentPlayTask() {
        if (null != mMediaPlayer && null != mAudios && mAudios.size() > 0) {
            if (null != mOnPlayerEventListeners) {
                AudioInfo musicInfo = (AudioInfo) mAudios.get(mCurrentPlayIndex);
                try {
                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
                        onPlayerEventListener.onPlayMusiconInfo(musicInfo, mCurrentPlayIndex);
                        if (null != mMediaPlayer || mMusicPlayerState == MusicConstants.MUSIC_PLAYER_PAUSE
                                || isPlaying()) {
                            //+500毫秒是因为1秒一次的播放进度回显，格式化分秒后显示有时候到不了终点时间
                            onPlayerEventListener.onTaskRuntime(mMediaPlayer.getDuration(),
                                    mMediaPlayer.getCurrentPosition() + 500, mBufferProgress);
                        } else {
                            onPlayerEventListener.onTaskRuntime(0, 0, mBufferProgress);
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向监听池中添加一个监听器
     *
     * @param listener 实现监听器的对象
     */
    @Override
    public void addOnPlayerEventListener(MusicPlayerEventListener listener) {
        if (null != mOnPlayerEventListeners) {
            mOnPlayerEventListeners.add(listener);
        }
    }

    /**
     * 从监听池中移除一个监听器
     *
     * @param listener 实现监听器的对象
     */
    @Override
    public void removePlayerListener(MusicPlayerEventListener listener) {
        if (null != mOnPlayerEventListeners) {
            mOnPlayerEventListeners.remove(listener);
        }
    }

    @Override
    public void addOnVedioPlayerEventListener(VedioPlayerEventListener listener) {
        if (null != mOnVedioPlayerEventListeners) {
            mOnVedioPlayerEventListeners.add(listener);
        }
    }

    @Override
    public void removeVedioPlayerListener(VedioPlayerEventListener listener) {
        if (null != mOnVedioPlayerEventListeners) {
            mOnVedioPlayerEventListeners.remove(listener);
        }
    }

    /**
     * 清空监听池
     */
    @Override
    public void removeAllPlayerListener() {
        if (null != mOnPlayerEventListeners) {
            mOnPlayerEventListeners.clear();
        }

        if (null != mOnVedioPlayerEventListeners) {
            mOnVedioPlayerEventListeners.clear();
        }
    }

    /**
     * 监听播放器正在处理的对象
     *
     * @param listener 实现监听器的对象
     */
    @Override
    public MusicPlayerManager setPlayInfoListener(MusicPlayerInfoListener listener) {
        MusicPlayerService.this.sMusicPlayerInfoListener = listener;
        return null;
    }

    /**
     * 移除监听播放对象事件
     */
    @Override
    public void removePlayInfoListener() {
        MusicPlayerService.this.sMusicPlayerInfoListener = null;
    }

    /**
     * 还原MediaPlayer
     */
    @Override
    public void onReset() {
        mBufferProgress = 0;
        MusicPlayerService.this.mIsPassive = false;
        try {
            if (null != mMediaPlayer) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放当前是音乐列表播放时：
     * 点击关闭通知栏或者点击悬浮窗口关闭事件调用该方法
     * 此时清理当前播放列表数据和关闭浮窗和通知栏
     */
    @Override
    public void onStop() {
        mBufferProgress = 0;
        //还原播放渠道
        setPlayingChannel(MusicConstants.CHANNEL_NET);
        seekTo(0);
        cleanNotification();
        if (!isVedioPlayPage()) {

            if (MusicWindowManager.getInstance().isWindowShowing()) {
                MusicWindowManager.getInstance().removeAllWindowView(MusicPlayerService.this.getApplicationContext());

            }
        } else {
            if (null != cachemAudios) {
                cachemAudios.clear();
            }

        }


//        if(null!=mAudios){
//            mAudios.clear();
//        }
        if (null != mAudioFocusManager) {
            mAudioFocusManager.releaseAudioFocus();
        }
        try {

            if (null != mMediaPlayer) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer.reset();
            }
//            if(null!=mWifiLock){
//                mWifiLock.release();
//            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mMediaPlayer = null;
            //通知关闭
            NotifationUpdateState(MusicConstants.MUSIC_PLAYER_STOP);
            //重制状态为初始状态
            this.mMusicPlayerState=MusicConstants.MUSIC_PLAYER_INIT;
        }
    }

    /**
     * 清楚缓存数据
     */
    @Override
    public void cleanMusicData() {
        //缓存清理
        if (null != cachemAudios) {
            cachemAudios.clear();
        }
        //当前播放列表数据清理
        if (null != mAudios) {
            mAudios.clear();
        }
        cleanNotification();
    }

    /**
     * 统一管理通知更新UI或逻辑的状态
     *
     * @param state
     */
    public void NotifationUpdateState(int state) {
        MusicPlayerService.this.mMusicPlayerState = state;
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(state));
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(state, null);
            }
        }
        if (null != mOnVedioPlayerEventListeners) {
            for (VedioPlayerEventListener vedioPlayerEventListener : mOnVedioPlayerEventListeners) {
                vedioPlayerEventListener.onVedioPlayerState(state, null, getCurrentPageType(), getAddTime());
            }
        }
    }

    /**
     * 播放器内部数据刷新
     *
     * @param audios 数据集
     * @param index  位置
     */
    @Override
    public void updateMusicPlayerData(List<?> audios, int index) {
        if (null == mAudios) {
            mAudios = new ArrayList<>();
        }
        if (null != mAudios) {
            mAudios.clear();
            mAudios.addAll(audios);
        }
        mCurrentPlayIndex = index;
    }

    @Override
    public void updateMusicPlayerData(List<?> audios) {

        if (cachemAudios == null) {
            cachemAudios = new ArrayList<>();
        }
        cachemAudios.clear();
        cachemAudios.addAll(audios);

    }

    @Override
    public void copyCacheMusicList() {

        if (cachemAudios != null && cachemAudios.size() > 0) {
            pause();
            onReset();
            if (mAudios == null) {
                mAudios = new ArrayList<>();
            }
            mAudios.clear();
            mAudios.addAll(cachemAudios);
            LogUtil.d(MusicPlayerManager.TAG, "copyCacheMusicList成功" + mAudios.size() + "当前列表");
        } else {
            LogUtil.d(MusicPlayerManager.TAG, "缓存数据为空");
        }
    }

    /**
     * 内部状态销毁
     */
    private void distroy() {
        if (null != mAudioFocusManager) {
            mAudioFocusManager.releaseAudioFocus();
            mAudioFocusManager = null;
        }
        try {
            if (null != mMediaPlayer) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
            }
        } catch (RuntimeException e) {

        } finally {
            try {
//                if(null!=mWifiLock){
//                    mWifiLock.release();
//                }
            } catch (RuntimeException e) {

            } finally {
                mMediaPlayer = null;
                MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_STOP;
            }
        }
    }

    /**
     * 创建一个默认的播放器悬浮窗窗口
     */
    @Override
    public void createMiniJukeboxWindow() {
        MusicWindowManager.getInstance().createMiniJukeBoxToWindown(getApplicationContext());
    }

    /**
     * 添加一个播放器内部默认的前台通知组件
     */
    @Override
    public synchronized void startServiceForeground() {
        notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
    }

    /**
     * 添加一个前台通知组件
     *
     * @param notification
     */
    @Override
    public void startServiceForeground(Notification notification) {
        notificationUtils.showNotification(notification, NOTIFICATION_ID, true, this);
    }

    /**
     * 添加一个前台通知组件
     *
     * @param notification
     * @param notifiid     通知ID
     */
    @Override
    public void startServiceForeground(Notification notification, int notifiid) {
        notificationUtils.showNotification(notification, notifiid, true, this);
    }

    @Override
    public synchronized void stopServiceForeground() {
        cleanNotification(NOTIFICATION_ID);
    }

    @Override
    public void startNotification() {

        //在老板微视或者音频播放中如果是关闭通知栏或者关闭浮窗。则不显示新通知栏
        if (getPlayerState() == MusicConstants.MUSIC_PLAYER_STOP) {
            return;
        }
        notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
    }

    @Override
    public void startNotification(int index) {
        //在老板微视或者音频播放中如果是关闭通知栏或者关闭浮窗。则不显示新通知栏
        if (getPlayerState() == MusicConstants.MUSIC_PLAYER_STOP) {
            return;
        }
        mCurrentPlayIndex = index;
        notificationUtils.showNotification(getCurrentPlayerMusic(index), this);
    }

    @Override
    public void startNotification(Notification notification) {
        notificationUtils.showNotification(notification, NOTIFICATION_ID, true, this);
    }

    @Override
    public void startNotification(Notification notification, int notifiid) {
        notificationUtils.showNotification(notification, notifiid, true, this);
    }

    @Override
    public void updateNotification() {
        //仅当用户在已经至少一次播放音乐后才尝试更新通知栏
        if (null != getCurrentPlayerMusic(mCurrentPlayIndex) && null != mNotificationManager) {
            //showNotification();
            notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void cleanNotification() {
        notificationUtils.cleanNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(NOTIFICATION_ID);
            } else {
                stopForeground(true);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建一个窗口播放器
     */
    @Override
    public void createWindowJukebox() {
//        if(MusicWindowManager.getInstance().checkAlertWindowsPermission(getApplicationContext())){
//            if(!MusicWindowManager.getInstance().isWindowShowing()){
//                BaseAudioInfo audioInfo = getCurrentPlayerMusic();
//                if(null!=audioInfo){
//                    MusicWindowManager.getInstance().createMiniJukeBoxToWindown(getApplicationContext());
//                    MusicStatus musicStatus=new MusicStatus();
//                    musicStatus.setId(audioInfo.getAudioId());
//                    String frontPath=MusicUtils.getInstance().getMusicFrontPath(audioInfo);
//                    musicStatus.setCover(frontPath);
//                    musicStatus.setTitle(audioInfo.getAudioName());
//                    int playerState = getPlayerState();
//                    boolean playing = playerState==MusicConstants.MUSIC_PLAYER_PLAYING
//                            || playerState==MusicConstants.MUSIC_PLAYER_PREPARE
//                            || playerState==MusicConstants.MUSIC_PLAYER_BUFFER;
//                    musicStatus.setPlayerStatus(playing?MusicStatus.PLAYER_STATUS_START:MusicStatus.PLAYER_STATUS_PAUSE);
//                    MusicWindowManager.getInstance().updateWindowStatus(musicStatus);
//                    //此处手动显示一把，避免悬浮窗还未成功创建,将正在播放得音频对象绑定到悬浮窗口
//                    MusicWindowManager.getInstance().onVisible(audioInfo.getAudioId());
//                }
//            }
//        }
    }

    @Override
    public MusicPlayerManager setNotificationEnable(boolean enable) {
        this.mNotificationEnable = enable;
        return null;
    }

    @Override
    public MusicPlayerManager setLockForeground(boolean enable) {
        this.mForegroundEnable = enable;
        return null;
    }

    @Override
    public MusicPlayerManager setPlayerActivityName(String className) {
        this.mPlayerActivityClass = className;
        return null;
    }

    @Override
    public MusicPlayerManager setLockActivityName(String className) {
        this.mLockActivityClass = className;
        return null;
    }


    /**
     * 开始播放媒体文件
     *
     * @param musicInfo
     */
    private synchronized void startPlay(AudioInfo musicInfo) {
        onReset();
        if (null != musicInfo && !TextUtils.isEmpty(musicInfo.getMediaUrl())) {
            if (null == mAudioFocusManager) {
                mAudioFocusManager = new MusicAudioFocusManager(MusicPlayerService.this.getApplicationContext());
            }
            MusicPlayerService.this.mIsPassive = false;
            int requestAudioFocus = mAudioFocusManager.requestAudioFocus(new MusicAudioFocusManager.OnAudioFocusListener() {
                /**
                 * 恢复音频输出焦点，这里恢复播放需要和用户调用恢复播放有区别
                 * 因为当用户主动暂停，获取到音频焦点后不应该恢复播放，而是继续保持暂停状态
                 */
                @Override
                public void onFocusGet() {
                    //如果是被动失去焦点的，则继续播放，否则继续保持暂停状态
                    if (mIsPassive) {
                        play();
                    }
                }

                /**
                 * 失去音频焦点后暂停播放，这里暂停播放需要和用户主动暂停有区别，做一个标记，配合onResume。
                 * 当获取到音频焦点后，根据onResume根据标识状态看是否需要恢复播放
                 */
                @Override
                public void onFocusOut() {
                    passivePause();
                }

                /**
                 * 返回播放器是否正在播放
                 * @return 为true正在播放
                 */
                @Override
                public boolean isPlaying() {
                    return MusicPlayerService.this.isPlaying();
                }
            });
            MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PREPARE;
            //showNotification();
            notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
//            if(requestAudioFocus== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            //如果外部的监听器不为空
            if (null != sMusicPlayerInfoListener) {
                sMusicPlayerInfoListener.onPlayMusiconInfo(musicInfo, mCurrentPlayIndex);
            }
            //初始化音乐媒体播放器
            initMediaPlayer(musicInfo);

//            }else{
//                MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_ERROR;
//                if (null != mOnPlayerEventListeners) {
//                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
//                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"未成功获取音频输出焦点");
//                    }
//                }
//                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_ERROR,
//                        musicInfo.getAudioId()));
//                //showNotification();
//                notificationUtils.showNotification(getCurrentPlayerMusic(),this);
//            }
        } else {
            MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_ERROR;
            if (null != mOnPlayerEventListeners && mOnPlayerEventListeners.size() > 0) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
                    onPlayerEventListener.onMusicPathInvalid(musicInfo, mCurrentPlayIndex);
                }
            }

            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_ERROR,
                    musicInfo.getMediaId()));

            //onCompletionPlay();
        }
    }

    /**
     * 初始化音乐媒体播放器
     *
     * @param musicInfo
     */
    public void initMediaPlayer(AudioInfo musicInfo) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setLooping(mLoop);
            mMediaPlayer.setWakeMode(MusicPlayerService.this, PowerManager.PARTIAL_WAKE_LOCK);
            //更新播放状态
            MusicStatus musicStatus = new MusicStatus();
            musicStatus.setId(String.valueOf(musicInfo.getMediaId()));
            String frontPath = MusicUtils.getInstance().getMusicFrontPath(musicInfo);
            musicStatus.setCover(frontPath);
            musicStatus.setTitle(musicInfo.getTitle());
            musicStatus.setPlayerStatus(MusicStatus.PLAYER_STATUS_PREPARED);
            MusicPlayerManager.getInstance().observerUpdata(musicStatus);

            Class<MediaPlayer> clazz = MediaPlayer.class;
            Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
            String path = getPlayPath(musicInfo.getMediaUrl());
            LogUtil.d(MusicPlayerManager.TAG, "startPlay-->: ID:" + musicInfo.getMediaId() + ",TITLE:"
                    + musicInfo.getTitle() + ",PATH:" + path);
            method.invoke(mMediaPlayer, path, null);

            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, "播放准备中");
                }
            }
//            if(null!=mWifiLock){
//                mWifiLock.acquire();
//            }

            mMediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_ERROR;
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, "播放失败，" + e.getMessage());
                }
            }
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_ERROR,
                    musicInfo.getMediaId()));
            notificationUtils.cleanNotification();
        }
    }

    /**
     * 转换播放地址
     *
     * @param filePath
     * @return 真实的播放地址
     */
    private String getPlayPath(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            if (filePath.startsWith("http:") || filePath.startsWith("https:")) {
                return filePath;
            }
            return Uri.parse(filePath).getPath();
        }
        return null;
    }

    /**
     * 播放器内部根据播放模式自动开始下个任务
     */
    private void onCompletionPlay() {
        if (null != getCurrentPlayList() && mAudios.size() > 0) {
            switch (getPlayerModel()) {

                //列表循环
                case MusicConstants.MUSIC_MODEL_LOOP:
                    if (mCurrentPlayIndex >= mAudios.size() - 1) {
                        mCurrentPlayIndex = 0;
                    } else {
                        mCurrentPlayIndex++;
                    }
                        postViewHandlerCurrentPosition(mCurrentPlayIndex);
                        startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
    }

    /**
     * 上报给UI组件，当前内部自动正在处理的对象位置
     *
     * @param currentPlayIndex 数据源中的Index
     */
    private void postViewHandlerCurrentPosition(int currentPlayIndex) {
        if (null != mOnPlayerEventListeners && null != mAudios && mAudios.size() > currentPlayIndex) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onPlayMusiconInfo((AudioInfo) mAudios.get(currentPlayIndex), currentPlayIndex);
            }
        }
    }

    private void postVedioViewHandlerCurrentPosition(int currentPlayIndex) {
        if (null != mOnVedioPlayerEventListeners && null != mAudios && mAudios.size() > currentPlayIndex) {
            for (VedioPlayerEventListener onPlayerEventListener : mOnVedioPlayerEventListeners) {
                onPlayerEventListener.onPlayVedioInfo((AudioInfo) mAudios.get(currentPlayIndex), currentPlayIndex, getCurrentPageType(), getAddTime());
            }
        }
    }

    //===========================================播放回调============================================

    /**
     * 缓冲完成后调用,只有在缓冲成功，才是正在播放状态
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        mediaPlayer.start();
        MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PLAYING;
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onPrepared(mediaPlayer.getDuration());
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, "播放中");
            }
        }
        //通知主页，如果关心正在、最近播放的话
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());

    }

    /**
     * 音乐播放器回调：播放完成调用,播放完成后不再主动停止，自动开始下一曲
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //showNotification();
        notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
        MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_STOP;
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
            }
        }
        //播放完成，根据用户设置的播放模式来自动播放下一首
        onCompletionPlay();
    }

    /**
     * 缓冲进度，MediaPlayer的onBufferingUpdate到了100%就不再回调
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
        this.mBufferProgress = progress;
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onBufferingUpdate(progress);
            }
        }
    }

    /**
     * 设置进度完成调用
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

        MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_PLAYING;
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
            }
        }
    }

    /**
     * 播放失败
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int event, int extra) {
        LogUtil.d(TAG, "onError--EVENT:" + event + ",EXTRA:" + extra);
        if (!isVedioPlayPage()) {
            MusicPlayerService.this.mMusicPlayerState = MusicConstants.MUSIC_PLAYER_ERROR;
            onReset();
            String content = getErrorMessage(event);
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, content);
                }
            }
            //简单的更新播放状态
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_STOP));
            notificationUtils.showNotification(getCurrentPlayerMusic(mCurrentPlayIndex), this);
            //下一首
            if (isCheckNetwork()) {
                LogUtil.d(MusicPlayerManager.TAG, "自动播放下一首 ");
                //onCompletionPlay();
            }
        }

        return false;
    }

    private String getErrorMessage(int event) {
        String content = "播放失败，未知错误";
        switch (event) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                content = "播放失败，未知错误";
                break;
            //收到次错误APP必须重新实例化新的MediaPlayer
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                content = "播放器内部错误";
                break;
            //流开始位置错误
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                content = "媒体流错误";
                break;
            //IO,超时错误
            case MediaPlayer.MEDIA_ERROR_IO:
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                content = "网络连接超时";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                content = "请求播放失败：403";
                break;
            case -2147483648:
                content = "系统错误";
                break;
        }
        if (!isCheckNetwork()) {
            content = "设备未连网，请检查网络连接！";
        }
        return content;
    }

    /**
     * 获取音频信息
     */
    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int event, int extra) {
        int state = -1;
        if (event == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            state = MusicConstants.MUSIC_PLAYER_BUFFER;
        } else if (event == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            state = MusicConstants.MUSIC_PLAYER_PLAYING;
        } else if (event == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            state = MusicConstants.MUSIC_PLAYER_PLAYING;
        }
        if (state > -1) {
            MusicPlayerService.this.mMusicPlayerState = state;
        }
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState, null);
            }
        }
        return false;
    }

    /**
     * 获取当前设备是否有网
     *
     * @return 为true标识已联网
     */
    public boolean isCheckNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    //=========================================广播监听==============================================

    private class HeadsetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onReceive:action:" + action);
            //耳机拔出
            if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
                //屏幕点亮
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                //用户需要开启锁屏控制才生效
                if (!TextUtils.isEmpty(mLockActivityClass)) {
                    int playerState = getPlayerState();
                    if (playerState == MusicConstants.MUSIC_PLAYER_PREPARE
                            || playerState == MusicConstants.MUSIC_PLAYER_PLAYING) {
                        Intent startIntent = new Intent();
                        startIntent.setClassName(getPackageName(), mLockActivityClass);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplicationContext().startActivity(startIntent);
                    }
                }
                //前台进程-通知栏根点击事件
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW)) {
                long audioID = intent.getLongExtra(MusicConstants.MUSIC_KEY_MEDIA_ID, 0);
                if (audioID > 0) {
                    if (!TextUtils.isEmpty(mPlayerActivityClass)) {

                        //请注意，这里如果APP处于非活跃状态，默认是打开你清单文件的LAUNCHER Activity，
                        // 并入参audioid,Long类型：MusicConstants.KEY_MUSIC_ID。分两种场景处理
                        //1：如果你的APP正在运行并且播放器界面正在显示关心onNewIntent（），
                        // 如果APP正在再运行但播放器界面未打开，关心onCreate()。最终从intent取出MusicConstants.KEY_MUSIC_ID。

                        //2：如果你的APP被关闭了，没有Activity在栈中，关心你的LAUNCHER Activity 的 onCreate()
                        // 并获取intent,从intent取出MusicConstants.KEY_MUSIC_ID。自行处理跳转至播放器界面
                        boolean appRunning = MusicUtils.getInstance().isAppRunning(getApplicationContext(), getApplicationContext().getPackageName());
                        if (appRunning) {
                            //Player Activity
                            Intent startIntent = new Intent();
                            startIntent.setClassName(getPackageName(), mPlayerActivityClass);
                            startIntent.putExtra(MusicConstants.KEY_MUSIC_ID, audioID);
                            //如果播放器组件未启用，创建新的实例
                            //如果播放器组件已启用且在栈顶，复用播放器不传递任何意图
                            //反之则清除播放器之上的所有栈，让播放器组件显示在最顶层
                            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(startIntent);
                        } else {
                            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            launchIntent.putExtra(MusicConstants.KEY_MUSIC_ID, audioID);
                            context.startActivity(launchIntent);
                        }
                    }
                }
                //前台进程-上一首
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST)) {
                if (!ButtonUtils.isFastDoubleClick(1)) {
                    if (isVedioPlayPage()) {
                        playLastVedio();
                    } else {
                        playLastMusic();
                    }
                }
                //前台进程-下一首
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT)) {
                if (!ButtonUtils.isFastDoubleClick(2)) {
                    if (isVedioPlayPage()) {
                        playNextVedio();
                    } else {
                        playNextMusic();
                    }
                }

                //前台进程-暂停、开始
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE)) {
                LogUtil.d(MusicPlayerManager.TAG, "Service" + MusicPlayerManager.getInstance().getMyTypeName(getCurrentPageType()) + "isVedioPlayPage()--" + isVedioPlayPage());
                if (!ButtonUtils.isFastDoubleClick(0)) {
                    if (isVedioPlayPage()) {
                        if (getPlayerState() == MusicConstants.VEDIO_PLAYER_PAUSE) {
                            playStartVedio();
                        } else if (getPlayerState() == MusicConstants.VEDIO_PLAYER_PLAYING) {
                            playPauseVedio();
                        }
                    } else {
                        playOrPause();
                    }
                }

                //前台进程-关闭前台进程
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE)) {

                onStop();
            } else if (action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_DELECT)) {
                onStop();
            }
        }
    }

    /**
     * 清除通知，常驻进程依然保留(如果开启)
     *
     * @param notifiid 通知栏ID
     */
    private void cleanNotification(int notifiid) {
        notificationUtils.cleanNotification(notifiid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        distroy();
        if (null != mHeadsetBroadcastReceiver) {
            unregisterReceiver(mHeadsetBroadcastReceiver);
            mHeadsetBroadcastReceiver = null;
        }
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_DESTROY));
        cleanNotification();
        //mWifiLock=null;
        mIsPassive = false;
        mAudioFocusManager = null;
        if (null != mOnPlayerEventListeners) {
            mOnPlayerEventListeners.clear();
        }
        if (null != mOnVedioPlayerEventListeners) {
            mOnVedioPlayerEventListeners.clear();
        }
        if (null != mAudios) {
            mAudios.clear();
        }
        if (null != mAudioFocusManager) {
            mAudioFocusManager.onDestroy();
            mAudioFocusManager = null;
        }
        mNotificationManager = null;
    }

}
