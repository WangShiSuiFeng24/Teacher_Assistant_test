package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.library.SwitchButton;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean isShowGender;

    private TitleBarView titleBarView;

    private SwitchButton switchButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ImmersiveStatusBar.setImmersiveStatusBar(SettingActivity.this);

        titleBarView = findViewById(R.id.title5);
        initTitleBar();

        switchButton = findViewById(R.id.switch_button);
        initSwitchButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initTitleBar() {
        titleBarView.setTitleSize(20);
        titleBarView.setTitle(getString(R.string.set_up));
        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void tvRightClick() {
                //暂不处理
            }

            @Override
            public void ivRightClick(View view) {
                //暂不处理
            }
        });
    }

    private void initSwitchButton() {
        sharedPreferences = getSharedPreferences("count", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isShowGender = sharedPreferences.getBoolean("isShowGender", false);

        if (isShowGender) {
            switchButton.open();
        } else {
            switchButton.close();
        }

        switchButton.setOnStatusListener(new SwitchButton.OnStatusListener() {
            @Override
            public void onOpen() {
                editor.putBoolean("isShowGender", true);
                editor.apply();
            }

            @Override
            public void onClose() {
                editor.putBoolean("isShowGender", false);
                editor.apply();
            }
        });
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }
}
