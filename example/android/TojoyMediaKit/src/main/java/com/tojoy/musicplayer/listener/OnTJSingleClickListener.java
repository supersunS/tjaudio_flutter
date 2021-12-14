package com.tojoy.musicplayer.listener;

import android.view.View;

import com.tojoy.musicplayer.utils.ButtonUtils;

/**
 * desc   : 防止快速点击拦截
 * author : qll
 * e-mail : 827690573@qq.com
 * Date   : 2021/3/17
 **/
public abstract class OnTJSingleClickListener implements View.OnClickListener{

    public abstract void onSingleClick(View view);

    @Override
    public void onClick(View v) {
        if (v != null && !ButtonUtils.isFastDoubleClick(v.getId())) {
            onSingleClick(v);
        }
    }
}
