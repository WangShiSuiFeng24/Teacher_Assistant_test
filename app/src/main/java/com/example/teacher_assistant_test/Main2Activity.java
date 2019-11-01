package com.example.teacher_assistant_test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private List<Student> studentList = new ArrayList<>();

    private Button clear_score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initStudent();
        final RecyclerView recyclerView = findViewById(R.id.Recycler_View_Student);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        final StudentAdapter studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

        clear_score = findViewById(R.id.clear_score);
        clear_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                builder.setCancelable(false)
                        .setTitle("Alarm")
                        .setMessage("即将清空成绩,慎重!!!")
                        .setPositiveButton("确定清空!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase database = MyDatabaseHelper.getInstance(Main2Activity.this);
                                String deleteAll = "DELETE FROM StudentMark";
                                database.execSQL(deleteAll);
                                studentList.clear();
                                studentAdapter.notifyDataSetChanged();
                                initStudent();
                                Toast.makeText(Main2Activity.this, "成绩已清空", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消清空!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });


    }

    private void initStudent() {
        String sqlSelect="SELECT Student.stu_id,Student.stu_name,Student.stu_gender,StudentMark.score,StudentMark.total_score FROM Student LEFT JOIN StudentMark ON Student.stu_id = StudentMark.stu_id";
        //扫描数据库，将信息放入markList
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
