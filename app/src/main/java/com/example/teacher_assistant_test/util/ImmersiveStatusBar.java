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
     * 解决着色式沉浸式状态栏问题,先去掉StatusBar,后添加"假"View
     * @param activity
     */
    public static void setImmersiveStatusBar(Activity activity) {
        Window window = activity.getWindow();

        //Build.VERSION.SDK_INT:获取系统属性配置文件中"ro.build.version.sdk"的值，该值即为当前设备的系统版本号。
        //Build.VERSION_CODES 类下包含对应各个版本的版本号信息。
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

             //除掉了半透明的状态栏
             //TRANSLUCENT:半透明的
            //View.SYSTEM_UI_FLAG_FULLSCREEN API 16 状态栏隐藏，效果同设置WindowManager.LayoutParams.FLAG_FULLSCREEN
            //View.SYSTEM_UI_FLAG_LAYOUT_STABLE API 16 保持整个View稳定, 常和控制System UI悬浮, 隐藏的Flags共用, 使View不会因为System UI的变化而重新layout
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);


            ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
//        int statusBarHeight = getStatusBarHeight(this);

            //获取StatusBar高度
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
