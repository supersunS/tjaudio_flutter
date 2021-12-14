package com.tojoy.musicplayer.listener;

import com.tojoy.musicplayer.model.AudioInfo;

public interface VedioPlayerEventListener {

    /**
     * 播放器所有状态回调
     * @param playerState 播放器内部状态
     */
    void onVedioPlayerState(int playerState,String message,int type,long time);

    /**
     * 当前正在播放的任务
     * @param musicInfo 正在播放的对象
     * @param position 当前正在播放的位置
     */

    void onPlayVedioInfo(AudioInfo musicInfo, int position,int type,long time);

}
