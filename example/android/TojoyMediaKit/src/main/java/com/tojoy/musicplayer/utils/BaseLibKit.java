package com.tojoy.musicplayer.utils;

import android.content.Context;

public class BaseLibKit {

    // context
    private static Context mContext;

    public static void init(Context context, String versionName) {
        mContext = context;
        mVersionName = versionName;
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 版本号
     *
     * @return
     */
    private static String mVersionName;

    public static String getVersionName() {
        return mVersionName;
    }
}
