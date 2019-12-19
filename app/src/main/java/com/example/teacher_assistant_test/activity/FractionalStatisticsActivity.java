package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;

public class FractionalStatisticsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fractional_statistics);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏

        ImmersiveStatusBar.setImmersiveStatusBar(this);

        long test_id = getIntent().getLongExtra("test_id", 0);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy_info:

            case R.id.btn_refresh:

            default:
                break;
        }
    }

    /**
     * 传进来一个测试id，则可根据测试id查出对应test_time，test_full_score，连表查询出测试
     * @param context
     */
    public static void actionStart(Context context, long test_id) {
        Intent intent = new Intent(context, FractionalStatisticsActivity.class);
        intent.putExtra("test_id", test_id);
        context.startActivity(intent);
    }
}
