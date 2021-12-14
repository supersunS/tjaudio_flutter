package com.tojoy.musicplayer.model;

import android.text.TextUtils;

public class AudioInfo  {
    //Item类型
    public static final int ITEM_UNKNOWN=0;
    public static final int ITEM_DEFAULT=1;
    public static final int ITEM_TITLE=2;
    public static final int ITEM_MORE=3;
    public static final int ITEM_MUSIC=4;
    public static final int ITEM_ALBUM=5;

    //本地音乐
    public static final String TAG_LOCATION = "tag_location";
    //最近播放
    public static final String TAG_LAST_PLAYING = "tag_last_playing";
    //收藏
    public static final String TAG_COLLECT = "tag_collect";
    /**
     * 数据类别
     */
    //默认
    public static final String ITEM_CLASS_TYPE_DEFAULT="item_default";
    //专辑
    public static final String ITEM_CLASS_TYPE_ALBUM="item_album";
    //音乐
    public static final String ITEM_CLASS_TYPE_MUSIC="item_music";
    //标题
    public static final String ITEM_CLASS_TYPE_TITLE="item_title";
    //更多
    public static final String ITEM_CLASS_TYPE_MORE="item_title";

    private int itemType;
    /**
     * 必选字段
     */
    // * 音频唯一标识ID
    private String mediaId;
    // * 音频时长
    private long playbackDuration;
    // * 音频名称
    private String title;
    // * 音频封面
    private String coverUrl;
    // * 音频资源地址
    private String mediaUrl;
    // * 作者昵称
    private String auther;
    /**
     * 非必选
     */
    //作者ID
    private String userid;
    //作者头像
    private String avatar;
    //文件大小
    private long audioSize;
    //专辑名称
    private String audioAlbumName;
    //音频类型
    private String audioType;
    //多媒体描述
    private String audioDescribe;

    public String getAudioHashKey() {
        return audioHashKey;
    }

    public void setAudioHashKey(String audioHashKey) {
        this.audioHashKey = audioHashKey;
    }

    //这个字段请忽视。。。这个标识只适用于本Demo的酷狗API，由于数据中没有合适的ID字段，故使用这个字段作为一标识
    private String audioHashKey = "";
    //本地缓存时用到
    private long addtime;

    /**
     * 本地UI和历史记录交互字段
     */
    //UI正在播放选中交互
    protected boolean isSelected;
    //最近播放时间
    private long lastPlayTime;


    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public long getPlaybackDuration() {
        return playbackDuration;
    }

    public void setPlaybackDuration(long playbackDuration) {
        this.playbackDuration = playbackDuration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getAuther() {
        return auther;
    }

    public void setAuther(String auther) {
        this.auther = auther;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getAudioSize() {
        return audioSize;
    }

    public void setAudioSize(long audioSize) {
        this.audioSize = audioSize;
    }

    public String getAudioAlbumName() {
        return audioAlbumName;
    }

    public void setAudioAlbumName(String audioAlbumName) {
        this.audioAlbumName = audioAlbumName;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public String getAudioDescribe() {
        return audioDescribe;
    }

    public void setAudioDescribe(String audioDescribe) {
        this.audioDescribe = audioDescribe;
    }

    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public AudioInfo() {

    }


    @Override
    public String toString() {
        return "AudioInfo{" +
                "itemType=" + itemType +
                ", mediaId='" + mediaId + '\'' +
                ", playbackDuration=" + playbackDuration +
                ", title='" + title + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", auther='" + auther + '\'' +
                ", userid='" + userid + '\'' +
                ", avatar='" + avatar + '\'' +
                ", audioSize=" + audioSize +
                ", audioAlbumName='" + audioAlbumName + '\'' +
                ", audioType='" + audioType + '\'' +
                ", audioDescribe='" + audioDescribe + '\'' +
                ", audioHashKey='" + audioHashKey + '\'' +
                ", addtime=" + addtime +
                ", isSelected=" + isSelected +
                ", lastPlayTime=" + lastPlayTime +
                '}';
    }
}
