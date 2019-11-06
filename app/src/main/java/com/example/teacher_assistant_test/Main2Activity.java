package com.example.teacher_assistant_test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.teacher_assistant_test.adapter.StudentAdapter;
import com.example.teacher_assistant_test.bean.Student;
import com.example.teacher_assistant_test.util.GetAlertDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private List<Student> studentList = new ArrayList<>();

    private Button clear_score;

    private long test_id;
    private String test_name;

    private StudentAdapter studentAdapter;

    private boolean isIdSelectSortPressed;
    private ImageView id_select_sort;
    private boolean isScoreSelectSortPressed;
    private ImageView score_select_sort;

    private Button fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        isIdSelectSortPressed = false;
        id_select_sort = findViewById(R.id.id_select_sort);
        isScoreSelectSortPressed = false;
        score_select_sort = findViewById(R.id.score_select_sort);

        Intent intent = getIntent();
        test_id = intent.getLongExtra("test_id", 0);
        test_name = intent.getStringExtra("test_name");
        Log.i("Main2Activity", "test_id:"+test_id+" test_name:"+test_name);

        initStudent();

        //设置标题栏文字
        setTitle(test_name);
//        if(studentList.size() != 0) {
//            String title = studentList.get(0).getTest_name();
//            Log.i("Main2Activity", "title:"+title);
//            if(!TextUtils.isEmpty(title)) setTitle(title);
//        }

        final RecyclerView recyclerView = findViewById(R.id.Recycler_View_Student);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

//        clear_score = findViewById(R.id.clear_score);
//        clear_score.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(Main2Activity.this,
//                        "Alarm", "即将清空成绩,慎重!!!", null, "确定清空!",
//                        "取消清空!");
//                alertDialog.setCancelable(false);
//                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        SQLiteDatabase database = MyDatabaseHelper.getInstance(Main2Activity.this);
//                        String deleteAll = "DELETE FROM StudentMark";
//                        database.execSQL(deleteAll);
//                        studentList.clear();
//                        studentAdapter.notifyDataSetChanged();
//                        initStudent();
//                        alertDialog.dismiss();
//                        Toast.makeText(Main2Activity.this, "成绩已清空", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到语音识别录成绩界面
                Intent intent = new Intent(Main2Activity.this, Main3Activity.class);
                startActivity(intent);
            }
        });
    }

    public void idSelectSortControl(View v) {
        if (!isIdSelectSortPressed) {
            isIdSelectSortPressed = true;
            id_select_sort.setImageResource(R.drawable.ic_drop_up);
            if (studentList.size() != 0) {
                Collections.sort(studentList, new Comparator<Student>() {
                    @Override
                    public int compare(Student o1, Student o2) {
                        return o1.getStu_id() - o2.getStu_id();
                    }
                });
                studentAdapter.notifyDataSetChanged();
            }
        } else {
            isIdSelectSortPressed = false;
            id_select_sort.setImageResource(R.drawable.ic_drop_down);
            if (studentList.size() != 0) {
                Collections.sort(studentList, new Comparator<Student>() {
                    @Override
                    public int compare(Student o1, Student o2) {
                        return o2.getStu_id() - o1.getStu_id();
                    }
                });
                studentAdapter.notifyDataSetChanged();
            }
        }
    }

    public void scoreSelectSortControl(View v) {
        if(!isScoreSelectSortPressed) {
            isScoreSelectSortPressed = true;
            score_select_sort.setImageResource(R.drawable.ic_drop_up);
            if(studentList.size() != 0) {
                Collections.sort(studentList, new Comparator<Student>() {
                    @Override
                    public int compare(Student o1, Student o2) {
                        return o1.getTotal_score() - o2.getTotal_score();
                    }
                });
                studentAdapter.notifyDataSetChanged();
            }
        } else {
            isScoreSelectSortPressed = false;
            score_select_sort.setImageResource(R.drawable.ic_drop_down);
            if(studentList.size() != 0) {
                Collections.sort(studentList, new Comparator<Student>() {
                    @Override
                    public int compare(Student o1, Student o2) {
                        return o2.getTotal_score() - o1.getTotal_score();
                    }
                });
                studentAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initStudent() {
        Log.i("Main2Activity", "开始初始化Student.........");
        String sqlSelect="SELECT StudentMark.stu_id,Student.stu_name,Student.stu_gender,StudentMark.test_id,StudentTest.test_name,StudentMark.score,StudentMark.total_score "
                + "FROM StudentMark INNER JOIN Student ON StudentMark.stu_id = Student.stu_id "
                + "INNER JOIN StudentTest ON StudentMark.test_id = StudentTest.test_id";
        //扫描数据库，将信息放入markList
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main2Activity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});

        //打印cursor中的行数
        Log.i("Main2Activity", "cursor.getCount():"+cursor.getCount());

        while(cursor.moveToNext()){
            int test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            Log.i("Main2Activity", "数据库test_id:"+test_id);

            //只有当查询出的条目的test_id等于传入的this.test_id时才将该条目add到List<Student>
            if(test_id == this.test_id) {
                int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
                String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));
                String score = cursor.getString(cursor.getColumnIndex("score"));
                int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));

                Student student = new Student(stu_id, stu_name, stu_gender, test_name, score, total_score);
                studentList.add(student);
            }
        }
        cursor.close();
    }
}
