package com.example.teacher_assistant_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.teacher_assistant_test.util.IDUSTool;

public class MainActivity extends AppCompatActivity {
    private Button bt1,bt2,bt3;
//    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏

        setContentView(R.layout.activity_main);

        initDataBase();

        bt1 = findViewById(R.id.Test);
        bt2 = findViewById(R.id.Test_2);
        bt3 = findViewById(R.id.Test_Insert);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);

                startActivity(intent);
            }
        });

//        bt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                private static fianl DELETE_DB = "DELETE FROM Student.db"
//            }
//        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main3Activity.class);

                startActivity(intent);
            }
        });
    }

    private void initDataBase() {
        IDUSTool idusTool = new IDUSTool(MainActivity.this);
//        SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getWritableDatabase();
        idusTool.insertStuDB("1", "Dan", "girl");
        idusTool.insertStuDB("2", "Jow", "boy");
        idusTool.insertStuDB("3", "Marry", "girl");
        idusTool.insertStuDB("4", "Jorge", "boy");
        idusTool.insertStuDB("5", "Judy", "girl");
        for (int i=1; i<=20; i++) {
            idusTool.insertStuDB(""+i+"", "", "");
        }



//        idusTool.insertStuMarkDB("2", "88");
//        idusTool.insertStuMarkDB("3", "99");

    }
}
