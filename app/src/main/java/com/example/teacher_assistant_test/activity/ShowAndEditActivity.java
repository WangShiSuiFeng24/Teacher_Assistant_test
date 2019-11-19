package com.example.teacher_assistant_test.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.StudentAdapter;
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Student;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.ExcelUtils;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.StrProcess;
import com.github.clans.fab.FloatingActionButton;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

public class ShowAndEditActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 2;

    private List<Student> studentList = new ArrayList<>();

    //备份，即使当前页面被编辑后，students始终保存刚进入页面时的数据
    private List<Student> backUpStudentList = new ArrayList<>();

    private long test_id;
    private String test_name;

    private StudentAdapter studentAdapter;
    private RecyclerViewEmptySupport recyclerView;

    private boolean isIdSelectSortPressed;
    private ImageView id_select_sort;
    private boolean isScoreSelectSortPressed;
    private ImageView score_select_sort;

    private RecognizerDialog mIatDialog = null;
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();

    private boolean isUpdate = false;

    //导出Excel
    private ArrayList<ArrayList<String>> recordList;
    private static String[] title = {"学号", "姓名", "性别", "成绩", "总成绩"};
    private File file;
    private String fileName;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_and_edit);

        ImmersiveStatusBar.setImmersiveStatusBar(this);

        isIdSelectSortPressed = false;
        id_select_sort = findViewById(R.id.id_select_sort);
        isScoreSelectSortPressed = false;
        score_select_sort = findViewById(R.id.score_select_sort);

        Intent intent = getIntent();
        test_id = intent.getLongExtra("test_id", 0);
        test_name = intent.getStringExtra("test_name");
        Log.i("ShowAndEditActivity", "test_id："+test_id+" test_name："+test_name);

        final TitleBarView titleBarView = findViewById(R.id.title2);
        titleBarView.setTitleSize(20);
        titleBarView.setTitle(test_name);
        titleBarView.setRightTextColor(Color.parseColor("#808080"));
        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void tvRightClick() {
                if(!isUpdate) {
                    //数据没有修改或增添，点击无效，不处理
                } else {
                    //保存当前页面修改数据(只修改了score)到数据库
                    //EditText(score)被实时监听，其编辑的数据实时更新在studentList中
                    //所以将studentList的数据更新到数据库即可
                    //因为只修改了score，所以只更新score
                    //score只在StudentMark表中。。只更新StudentMark表中的score

                    //检查studentList中是否有非法score   //增加检查studentList中是否有非法id
                    SQLiteDatabase db = MyDatabaseHelper.getInstance(ShowAndEditActivity.this);
                    boolean isLegal = true;
                    for(Student student : studentList) {
//                    int stu_id = student.getStu_id();
//                    Cursor cursor1 = db.query("Student", new String[] {"stu_id"}, "stu_id = ?",
//                            new String[] {""+stu_id+""}, null, null, null);
//
//                    if(cursor1.getCount() == 0) {
//                        AlertDialog alertDialog = GetAlertDialog.getAlertDialog(ShowAndEditActivity.this, "Alarm",
//                                "数据库中不存在学号:" + stu_id +"的同学,是否添加该同学", null, "是", "否");
//                        alertDialog.setCancelable(false);
//                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//
//                            }
//                        });
//                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                            }
//                        });
//                        isLegal = false;
//                    }

                        if(!new CheckExpression().checkExpression(student.getScore())) {
                            Toast.makeText(ShowAndEditActivity.this, "成绩："+student.getScore()+" 非法", Toast.LENGTH_SHORT).show();
                            isLegal = false;
                        }
                    }

                    if(isLegal) {
                        //先删除数据库同studentList中学号的学生，再将studentList直接插入数据库
                        for(int i=0; i<backUpStudentList.size(); i++) {
                            int delete_stu_id = backUpStudentList.get(i).getStu_id();
                            db.delete("StudentMark", "stu_id = ?", new String[]{""+delete_stu_id+""});
                        }//先删
                        for(Student student : studentList) {
                            String stu_id = String.valueOf(student.getStu_id());

                            String score= student.getScore();
                            int total_score = student.getTotal_score();
                            new IDUSTool(ShowAndEditActivity.this).insertStuMarkDB(stu_id, test_id, score, total_score);
                        }//再插入
                        db.close();
                        Toast.makeText(ShowAndEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();

                        /*
                        //更新相同学号stu_id的score和total_score
                        ContentValues values = new ContentValues();
                        Iterator<Student> studentIterator = studentList.iterator();
                        while(studentIterator.hasNext()) {
                            Student student = studentIterator.next();
                            int stu_id = student.getStu_id();
                            values.put("score", String.valueOf(student.getScore()));
                            values.put("total_score", student.getTotal_score());
                            Log.i("ShowAndEditActivity", "score："+student.getScore()+" total_score:"+student.getTotal_score());
                            //该法不会将score算术表达式自动计算成结果更新
                            db.update("StudentMark", values, "test_id = ? AND stu_id = ?",
                                    new String[]{""+test_id+"", ""+stu_id+""});
//                            Toast.makeText(ShowAndEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                            //测试
//                    int i = db.update("StudentMark", values, "test_id = ? AND stu_id = ?",
//                            new String[]{"1051306716", "2"});
//                    Log.i("ShowAndEditActivity", "update:"+i);

                            //该法可行，但是会将score算术表达式自动计算成结果更新
//                    String sqlUpdate = "UPDATE StudentMark SET score ="+student.getScore()+", total_score ="+student.getTotal_score()
//                            +" WHERE test_id = "+test_id+" AND stu_id = "+stu_id+"";
//                    db.execSQL(sqlUpdate);
                            Log.i("ShowAndEditActivity", "test_id："+test_id+" stu_id："+stu_id);
                        }


                        //合法时插入Student......item
                        //向StudentMark表中插入stu_id,test_id,score,total_score
                        for(Student student : studentList) {
                            int stu_id = student.getStu_id();

                            Cursor cursor2 = db.query("StudentMark", new String[] {"stu_id, test_id"}, "stu_id = ? AND test_id = ?",
                                    new String[]{""+stu_id+"", ""+test_id+""}, null, null, null);

                            if(cursor2.getCount() == 0) {
                                values.clear();
                                values.put("stu_id", student.getStu_id());
                                values.put("test_id", test_id);
                                values.put("score", student.getScore());
                                values.put("total_score", student.getTotal_score());

                                db.insert("StudentMark", null, values);
                            } else {
                                Log.d(ShowAndEditActivity.this.getLocalClassName(), "StudentMark表中已存在学号：" + stu_id + " 测试号：" + test_id);
                            }
                            cursor2.close();
                        }
                        db.close();
                        Toast.makeText(ShowAndEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                        */
                    }
                }
            }

            @Override
            public void ivRightClick(View view) {
                if(!isUpdate) {
                    //先判断权限是否授予，没有则单独申请，有则继续
                    if(ContextCompat.checkSelfPermission(ShowAndEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ShowAndEditActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
                    } else {
                        exportSheet();

                        Uri shareFileUri = FileUtil.getFileUri(ShowAndEditActivity.this, ShareContentType.FILE, new File(fileName));

                        new Share2.Builder(ShowAndEditActivity.this)
                                //指定分享的文件类型
                                .setContentType(ShareContentType.FILE)
                                //设置要分享的文件Uri
                                .setShareFileUri(shareFileUri)
                                //设置分享选择器的标题
                                .setTitle("Share File")
                                .build()
                                //发起分享
                                .shareBySystem();
                    }
                }
            }
        });

        initStudent();

        recyclerView = findViewById(R.id.Recycler_View_Student);
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyMessage = findViewById(R.id.empty_message);
        emptyMessage.setText("成绩数据为空，请 点击下方按钮创建学生成绩数据。");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setEmptyView(emptyView);

        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

        studentAdapter.setOnItemClickListener(new StudentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //单击不处理
            }

            @Override
            public void onItemLongClick(final int position) {
                //长按则删除
                //只先从当前页面删除，后面还有保存按钮把关
                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(ShowAndEditActivity.this,
                        "alarm", "将要删除学号为：" + studentList.get(position).getStu_id() + "的条目",
                        null, "yes", "no");
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        studentAdapter.remove(position);
                        alertDialog.dismiss();
                    }
                });

                isUpdate = true;
                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                titleBarView.setRightDrawable(R.drawable.ic_export_excel_gray);
            }
        });

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

                                isUpdate = true;
                                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                titleBarView.setRightDrawable(R.drawable.ic_export_excel_gray);
                            }
                        }
                    }
                }
            }
        });

        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(ShowAndEditActivity.this.getLocalClassName(), "SpeechRecognizer init() code = " + code);
                if(code != ErrorCode.SUCCESS) {
                    Toast.makeText(ShowAndEditActivity.this, "初始化失败，错误码：" + code + "，请点击网址https://www.xfyun.cn/document/error-code查询解决方案", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRecognizerDialogListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(this.getClass().getName(), "recognizer json result：" + results.getResultString());

                String resultStr = updateResult(results);

                if(isLast) {
                    Log.d(ShowAndEditActivity.this.getLocalClassName(), "recognizer result： " + resultStr);

                    List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

                    if(newMarkList == null) {
                        AlertDialog alertDialog = GetAlertDialog
                                .getAlertDialog(ShowAndEditActivity.this, "Tip",
                                        "语音识别结果为："+resultStr+"\r\n无有效成绩数据，请重新录音！\r\n语音录成绩格式请参照：＂8号 88(+8)分＂这样效果会更好哦！",
                                        null, "OK", "CANCEL");
                        alertDialog.setCanceledOnTouchOutside(false);
                    } else {
                        Iterator<Mark> iteratorNew = newMarkList.iterator();

                        while(iteratorNew.hasNext()) {
                            final Mark newMark = iteratorNew.next();

                            if(studentList.size() == 0) {
                                //第一次添加
                                //先根据学号stu_id查出姓名stu_name和stu_gender
                                SQLiteDatabase db = MyDatabaseHelper.getInstance(ShowAndEditActivity.this);
                                Cursor cursor = db.query("Student", new String[]{"stu_name", "stu_gender"}, "stu_id = ?",
                                        new String[]{""+newMark.getStu_id()+""}, null, null, null);
                                String stu_name = null;
                                String stu_gender = null;
                                while (cursor.moveToNext()) {
                                    stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                                    stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                                }
                                cursor.close();

                                Student student = new Student();
                                student.setStu_id(Integer.parseInt(newMark.getStu_id()));

                                student.setStu_name(stu_name);
                                student.setStu_gender(stu_gender);

                                student.setScore(newMark.getScore());
                                student.setTotal_score(newMark.getTotal_score());

                                studentList.add(student);
                                studentAdapter.notifyDataSetChanged();

                                isUpdate = true;
                                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                titleBarView.setRightDrawable(R.drawable.ic_export_excel_gray);

                            } else {
                                boolean flag = false;
                                for(int i=0; i<studentList.size(); i++) {
                                    final Student student = studentList.get(i);

                                    if(newMark.getStu_id().equals(String.valueOf(student.getStu_id()))) {
                                        flag = true;
                                        final AlertDialog alertDialog = GetAlertDialog
                                                .getAlertDialog(ShowAndEditActivity.this,
                                                        "Alarm", "学号："+newMark.getStu_id()+"已存在，"+
                                                                "是否需要更改成绩："+student.getScore()+"为："+newMark.getScore(),
                                                        null, "OK", "CANCEL");
                                        alertDialog.setCanceledOnTouchOutside(false);

                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                student.setScore(newMark.getScore());
                                                student.setTotal_score(newMark.getTotal_score());
                                                studentAdapter.notifyDataSetChanged();

                                                isUpdate = true;
                                                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                                titleBarView.setRightDrawable(R.drawable.ic_export_excel_gray);

                                                alertDialog.dismiss();
                                            }
                                        });
                                        break;
                                    }
                                }

                                if(!flag) {
                                    //语音识别stu_id没有一个相同，这里处理

                                    //先根据学号stu_id查出姓名stu_name和stu_gender
                                    SQLiteDatabase db = MyDatabaseHelper.getInstance(ShowAndEditActivity.this);
                                    Cursor cursor = db.query("Student", new String[]{"stu_name", "stu_gender"}, "stu_id = ?",
                                            new String[]{""+newMark.getStu_id()+""}, null, null, null);
                                    String stu_name = null;
                                    String stu_gender = null;
                                    while (cursor.moveToNext()) {
                                        stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                                        stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                                    }
                                    cursor.close();

                                    Student student = new Student();
                                    student.setStu_id(Integer.parseInt(newMark.getStu_id()));

                                    student.setStu_name(stu_name);
                                    student.setStu_gender(stu_gender);

                                    student.setScore(newMark.getScore());
                                    student.setTotal_score(newMark.getTotal_score());

                                    studentList.add(student);
                                    studentAdapter.notifyDataSetChanged();

                                    isUpdate = true;
                                    titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                    titleBarView.setRightDrawable(R.drawable.ic_export_excel_gray);


//                                    Toast.makeText(ShowAndEditActivity.this, "没有你想修改的学生id", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        //清空错误缓存
                        mIatResults.clear();
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                /**
                 * 过滤掉没有说话的错误码显示
                 */
                TextView tv_error = mIatDialog.getWindow().getDecorView().findViewWithTag("errtxt");
                if (tv_error != null) {
                    tv_error.setText("您好像没有说话哦。。。");
                }            }
        };
        // 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5db04b35");

//        ImageView fab = findViewById(R.id.fab);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIatDialog = new RecognizerDialog(ShowAndEditActivity.this, mInitListener);

                mIatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");

                mIatDialog.setParameter("dwa", "wpgs");

                mIatDialog.setListener(mRecognizerDialogListener);

                mIatDialog.show();

                TextView txt = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
                txt.setText(R.string.tip);
                txt.setTextColor(Color.WHITE);
                txt.getPaint().setFlags(Paint.SUBPIXEL_TEXT_FLAG);

                TextView title = mIatDialog.getWindow().getDecorView().findViewWithTag("title");
                title.setTextColor(Color.WHITE);

//                //跳转到语音识别录成绩界面
//                Intent intent = new Intent(ShowAndEditActivity.this, RecordMarkActivity.class);
//                startActivity(intent);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "缺少文件读写权限，可能会造成无法分享文件", Toast.LENGTH_SHORT).show();
            }
        }
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
        Log.i("ShowAndEditActivity", "开始初始化Student。。。。。。");
        String sqlSelect="SELECT StudentMark.stu_id,Student.stu_name,Student.stu_gender,StudentMark.test_id,StudentTest.test_name,StudentMark.score,StudentMark.total_score "
                + "FROM StudentMark INNER JOIN Student ON StudentMark.stu_id = Student.stu_id "
                + "INNER JOIN StudentTest ON StudentMark.test_id = StudentTest.test_id";
        //扫描数据库，将信息放入markList
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(ShowAndEditActivity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});

        //打印cursor中的行数
        Log.i("ShowAndEditActivity", "cursor.getCount()："+cursor.getCount());

        while(cursor.moveToNext()){
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            Log.i("ShowAndEditActivity", "数据库test_id："+test_id);

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
                backUpStudentList.add(student);
            }
        }
        cursor.close();
    }

    //读取动态修正返回结果
    private String updateResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d(ShowAndEditActivity.this.getLocalClassName(), "parseIatResult： " + text);

        String sn = null;
        String pgs = null;
        String rg = null;

        //读取json中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
            pgs = resultJson.optString("pgs");
            rg = resultJson.optString("rg");
            Log.d(this.getLocalClassName(), "sn：" + sn + "\r\npgs：" + pgs + "\r\nrg：" + rg);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
        if(pgs.equals("rpl")) {
            Log.d(this.getLocalClassName(), "recognizer result replace： " + results.getResultString());

            String[] strings = rg.replace("[", "").replace("]", "").split(",");
            int begin = Integer.parseInt(strings[0]);
            int end = Integer.parseInt(strings[1]);
            for (int i=begin; i<=end; i++) {
                mIatResults.remove(i+"");
                Log.d(this.getClass().getName(), "修正mIatResults：" + mIatResults.toString());
            }
        }

        mIatResults.put(sn, text);
        Log.d(this.getClass().getName(), "mIatResults：" + mIatResults.toString());
        StringBuffer resultBuffer = new StringBuffer();

        for(String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
    }

    public static void actionStart(Context context, Long test_id, String test_name) {
        Intent intent = new Intent(context, ShowAndEditActivity.class);
        intent.putExtra("test_id", test_id);
        intent.putExtra("test_name",test_name);
        context.startActivity(intent);
    }


    /**
     * 导出Excel
     * @param
     */
    private void exportSheet() {
        file = new File(getSDPath() + "/Record");
        makeDir(file);
//        ExcelUtils.initExcel(file.toString() + "/成绩表.xls", title);
//        fileName = getSDPath() + "/Record/成绩表.xls";

        ExcelUtils.initExcel(file.toString() + "/" + test_name + "成绩表.xls", title);//初始化表第一行
        fileName = getSDPath() + "/Record/" + test_name + "成绩表.xls";

        ExcelUtils.writeObjListToExcel(getRecordData(), fileName, ShowAndEditActivity.this);//将ObjList写入Excel
    }


    /**
     * 将数据集合 转化为ArrayList<ArrayList<String>>
     * @return ArrayList<ArrayList<ArrayList>>
     */
    private ArrayList<ArrayList<String>> getRecordData() {
        recordList = new ArrayList<>();
        for(int i=0; i<studentList.size(); i++) {
            Student student = studentList.get(i);
            ArrayList<String> beanList = new ArrayList<>();
            beanList.add(String.valueOf(student.getStu_id()));
            beanList.add(student.getStu_name());
            beanList.add(student.getStu_gender());
            beanList.add(student.getScore());
            beanList.add(String.valueOf(student.getTotal_score()));
            recordList.add(beanList);
        }
        return recordList;
    }

    /**
     * 获取sd卡路径
     * @return
     */
    private String getSDPath() {
        File sdDir = null;
        //getExternalStorageState(),返回File 获取外部内存当前状态
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在，MEDIA_MOUNTED，返回getExternalStorageState() ，表明对象是否存在并具有读/写权限
        if(sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
        }

        return sdDir.toString();
    }

    private void makeDir(File dir) {
        if(!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    //获取点击事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if(isHideInput(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //判断是否需要隐藏
    private boolean isHideInput(View v, MotionEvent ev) {
        if(v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);

            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();

            if(ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //隐藏软键盘
    private void hideSoftInput(IBinder token) {
        if(token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
