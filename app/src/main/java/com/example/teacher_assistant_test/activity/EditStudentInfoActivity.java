package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.StudentInfoAdapter;
import com.example.teacher_assistant_test.bean.StudentInfo;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EditStudentInfoActivity extends AppCompatActivity {

    private List<StudentInfo> studentInfoList = new ArrayList<>();

    private TitleBarView titleBarView;

    private RecyclerView recyclerView;
    private StudentInfoAdapter studentInfoAdapter;

    private FloatingActionMenu fab;
    private FloatingActionButton fab_insert;
    private FloatingActionButton fab_delete;
    private FloatingActionButton fab_update;

    private boolean isDataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_info);

        ImmersiveStatusBar.setImmersiveStatusBar(EditStudentInfoActivity.this);

        titleBarView = findViewById(R.id.title4);
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

        fab = findViewById(R.id.fab);
        fab.setClosedOnTouchOutside(true);//可以设置点击蒙版关闭的开关

        fab_insert = findViewById(R.id.fab_insert);
        fab_delete = findViewById(R.id.fab_delete);
        fab_update = findViewById(R.id.fab_update);

        fab_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                showInsertDialog();
            }
        });

        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                showDeleteDialog();
            }
        });

        fab_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                showUpdateDialog();
            }
        });





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

    private void showInsertDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edit_stu_id = new EditText(this);
        edit_stu_id.setHint("学号:请输入阿拉伯数字");
        layout.addView(edit_stu_id);

        final EditText edit_stu_name = new EditText(this);
        edit_stu_name.setHint("姓名");
        layout.addView(edit_stu_name);


//        final RadioGroup gender = new RadioGroup(this);
//        gender.setOrientation(LinearLayout.HORIZONTAL);
//
//        final RadioButton maleRadioButton = new RadioButton(this);
//        maleRadioButton.setText("男");
//        gender.addView(maleRadioButton);
//
//        final RadioButton femaleRadioButton = new RadioButton(this);
//        femaleRadioButton.setText("女");
//        gender.addView(femaleRadioButton);
//
//        layout.addView(gender);


        final EditText edit_stu_gender = new EditText(this);
        edit_stu_gender.setHint("性别:请输入 \"男\" 或 \"女\"");
        layout.addView(edit_stu_gender);

        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, "插入一条学生信息",
                null, layout, "确定", "取消");
        alertDialog.setCanceledOnTouchOutside(false);

        edit_stu_id.setFocusable(true);
        edit_stu_id.setFocusableInTouchMode(true);
        edit_stu_id.requestFocus();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) edit_stu_id.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(edit_stu_id, 0);
            }
        }, 300);

