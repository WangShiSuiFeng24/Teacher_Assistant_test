package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.bean.Record;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.StudentInfoAdapter;
import com.example.teacher_assistant_test.bean.StudentInfo;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EditStudentInfoActivity extends AppCompatActivity {

    private List<StudentInfo> studentInfoList = new ArrayList<>();
    //备份，即使当前页面被编辑后，backUpStudentInfoList始终保存刚进入页面时的数据
    private List<StudentInfo> backUpStudentInfoList = new ArrayList<>();

    private TitleBarView titleBarView;

    private StudentInfoAdapter studentInfoAdapter;

    private FloatingActionMenu fab;

    private boolean isDataChanged = false;


    private LinearLayout student_info_title;
    private ImageView check_box;

    private Button save_to_db;
    private Button insert_to_list;

    //recordUI中编辑时弹出的"bottom_dialog"
    private LinearLayout my_collection_bottom_dialog;

    private TextView selectNum;
    private TextView selectAll;
    private Button btnDelete;

    //设置编辑模式默认为RECORD_MODE_CHECK
    private static final int RECORD_MODE_CHECK = 0;
    private static final int RECORD_MODE_EDIT = 1;
    private int editMode = RECORD_MODE_CHECK;

    //默认全选状态为false,编辑状态为false，选中数量为0
    private boolean isSelectAll = false;
    private boolean editorStatus = false;
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_info);

        ImmersiveStatusBar.setImmersiveStatusBar(EditStudentInfoActivity.this);

        initUI();

        initStudentInfo();

        titleBarView = findViewById(R.id.title4);
        titleBarView.setTitleSize(20);
        titleBarView.setTitle(getString(R.string.stu_info));
        titleBarView.setRightTextColor(Color.WHITE);

        if (studentInfoList.size() == 0) {
            titleBarView.setRightTextColor(Color.parseColor("#b7b8bd"));
        }

        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {

                if (isDataChanged) {
                    final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(EditStudentInfoActivity.this, getString(R.string.save),
                            getString(R.string.stu_info_UI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            save_to_db.performClick();

                            if (!isDataChanged) {
                                finish();
                            }

                            alertDialog.dismiss();

                        }
                    });

                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            finish();

                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            alertDialog.dismiss();
                        }
                    });
                } else {

                    finish();
                }
            }

            @Override
            public void tvRightClick() {
//                //判断数据是否更新，若未更新，则设置点击无效，不处理
//                //否则，保存更新数据到数据库
//                if(isDataChanged) {
//                    saveDataToDataBase();
//                }

                if (studentInfoList.size() != 0) {
                    updateEditMode();
                }

            }

            @Override
            public void ivRightClick(View view) {
                //无图片，不处理
            }
        });

        //设置recordRecyclerView的空View
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyMessage = findViewById(R.id.empty_message);
        emptyMessage.setText(R.string.stu_info_UI_empty_message);

        RecyclerViewEmptySupport recyclerView = findViewById(R.id.Recycler_View_StudentInfo);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setEmptyView(emptyView);

        studentInfoAdapter = new StudentInfoAdapter(studentInfoList);
        recyclerView.setAdapter(studentInfoAdapter);

        studentInfoAdapter.setOnItemClickListener(new StudentInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                studentInfoAdapterOnItemClick(position);
            }
        });

//        fab = findViewById(R.id.fab);
//        fab.setClosedOnTouchOutside(true);//可以设置点击蒙版关闭的开关

