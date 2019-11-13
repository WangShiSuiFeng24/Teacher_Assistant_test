package com.example.teacher_assistant_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.teacher_assistant_test.adapter.StudentInfoAdapter;
import com.example.teacher_assistant_test.bean.StudentInfo;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;

import java.util.ArrayList;
import java.util.List;

public class EditStudentInfoActivity extends AppCompatActivity {

    private List<StudentInfo> studentInfoList = new ArrayList<>();

    private RecyclerView recyclerView;
    private StudentInfoAdapter studentInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_info);

        ImmersiveStatusBar.setImmersiveStatusBar(EditStudentInfoActivity.this);

        TitleBarView titleBarView = findViewById(R.id.title4);
        titleBarView.setTitleSize(20);
        titleBarView.setTitle("学生基本信息");
        titleBarView.setRightTextColor(Color.parseColor("#808080"));
        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void tvRightClick() {
                //判断数据是否更新，若未更新，则设置点击无效，不处理
                //否则，保存更新数据到数据库
            }

            @Override
            public void ivRightClick(View view) {
                //无图片，不处理
            }
        });

        initStudentInfo();
        recyclerView = findViewById(R.id.Recycler_View_StudentInfo);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        studentInfoAdapter = new StudentInfoAdapter(studentInfoList);
        recyclerView.setAdapter(studentInfoAdapter);



    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, EditStudentInfoActivity.class);
        context.startActivity(intent);
    }

    private void initStudentInfo() {
        String sqlSelect = "SELECT stu_id,stu_name,stu_gender FROM Student";

        //扫描数据库，将信息放入studentInfoList
        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

        while (cursor.moveToNext()) {
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
            String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));

            StudentInfo studentInfo = new StudentInfo(stu_id, stu_name, stu_gender);

            studentInfoList.add(studentInfo);
        }
        cursor.close();
    }
}
