package com.example.teacher_assistant_test.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;

import com.example.teacher_assistant_test.R;

public class ImmersiveStatusBar {

    /**
     * 解决着色式沉浸式状态栏问题
     * @param activity
     */
    public static void setImmersiveStatusBar(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

            /**
             * 除掉了半透明的状态栏
             */
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);


            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
//        int statusBarHeight = getStatusBarHeight(this);
            int height = 0;
            int resourceId = activity.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                height = activity.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
            }
            int statusBarHeight = height;

            View mTopView = mContentView.getChildAt(0);
            if (mTopView != null && mTopView.getLayoutParams() != null && mTopView.getLayoutParams().height == statusBarHeight) {
                //避免重复添加 View
                mTopView.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                return;
            }
            //使 ChildView 预留空间
            if (mTopView != null) {
                ViewCompat.setFitsSystemWindows(mTopView, true);
            }

            //添加假 View
            mTopView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
            mTopView.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
            mContentView.addView(mTopView, 0, lp);
        }
    }
}
