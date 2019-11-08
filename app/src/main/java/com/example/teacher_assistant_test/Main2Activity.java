package com.example.teacher_assistant_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.adapter.StudentAdapter;
import com.example.teacher_assistant_test.bean.Student;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.GetAlertDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private List<Student> studentList = new ArrayList<>();

    private long test_id;
    private String test_name;

    private StudentAdapter studentAdapter;
    private RecyclerView recyclerView;

    private boolean isIdSelectSortPressed;
    private ImageView id_select_sort;
    private boolean isScoreSelectSortPressed;
    private ImageView score_select_sort;

    private Button fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), new LayoutInflater.Factory2() {
//            @Nullable
//            @Override
//            public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//                AppCompatDelegate delegate = getDelegate();
//                View view = delegate.createView(parent, name, context, attrs);
//
//                if(name.equalsIgnoreCase("com.example.teacher_assistant_test.res.menu.main")
//                        || name.equalsIgnoreCase("com.example.teacher_assistant_test.view.menu.main")) {
//                    try {
//                        if(view instanceof TextView) {
//                            ((TextView) view).setTextColor(Color.GREEN);
//                        }
//                        return view;
//                    } catch (InflateException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return view;
//            }
//
//            @Nullable
//            @Override
//            public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//                return null;
//            }
//        });


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

        recyclerView = findViewById(R.id.Recycler_View_Student);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

        studentAdapter.setOnScoreFillListener(new StudentAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(int position, String score) {




                //编辑成绩监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < studentList.size()) {
                    //位置存在
                    //判断当前EditText中内容score是否为空
                    if(!TextUtils.isEmpty(score)) {
                        //不为空
                        studentList.get(position).setScore(score);
                        //先要判断编辑的score字符串是否符合规则，是则计算total_score,否则不计算
                        if(new CheckExpression().checkExpression(score)) {
//                        score = score.replaceAll(" ","");
                            int total_score = (int) new Calculator().calculate(score);
                            studentList.get(position).setTotal_score(total_score);

                            if(!recyclerView.isComputingLayout()) {
                                studentAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.save_data://监听菜单按钮
                //保存当前页面修改数据(只修改了score)到数据库
                //EditText(score)被实时监听，其编辑的数据实时更新在studentList中
                //所以将studentList的数据更新到数据库即可
                //因为只修改了score，所以只更新score
                //score只在StudentMark表中。。只更新StudentMark表中的score
                SQLiteDatabase db = MyDatabaseHelper.getInstance(Main2Activity.this);
                ContentValues values = new ContentValues();
                Iterator<Student> studentIterator = studentList.iterator();
                while(studentIterator.hasNext()) {
                    Student student = studentIterator.next();
                    int stu_id = student.getStu_id();
                    values.put("score", String.valueOf(student.getScore()));
                    values.put("total_score", student.getTotal_score());
                    Log.i("Main2Activity", "score:"+student.getScore()+" total_score:"+student.getTotal_score());
                    //该法不会将score算术表达式自动计算成结果更新
                    db.update("StudentMark", values, "test_id = ? AND stu_id = ?",
                            new String[]{""+test_id+"", ""+stu_id+""});
                    Toast.makeText(Main2Activity.this, "保存成功", Toast.LENGTH_SHORT).show();

                    //测试
//                    int i = db.update("StudentMark", values, "test_id = ? AND stu_id = ?",
//                            new String[]{"1051306716", "2"});
//                    Log.i("Main2Activity", "update:"+i);

                    //该法可行，但是会将score算术表达式自动计算成结果更新
//                    String sqlUpdate = "UPDATE StudentMark SET score ="+student.getScore()+", total_score ="+student.getTotal_score()
//                            +" WHERE test_id = "+test_id+" AND stu_id = "+stu_id+"";
//                    db.execSQL(sqlUpdate);
                    Log.i("Main2Activity", "test_id:"+test_id+" stu_id:"+stu_id);
                }
                db.close();
                break;
        }
        return super.onOptionsItemSelected(item);
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
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
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
