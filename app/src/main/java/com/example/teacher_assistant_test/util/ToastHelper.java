package com.example.teacher_assistant_test.util;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by Andong Ming on 2019.12.18.
 */
public class ToastHelper {

    /**
     * 显示toast，自己定义显示长短。
     * @param activity 传入context
     * @param word 我们需要显示的toast的内容
     * @param time length  long类型，我们传入的时间长度（如500）
     */
    public static void showToast(Activity activity, String word, long time) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, time);
            }
        });
    }
}
