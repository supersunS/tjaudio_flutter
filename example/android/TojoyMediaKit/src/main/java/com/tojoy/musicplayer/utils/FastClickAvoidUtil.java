package com.tojoy.musicplayer.utils;


/**
 * create by fanxi
 * create on 2019/4/17
 * description 防止控件快速点击
 */
public class FastClickAvoidUtil {
    private static long lastClickTime;
    /**
     * 连续点击间隔时间
     */
    private final static int SPACE_TIME = 400;

    public synchronized static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClickDouble;
        if (currentTime - lastClickTime > SPACE_TIME) {
            isClickDouble = false;
        } else {
            isClickDouble = true;
        }
        lastClickTime = currentTime;
        return isClickDouble;
    }

    /**
     * 自定义间隔时间
     */
    public synchronized static boolean isDoubleClick(int spaceTime) {
        long currentTime = System.currentTimeMillis();
        boolean isClickDouble;
        if (currentTime - lastClickTime > spaceTime) {
            isClickDouble = false;
        } else {
            isClickDouble = true;
        }
        lastClickTime = currentTime;
        return isClickDouble;
    }

}
