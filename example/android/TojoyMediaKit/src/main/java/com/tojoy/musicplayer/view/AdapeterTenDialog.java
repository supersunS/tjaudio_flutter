package com.tojoy.musicplayer.view;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * function
 * Created by daibin
 * on 2020/9/18.
 * 邮箱：daibin@tojoy.com
 */
public class AdapeterTenDialog extends Dialog {
    public AdapeterTenDialog(@NonNull Context context) {
        super(context);
    }

    public AdapeterTenDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AdapeterTenDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Window window = getWindow();
        if (hasFocus && window != null) {
            View decorView = window.getDecorView();
            if (decorView.getHeight() == 0 || decorView.getWidth() == 0) {
                decorView.requestLayout();
            }
        }
    }
}
