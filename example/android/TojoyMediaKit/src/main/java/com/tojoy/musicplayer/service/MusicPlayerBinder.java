package com.tojoy.musicplayer.service;

import android.app.Notification;
import android.os.Binder;

import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.iinterface.MusicPlayerPresenter;
import com.tojoy.musicplayer.listener.MusicPlayerEventListener;
import com.tojoy.musicplayer.listener.MusicPlayerInfoListener;
import com.tojoy.musicplayer.listener.VedioPlayerEventListener;
import com.tojoy.musicplayer.model.AudioInfo;

import java.util.List;

public class MusicPlayerBinder extends Binder {

    private final MusicPlayerPresenter mPresenter;

    public MusicPlayerBinder(MusicPlayerPresenter presenter){
        this.mPresenter=presenter;
    }


    public void updateCurrentIndex(int index){
        if(null!=mPresenter){
            mPresenter.updateCurrentIndex(index);
        }
    }
    public void setCurrentPageType(int type,long addTime){
        if(null!=mPresenter){
            mPresenter.setCurrentPageType(type,addTime);
        }
    }
    public void setVedioPlayPage(boolean isBossVedioPage){
        if(null!=mPresenter){
            mPresenter.setVedioPlayPage(isBossVedioPage);
        }
    }
    public void startPlayMusic(List<?> musicList, int position){
        if(null!=mPresenter){
            mPresenter.startPlayMusic(musicList,position);
        }
    }

    public void setLockForeground(boolean enable) {
        if(null!=mPresenter){
            mPresenter.setLockForeground(enable);
        }
    }

    public void setNotificationEnable(boolean notificationEnable) {
        if(null!=mPresenter){
            mPresenter.setNotificationEnable(notificationEnable);
        }
    }

    public void setPlayerActivityName(String className) {
        if(null!=mPresenter){
            mPresenter.setPlayerActivityName(className);
        }
    }

    public void setLockActivityName(String className) {
        if(null!=mPresenter){
            mPresenter.setLockActivityName(className);
        }
    }

    public void startPlayMusic(int position){
        if(null!=mPresenter){
            mPresenter.startPlayMusic(position);
        }
    }
    public void startPlayVedio(int position){
        if(null!=mPresenter){
            mPresenter.startPlayVedio(position);
        }
    }
    public void addPlayMusicToTop(AudioInfo audioInfo){
        if(null!=mPresenter){
            mPresenter.addPlayMusicToTop(audioInfo);
        }
    }

    public void playOrPause(){
        if(null!=mPresenter){
            mPresenter.playOrPause();
        }
    }

    public void pause(){
        if(null!=mPresenter){
            mPresenter.pause();
        }
    }

    public void play(){
        if(null!=mPresenter){
            mPresenter.play();
        }
    }

    public void setLoop(boolean loop) {
        if (null!=mPresenter) {
            mPresenter.setLoop(loop);
        }
    }

    public void continuePlay(String sourcePath) {
        if (null!=mPresenter) {
            mPresenter.continuePlay(sourcePath);
        }
    }

    public void continuePlay(String sourcePath,int position) {
        if (null!=mPresenter) {
            mPresenter.continuePlay(sourcePath,position);
        }
    }

    public void onReset(){
        if(null!=mPresenter) mPresenter.onReset();
    }

    public void onStop(){
        if(null!=mPresenter) mPresenter.onStop();
    }

    public void cleanMusicData(){
        if(null!=mPresenter) mPresenter.cleanMusicData();
    }
    public void updateMusicPlayerData(List<?> audios, int index) {
        if(null!=mPresenter){
            mPresenter.updateMusicPlayerData(audios,index);
        }
    }
    public void updateMusicPlayerData(List<?> audios) {
        if(null!=mPresenter){
            mPresenter.updateMusicPlayerData(audios);
        }
    }

    public void copyCacheMusicList(){
        if(null!=mPresenter){
            mPresenter.copyCacheMusicList();
        }
    };

    public int setPlayerModel(int model) {
        if (null!=mPresenter) {
            return mPresenter.setPlayerModel(model);
        }
        return MusicConstants.MUSIC_MODEL_LOOP;
    }

    public int getPlayerModel(){
        if (null!=mPresenter) {
            return mPresenter.getPlayerModel();
        }
        return MusicConstants.MUSIC_MODEL_LOOP;
    }

    public void onSeekTo(long currentTime){
        if(null!=mPresenter) mPresenter.seekTo(currentTime);
    }


    public void playLastMusic() {
        if(null!=mPresenter){
            mPresenter.playLastMusic();
        }
    }

    public void playNextMusic() {
        if(null!=mPresenter){
            mPresenter.playNextMusic();
        }
    }
    public void playNextVedio(){
        if(null!=mPresenter){
            mPresenter.playNextVedio();
        }
    };
    public void playLastVedio(){
        if(null!=mPresenter){
            mPresenter.playLastVedio();
        }
    };
    public void playPauseVedio(){
        if(null!=mPresenter){
            mPresenter.playPauseVedio();
        }
    };
    public void playStartVedio(){
        if(null!=mPresenter){
            mPresenter.playStartVedio();
        }
    };
    public int playLastIndex() {
        if(null!=mPresenter){
            return mPresenter.playLastIndex();
        }
        return -1;
    }