//        FloatingActionButton fab_insert = findViewById(R.id.fab_insert);
//        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
//        FloatingActionButton fab_update = findViewById(R.id.fab_update);
//
//        fab_insert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fab.close(true);
//                showInsertDialog();
//            }
//        });
//
//        fab_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fab.close(true);
//                showDeleteDialog();
//            }
//        });
//
//        fab_update.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fab.close(true);
//                showUpdateDialog();
//            }
//        });



        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断数据是否更新，若未更新，则设置点击无效，不处理
                //否则，保存更新数据到数据库
                if(isDataChanged) {
                    saveDataToDataBase();
                }
            }
        });

        //增加按钮逻辑
        insert_to_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInsertDialog();
            }
        });

        //全选按钮逻辑
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAllItem();
            }
        });

        //删除按钮逻辑
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecordItem();
            }
        });



    }

    private void initUI() {
        student_info_title = findViewById(R.id.student_info_title);
        check_box = findViewById(R.id.check_box);

        save_to_db = findViewById(R.id.save_to_db);
        setSaveBtnBackground(isDataChanged);
        insert_to_list = findViewById(R.id.insert_to_list);

        //绑定recordUI中编辑时弹出的"bottom_dialog"
        my_collection_bottom_dialog = findViewById(R.id.my_collection_bottom_dialog);

        selectNum = findViewById(R.id.tv_select_num);
        selectAll = findViewById(R.id.select_all);
        btnDelete = findViewById(R.id.btn_delete);
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

            StudentInfo backUpStudentInfo = new StudentInfo(stu_id, stu_name, stu_gender);

            studentInfoList.add(studentInfo);
            backUpStudentInfoList.add(backUpStudentInfo);
        }
        cursor.close();
    }

    private void showInsertDialog() {
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        final EditText edit_stu_id = new EditText(this);
//        edit_stu_id.setHint(R.string.insert_stu_id_hint);
//        layout.addView(edit_stu_id);
//
//        final EditText edit_stu_name = new EditText(this);
//        edit_stu_name.setHint(R.string.insert_stu_name_hint);
//        layout.addView(edit_stu_name);


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


//        final EditText edit_stu_gender = new EditText(this);
//        edit_stu_gender.setHint(R.string.insert_stu_gender_hint);
//        layout.addView(edit_stu_gender);


        View view = LayoutInflater.from(this).inflate(R.layout.edit_stu_id_name_gender, null, false);

        EditText edit_stu_id = view.findViewById(R.id.stu_id_edit);
        EditText edit_stu_name = view.findViewById(R.id.stu_name_edit);
        EditText edit_stu_gender = view.findViewById(R.id.stu_gender_edit);

        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, getString(R.string.insert_one_stu_info),
                null, view, getString(R.string.confirm), getString(R.string.cancel));
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
                 * 还没有检查长度限制等
                 */

                if(canParseInt(edit_stu_id.getText().toString().trim())) {
                    //检查studentInfoList中是否有input这个学号
                    boolean hasSameId = false;
                    for(int i=0; i<studentInfoList.size(); i++) {
                        int stu_id = studentInfoList.get(i).getStu_id();
                        if(stu_id == Integer.parseInt(edit_stu_id.getText().toString().trim())) {
                            hasSameId = true;
                            Toast.makeText(EditStudentInfoActivity.this, getString(R.string.insert_same_stu_id_hint, edit_stu_id.getText().toString().trim()), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EditStudentInfoActivity.this, R.string.insert_empty_hint, Toast.LENGTH_SHORT).show();
                        } else {
                            if(isGenderLegal(stu_gender)) {
                                StudentInfo studentInfo = new StudentInfo(stu_id, stu_name, stu_gender);
                                studentInfoList.add(studentInfo);
                                studentInfoAdapter.notifyDataSetChanged();
                                isDataChanged = true;

                                setSaveBtnBackground(true);

                                student_info_title.setVisibility(View.VISIBLE);

                                titleBarView.setRightTextColor(Color.parseColor("#FFFFFF"));
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(EditStudentInfoActivity.this, R.string.insert_illegal_stu_gender_hint, Toast.LENGTH_SHORT).show();
                                edit_stu_gender.requestFocus();
                            }
                        }
                    }
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, getString(R.string.insert_illegal_stu_id_hint, edit_stu_id.getText().toString().trim()), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EditStudentInfoActivity.this, "学号为：" + stu_id + "的学生信息已删除", Toast.LENGTH_SHORT).show();
                            hasInput = true;
                            break;
                        }
                    }

                    if(!hasInput) {
                        Toast.makeText(EditStudentInfoActivity.this, "没有学号为：" + Integer.parseInt(input) + "的学生信息", Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, "您输入的学号不合法，请重新输入！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUpdateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edit_stu_id = new EditText(this);
        edit_stu_id.setHint("学号：请输入阿拉伯数字");
        layout.addView(edit_stu_id);

        final EditText edit_stu_name = new EditText(this);
        edit_stu_name.setHint("姓名：");
        layout.addView(edit_stu_name);

        final EditText edit_stu_gender = new EditText(this);
        edit_stu_gender.setHint("性别：请输入 \"男\" 或 \"女\"");
        layout.addView(edit_stu_gender);

        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, "请输入需要修改信息的学生学号及修改结果",
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
                 * 还没有检查长度限制等
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
                                    Toast.makeText(EditStudentInfoActivity.this, "修改学号为：" + stu_id + " 的学生信息成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EditStudentInfoActivity.this, "性别非\"男\" 或 \"女\"", Toast.LENGTH_SHORT).show();
                                    edit_stu_gender.requestFocus();
                                }

                            }
                        }
                    }

                    if(!hasSame) {
                        Toast.makeText(EditStudentInfoActivity.this, "没有您想修改学号为：" + edit_stu_id.getText().toString().trim() + " 的学生信息", Toast.LENGTH_SHORT).show();
                        edit_stu_id.requestFocus();
                    }
                } else {
                    Toast.makeText(EditStudentInfoActivity.this, "学号："+edit_stu_id.getText().toString().trim()+" 非法", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    /**
     * recordAdapter点击事件
     * @param position 当前item位置
     */
    private void studentInfoAdapterOnItemClick(int position) {
        if (editorStatus) {
            StudentInfo studentInfo = studentInfoList.get(position);
            boolean isSelect = studentInfo.isSelect();
            if (!isSelect) {
                index++;
                studentInfo.setSelect(true);
                if (index == studentInfoList.size()) {
                    isSelectAll = true;
                    selectAll.setText(R.string.cancel_select_all);
                }
            } else {
                    index--;
                    studentInfo.setSelect(false);
                    isSelectAll = false;
                    selectAll.setText(R.string.select_all);
            }
            setDeleteBtnBackground(index);
            selectNum.setText(String.valueOf(index));
            studentInfoAdapter.notifyDataSetChanged();
        }
    }

    private void saveDataToDataBase() {
        //更新相同学号的姓名stu_name和性别stu_gender
        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        //先删除数据库同backupStudentInfoList中学号的学生，再将studentInfoList直接插入数据库
        for (int i=0; i < backUpStudentInfoList.size(); i++) {
            int delete_stu_id = backUpStudentInfoList.get(i).getStu_id();
            db.delete("Student", "stu_id = ?", new String[]{"" + delete_stu_id});
        }//先删
        ContentValues values = new ContentValues();
        for (StudentInfo studentInfo : studentInfoList) {
            int stu_id = studentInfo.getStu_id();
            String stu_name = studentInfo.getStu_name();
            String stu_gender = studentInfo.getStu_gender();

            values.clear();
            values.put("stu_id", stu_id);
            values.put("stu_name", stu_name);
            values.put("stu_gender", stu_gender);

            db.insert("Student", null, values);
        }
        db.close();
        isDataChanged = false;
        setSaveBtnBackground(false);
        Toast.makeText(EditStudentInfoActivity.this, R.string.save_successfully, Toast.LENGTH_SHORT).show();


//        ContentValues values = new ContentValues();
//        Iterator<StudentInfo> infoIterator = studentInfoList.iterator();
//        while(infoIterator.hasNext()) {
//            StudentInfo studentInfo = infoIterator.next();
//            int stu_id = studentInfo.getStu_id();
//            values.put("stu_name", studentInfo.getStu_name());
//            values.put("stu_gender", studentInfo.getStu_gender());
//
//            db.update("Student", values, "stu_id = ?", new String[]{""+stu_id+""});
//        }
//
//        //插入不相同学号的item
//        for(StudentInfo studentInfo : studentInfoList) {
//            int stu_id = studentInfo.getStu_id();
//            Cursor cursor = db.query("Student", new String[] {"stu_id"}, "stu_id = ?",
//                    new String[]{""+stu_id+""}, null, null, null);
//
//            if(cursor.getCount() == 0) {
//                values.clear();
//                values.put("stu_id", studentInfo.getStu_id());
//                values.put("stu_name", studentInfo.getStu_name());
//                values.put("stu_gender", studentInfo.getStu_gender());
//
//                db.insert("Student", null, values);
//            } else {
//                Log.d("EditStudentInfoActivity", "StudentMark表中已存在学号：" + stu_id);
//            }
//            cursor.close();
//        }
//        db.close();
//
//        Toast.makeText(EditStudentInfoActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//        finish();
    }

    /**
     * 按back键时根据各种状态自定义返回效果
     */
    @Override
    public void onBackPressed() {
        if (editorStatus) {
            editorStatus = false;
            updateEditMode();
            my_collection_bottom_dialog.setVisibility(View.GONE);

            save_to_db.setVisibility(View.VISIBLE);
            setSaveBtnBackground(isDataChanged);
            insert_to_list.setVisibility(View.VISIBLE);

            return;
        } else {

            if (isDataChanged) {

                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(EditStudentInfoActivity.this, getString(R.string.save),
                        getString(R.string.stu_info_UI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        save_to_db.performClick();

                        if (!isDataChanged) {
                            finish();
                        }

                        alertDialog.dismiss();

                    }
                });

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        finish();

                        alertDialog.dismiss();
                    }
                });

                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog.dismiss();
                    }
                });

            } else {

                super.onBackPressed();
            }
        }
    }

    //使用正则表达式判断该字符串是否为数字，第一个\是转义符，\d+表示匹配1个或 //多个连续数字，"+"和"*"类似，"*"表示0个或多个
    private boolean canParseInt(String string){
        if(string == null){ //验证是否为空
            return false;
        }
        return string.matches("\\d+");
    }

    private boolean isGenderLegal(String stu_gender) {
        return stu_gender.equals(getString(R.string.boy)) || stu_gender.equals(getString(R.string.girl));
    }


    private void setSaveBtnBackground(boolean isDataChanged) {
        if (isDataChanged) {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            save_to_db.setEnabled(true);
            save_to_db.setTextColor(Color.WHITE);
        } else {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
            save_to_db.setEnabled(false);
            save_to_db.setTextColor(ContextCompat.getColor(this, R.color.color_b7b8bd));
        }
    }

