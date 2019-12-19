package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;

public class FractionalStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fractional_statistics);

        ImmersiveStatusBar.setImmersiveStatusBar(this);
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, FractionalStatisticsActivity.class);
        context.startActivity(intent);
    }
}