    public int playNextIndex() {
        if(null!=mPresenter){
            return mPresenter.playNextIndex();
        }
        return -1;
    }

    public int playRandomNextIndex() {
        if(null!=mPresenter){
            return mPresenter.playRandomNextIndex();
        }
        return -1;
    }

    public boolean isPlaying(){
        if(null!=mPresenter){
            return mPresenter.isPlaying();
        }
        return false;
    }

    public long getDurtion(){
        if(null!=mPresenter) {
            return mPresenter.getDurtion();
        }
        return 0;
    }

    public String getCurrentPlayerID() {
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerID();
        }
        return "";
    }
    public int getIndexById(String id) {
        if(null!=mPresenter) {
            return mPresenter.getIndexById(id);
        }
        return -1;
    }


    public AudioInfo getCurrentPlayerMusic(int index){
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerMusic(index);
        }
        return null;
    }
    public AudioInfo getCurrentPlayerMusic(){
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerMusic();
        }
        return null;
    }
    public String getCurrentPlayerHashKey(){
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerHashKey();
        }
        return "";
    }

    public List<?> getCurrentPlayList() {
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayList();
        }
        return null;
    }

    public List<?> getCachemAudios() {
        if(null!=mPresenter) {
            return mPresenter.getCachemAudios();
        }
        return null;
    }

    public void setPlayingChannel(int channel) {
        if(null!=mPresenter){
            mPresenter.setPlayingChannel(channel);
        }
    }


    public int getPlayingChannel() {
        if(null!=mPresenter){
            return mPresenter.getPlayingChannel();
        }
        return MusicConstants.CHANNEL_NET;
    }


    public void onCheckedPlayerConfig(){
        if(null!=mPresenter) mPresenter.onCheckedPlayerConfig();
    }

    public void onCheckedCurrentPlayTask(){
        if(null!=mPresenter) mPresenter.onCheckedCurrentPlayTask();
    }

    public int getPlayerState() {
        if(null!=mPresenter){
            return mPresenter.getPlayerState();
        }
        return 0;
    }

    public void setPlayerState(int playerState){
        if(null!=mPresenter){
            mPresenter.setPlayerState(playerState);
        }
    }

    public  void addOnVedioPlayerEventListener(VedioPlayerEventListener listener){
        if(null!=mPresenter) mPresenter.addOnVedioPlayerEventListener(listener);
    };

    public void addOnPlayerEventListener(MusicPlayerEventListener listener) {
        if(null!=mPresenter) mPresenter.addOnPlayerEventListener(listener);
    }
    public void removeVedioPlayerListener(VedioPlayerEventListener listener) {
        if(null!=mPresenter) mPresenter.removeVedioPlayerListener(listener);
    }
    public void removePlayerListener(MusicPlayerEventListener listener) {
        if(null!=mPresenter) mPresenter.removePlayerListener(listener);
    }

    public void removeAllPlayerListener() {
        if(null!=mPresenter) mPresenter.removeAllPlayerListener();
    }

    public void setPlayInfoListener(MusicPlayerInfoListener listener) {
        if(null!=mPresenter) mPresenter.setPlayInfoListener(listener);
    }

    public void removePlayInfoListener() {
        if(null!=mPresenter) mPresenter.removePlayInfoListener();
    }


    public void createMiniJukeboxWindow(){
        if(null!=mPresenter){
            mPresenter.createMiniJukeboxWindow();
        }
    }

    public void startServiceForeground(){
        if(null!=mPresenter){
            mPresenter.startServiceForeground();
        }
    }

    public void startServiceForeground(Notification notification){
        if(null!=mPresenter){
            mPresenter.startServiceForeground(notification);
        }
    }

    public void startServiceForeground(Notification notification,int notifiid){
        if(null!=mPresenter){
            mPresenter.startServiceForeground(notification,notifiid);
        }
    }

    public void stopServiceForeground(){
        if(null!=mPresenter){
            mPresenter.stopServiceForeground();
        }
    }

    public void createWindowJukebox(){
        if(null!=mPresenter){
            mPresenter.createWindowJukebox();
        }
    }

    public void startNotification() {
        if(null!=mPresenter){
            mPresenter.startNotification();
        }
    }
    public void startNotification(int index) {
        if(null!=mPresenter){
            mPresenter.startNotification(index);
        }
    }
    public void startNotification(Notification notification) {
        if(null!=mPresenter){
            mPresenter.startNotification(notification);
        }
    }

    public void startNotification(Notification notification, int notifiid) {
        if(null!=mPresenter){
            mPresenter.startNotification(notification,notifiid);
        }
    }

    public void updateNotification() {
        if(null!=mPresenter){
            mPresenter.updateNotification();
        }
    }

    public void cleanNotification() {
        if(null!=mPresenter){
            mPresenter.cleanNotification();
        }
    }
}
