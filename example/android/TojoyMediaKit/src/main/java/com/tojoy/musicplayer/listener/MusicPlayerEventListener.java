package com.tojoy.musicplayer.listener;

import com.tojoy.musicplayer.model.AudioInfo;

public interface MusicPlayerEventListener {


    /**
     * 播放器所有状态回调
     * @param playerState 播放器内部状态
     */
    void onMusicPlayerState(int playerState,String message);

    /**
     * 播放器准备好了
     * @param totalDurtion 总时长
     */
    void onPrepared(long totalDurtion);

    /**
     * 缓冲百分比
     * @param percent 百分比
     * 此方法已被废弃，缓冲进度播放器内部使用变量储存，每隔500mm连同播放进度回调至组件
     * 合并至onTaskRuntime方法
     */
    @Deprecated
    void onBufferingUpdate(int percent);


    /**
     * 当前正在播放的任务
     * @param musicInfo 正在播放的对象
     * @param position 当前正在播放的位置
     */

    void onPlayMusiconInfo(AudioInfo musicInfo, int position);

    /**
     * 音频地址无效,组件可处理付费购买等逻辑
     * @param musicInfo 播放对象
     * @param position 索引
     */
    void onMusicPathInvalid(AudioInfo musicInfo, int position);

    /**
     * @param totalDurtion 音频总时间
     * @param currentDurtion 当前播放的位置
     * @param bufferProgress 当前缓冲进度
     */
    void onTaskRuntime(long totalDurtion, long currentDurtion, int bufferProgress);

}
