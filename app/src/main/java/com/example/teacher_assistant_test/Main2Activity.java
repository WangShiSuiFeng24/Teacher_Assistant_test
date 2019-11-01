package com.example.teacher_assistant_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private List<Student> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initStudent();
        RecyclerView recyclerView = findViewById(R.id.Recycler_View_Student);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        StudentAdapter studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);
    }

    private void initStudent() {
        String sqlSelect="SELECT Student.stu_id,Student.stu_name,Student.stu_gender,StudentMark.score,StudentMark.total_score FROM Student LEFT JOIN StudentMark ON Student.stu_id = StudentMark.stu_id";
        //扫描数据库，将信息放入markList
//        MyDatabaseHelper mdb = new MyDatabaseHelper(this, "Student.db", null, 2);//打开数据库
//        SQLiteDatabase sd = mdb.getReadableDatabase();//获取数据库
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main2Activity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});
        while(cursor.moveToNext()){
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
            String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
            String score = cursor.getString(cursor.getColumnIndex("score"));
            int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));

            Student student = new Student(stu_id, stu_name, stu_gender, score, total_score);
            studentList.add(student);
        }
        cursor.close();
    }
}
