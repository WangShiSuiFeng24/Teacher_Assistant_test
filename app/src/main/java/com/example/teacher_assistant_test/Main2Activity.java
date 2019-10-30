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
    private List<Mark> markList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initScore();
        RecyclerView recyclerView = findViewById(R.id.Recycler_View_Mark);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        MarkAdapter markAdapter = new MarkAdapter(markList);
        recyclerView.setAdapter(markAdapter);
    }

    private void initScore() {
        String sqlSelect="SELECT * FROM StudentMark";
        //扫描数据库，将信息放入markList
//        MyDatabaseHelper mdb = new MyDatabaseHelper(this, "Mark.db", null, 2);//打开数据库
//        SQLiteDatabase sd = mdb.getReadableDatabase();//获取数据库
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main2Activity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});
        while(cursor.moveToNext()){
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            double score = cursor.getDouble(cursor.getColumnIndex("score"));

            Mark mark = new Mark(stu_id, score);
            markList.add(mark);
        }
        cursor.close();
    }
}