//        final String stu_gender;
//        int count = gender.getChildCount();
//        for(int i = 0 ;i < count;i++){
//            RadioButton rb = (RadioButton)gender.getChildAt(i);
//            if(rb.isChecked()){
//                stu_gender = rb.getTag().toString();
//                break;
//            }
//        }

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 还没有检查性别 男、女，长度限制等。。。
                 */

                if(canParseInt(edit_stu_id.getText().toString().trim())) {
                    //检查studentInfoList中是否有input这个学号
                    boolean hasSameId = false;
                    for(int i=0; i<studentInfoList.size(); i++) {
                        int stu_id = studentInfoList.get(i).getStu_id();
                        if(stu_id == Integer.parseInt(edit_stu_id.getText().toString().trim())) {
                            hasSameId = true;
                            Toast.makeText(EditStudentInfoActivity.this, "存在相同学号:"+edit_stu_id.getText().toString().trim() + " 请重新输入学号!", Toast.LENGTH_SHORT).show();
                            edit_stu_id.requestFocus();
                            break;
                        }
                    }

                    if(!hasSameId) {
                        int stu_id = Integer.parseInt(edit_stu_id.getText().toString().trim());
                        String stu_name = edit_stu_name.getText().toString().trim();
//                     String stu_gender = (maleRadioButton != null ? maleRadioButton.getText() : femaleRadioButton.getText()).toString();

                        String stu_gender = edit_stu_gender.getText().toString().trim();

                        if(TextUtils.isEmpty(stu_name) || TextUtils.isEmpty(stu_gender)) {
                            Toast.makeText(EditStudentInfoActivity.this, "姓名或性别栏为空", Toast.LENGTH_SHORT).show();
                        } else {
                            if(isGenderLegal(stu_gender)) {
                                StudentInfo studentInfo = new StudentInfo(stu_id, stu_name, stu_gender);
                                studentInfoList.add(studentInfo);
                                studentInfoAdapter.notifyDataSetChanged();
                                isDataChanged = true;
                                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(EditStudentInfoActivity.this, "性别非\"男\" 或 \"女\"", Toast.LENGTH_SHORT).show();
                                edit_stu_gender.requestFocus();
                            }
                        }
                    }
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, "学号:"+edit_stu_id.getText().toString().trim()+" 非法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteDialog() {
        final EditText edit = new EditText(this);
        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, "请输入要删除的学生的学号", null, edit,
                "确定", "取消");

        edit.setFocusable(true);
        edit.setFocusableInTouchMode(true);
        edit.requestFocus();

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(edit, 0);
            }
        }, 300);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edit.getText().toString().trim();
                if(canParseInt(input)) {
                    //检查studentInfoList中是否有input这个学号
                    boolean hasInput = false;
                    for(int i=0; i<studentInfoList.size(); i++) {
                        int stu_id = studentInfoList.get(i).getStu_id();
                        if(stu_id == Integer.parseInt(input)) {
                            studentInfoList.remove(i);
                            studentInfoAdapter.notifyDataSetChanged();
                            isDataChanged = true;
                            titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                            Toast.makeText(EditStudentInfoActivity.this, "学号为:" + stu_id + "的同学信息已删除", Toast.LENGTH_SHORT).show();
                            hasInput = true;
                            break;
                        }
                    }

                    if(!hasInput) {
                        Toast.makeText(EditStudentInfoActivity.this, "没有学号为:" + Integer.parseInt(input) + "的同学信息", Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, "您输入的学号不合法,请重新输入!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUpdateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edit_stu_id = new EditText(this);
        edit_stu_id.setHint("学号:请输入阿拉伯数字");
        layout.addView(edit_stu_id);

        final EditText edit_stu_name = new EditText(this);
        edit_stu_name.setHint("姓名");
        layout.addView(edit_stu_name);

        final EditText edit_stu_gender = new EditText(this);
        edit_stu_gender.setHint("性别:请输入 \"男\" 或 \"女\"");
        layout.addView(edit_stu_gender);

        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, "请输入需要修改信息的同学学号及修改结果",
                null, layout, "确定", "取消");
        alertDialog.setCanceledOnTouchOutside(false);

        edit_stu_id.setFocusable(true);
        edit_stu_id.setFocusableInTouchMode(true);
        edit_stu_id.requestFocus();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) edit_stu_id.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(edit_stu_id, 0);
            }
        }, 300);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 还没有检查性别 男、女，长度限制等。。。
                 */

                if(canParseInt(edit_stu_id.getText().toString().trim())) {
                    //检查studentInfoList中是否有input这个学号
                    boolean hasSame = false;
                    for(int i=0; i<studentInfoList.size(); i++) {
                        int stu_id = studentInfoList.get(i).getStu_id();
                        if(stu_id == Integer.parseInt(edit_stu_id.getText().toString().trim())) {
                            hasSame = true;
                            //修改操作
                            String stu_name = edit_stu_name.getText().toString().trim();
                            String stu_gender = edit_stu_gender.getText().toString().trim();

                            if(TextUtils.isEmpty(stu_name) || TextUtils.isEmpty(stu_gender)) {
                                Toast.makeText(EditStudentInfoActivity.this, "姓名或性别栏为空", Toast.LENGTH_SHORT).show();
                            } else {
                                if(isGenderLegal(stu_gender)) {
                                    studentInfoList.get(i).setStu_name(stu_name);
                                    studentInfoList.get(i).setStu_gender(stu_gender);

                                    studentInfoAdapter.notifyDataSetChanged();
                                    isDataChanged = true;
                                    titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                    alertDialog.dismiss();
                                    Toast.makeText(EditStudentInfoActivity.this, "修改学号为:" + stu_id + " 的同学信息成功!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EditStudentInfoActivity.this, "性别非\"男\" 或 \"女\"", Toast.LENGTH_SHORT).show();
                                    edit_stu_gender.requestFocus();
                                }

                            }
                        }
                    }

                    if(!hasSame) {
                        Toast.makeText(EditStudentInfoActivity.this, "没有您想修改学号为:" + edit_stu_id.getText().toString().trim() + " 的学生信息", Toast.LENGTH_SHORT).show();
                        edit_stu_id.requestFocus();
                    }
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, "学号:"+edit_stu_id.getText().toString().trim()+" 非法", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    //使用正则表达式判断该字符串是否为数字，第一个\是转义符，\d+表示匹配1个或 //多个连续数字，"+"和"*"类似，"*"表示0个或多个
    private boolean canParseInt(String string){
        if(string == null){ //验证是否为空
            return false;
        }
        return string.matches("\\d+");
    }

    private boolean isGenderLegal(String stu_gender) {
        if(stu_gender.equals("男") || stu_gender.equals("女")) {
            return true;
        }
        return false;
    }
}