//    /**
//     * 实现接口监听
//     * @param v view
//     */
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.btn_delete:
//                deleteRecordItem();
//                break;
//            case R.id.select_all:
//                selectAllItem();
//                break;
//            default:
//                break;
//        }
//    }

    /**
     * 删除按钮逻辑
     */
    private void deleteRecordItem() {
        if (index == 0) {
            btnDelete.setEnabled(false);
            return;
        }
        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, getString(R.string.alarm),
                getString(R.string.delete_one_item_hint), null,
                getString(R.string.confirm), getString(R.string.cancel));

        if (index == 1) {
            alertDialog.setMessage(getString(R.string.delete_one_item_hint));
        } else {
            alertDialog.setMessage(getString(R.string.delete_multiple_item_hint, index));
        }

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = studentInfoList.size(), j = 0; i > j; i--) {
                    StudentInfo studentInfo = studentInfoList.get(i-1);
                    if (studentInfo.isSelect()) {
                        studentInfoList.remove(studentInfo);
                        index--;
                    }
                }

                //删 更新
                isDataChanged = true;
                setSaveBtnBackground(true);

                index = 0;
                selectNum.setText(String.valueOf(0));
                setDeleteBtnBackground(index);
                if (studentInfoList.size() == 0) {
                    updateEditMode();

                    my_collection_bottom_dialog.setVisibility(View.GONE);

                    save_to_db.setVisibility(View.VISIBLE);

                    insert_to_list.setVisibility(View.VISIBLE);

                    student_info_title.setVisibility(View.GONE);

                    titleBarView.setRightTextColor(Color.parseColor("#b7b8bd"));
                }
                studentInfoAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

    }

    /**
     * 全选和反选按钮逻辑
     */
    private void selectAllItem() {
        if(studentInfoAdapter == null ) return;
        if(!isSelectAll) {
            for (int i = 0, j = studentInfoList.size(); i < j; i++) {
                studentInfoList.get(i).setSelect(true);
            }
            index = studentInfoList.size();
            btnDelete.setEnabled(true);
            selectAll.setText(R.string.cancel_select_all);
            isSelectAll = true;
        } else {
            for (int i=0, j = studentInfoList.size(); i < j; i++) {
                studentInfoList.get(i).setSelect(false);
            }
            index = 0;
            btnDelete.setEnabled(false);
            selectAll.setText(R.string.select_all);
            isSelectAll = false;
        }
        studentInfoAdapter.notifyDataSetChanged();
        setDeleteBtnBackground(index);
        selectNum.setText(String.valueOf(index));
    }

    /**
     * 编辑和取消按钮互转
     */
    private void updateEditMode() {
        editMode = editMode == RECORD_MODE_CHECK ? RECORD_MODE_EDIT : RECORD_MODE_CHECK;
        if (editMode == RECORD_MODE_EDIT) {
            titleBarView.setRightText(getString(R.string.cancel));
            my_collection_bottom_dialog.setVisibility(View.VISIBLE);

            check_box.setVisibility(View.VISIBLE);

            save_to_db.setVisibility(View.GONE);
            insert_to_list.setVisibility(View.GONE);

            editorStatus = true;
        } else {
            titleBarView.setRightText(getString(R.string.edit));
            my_collection_bottom_dialog.setVisibility(View.GONE);

            check_box.setVisibility(View.GONE);

            save_to_db.setVisibility(View.VISIBLE);
            insert_to_list.setVisibility(View.VISIBLE);

            editorStatus = false;
            clearAll();
        }
        studentInfoAdapter.setEditMode(editMode);
    }

    /**
     * 恢复（初始）默认设置
     */
    private void clearAll() {
        selectNum.setText(String.valueOf(0));
        isSelectAll = false;
        selectAll.setText(R.string.select_all);
        setDeleteBtnBackground(0);
    }

    /**
     * 根据选择的数量是否为0来判断按钮的是否可点击
     * @param size 选择的数量
     */
    private void setDeleteBtnBackground(int size) {
        if(size != 0) {
            btnDelete.setBackgroundResource(R.drawable.button_shape);
            btnDelete.setEnabled(true);
            btnDelete.setTextColor(Color.WHITE);
        } else {
            btnDelete.setBackgroundResource(R.drawable.button_unclickable_shape);
            btnDelete.setEnabled(false);
            btnDelete.setTextColor(ContextCompat.getColor(this, R.color.color_b7b8bd));
        }
    }
}
