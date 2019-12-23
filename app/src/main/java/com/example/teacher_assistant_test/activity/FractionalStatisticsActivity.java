package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.DateUtils;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.util.TouchEmptyCloseKeyBoardUtils;


import java.text.DecimalFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FractionalStatisticsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "FSActivity";

    @BindView(R.id.title)
    TitleBarView titleBarView;

    @BindView(R.id.tv_test_time) TextView tv_test_time;
    @BindView(R.id.tv_average_score) TextView tv_average_score;

    @BindView(R.id.tv_excellent_score_line_title) TextView tv_excellent_score_line_title;
    @BindView(R.id.et_excellent_score_line) EditText et_excellent_score_line;
    @BindView(R.id.tv_excellent_3) TextView tv_excellent_3;

    @BindView(R.id.et_full_mark) EditText et_full_mark;
    @BindView(R.id.tv_full_score_3) TextView tv_full_score_3;

    @BindView(R.id.et_11) EditText et_11;
    @BindView(R.id.et_12) EditText et_12;
    @BindView(R.id.tv_13) TextView tv_13;

    @BindView(R.id.et_21) EditText et_21;
    @BindView(R.id.et_22) EditText et_22;
    @BindView(R.id.tv_23) TextView tv_23;

    @BindView(R.id.et_31) EditText et_31;
    @BindView(R.id.et_32) EditText et_32;
    @BindView(R.id.tv_33) TextView tv_33;

    @BindView(R.id.et_41) EditText et_41;
    @BindView(R.id.et_42) EditText et_42;
    @BindView(R.id.tv_43) TextView tv_43;

    @BindView(R.id.et_51) EditText et_51;
    @BindView(R.id.et_52) EditText et_52;
    @BindView(R.id.tv_53) TextView tv_53;

    @BindView(R.id.tv_63) TextView tv_63;

    @BindView(R.id.btn_copy_info) Button btn_copy_info;
    @BindView(R.id.btn_refresh) Button btn_refresh;


    private long test_id;
    private String test_name;
    private int test_full_mark;
    private String test_time;
    private int test_type;

    private int count = 0;
    private int[] scores = new int[42];





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fractional_statistics);

        //在Activity中绑定View
        ButterKnife.bind(this);

        btn_copy_info.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏

        ImmersiveStatusBar.setImmersiveStatusBar(this);

        test_id = getIntent().getLongExtra("test_id", 0);



        initTestInfo();

        initScoreInfo();

        initTitleBar();

        initUI();

        //设置测试时间TextView监听
        tv_test_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
//                tv_test_time.setText(DateUtils.date2String(calendar.getTime(), DateUtils.YMD_FORMAT));
                DatePickerDialog dialog = new DatePickerDialog(FractionalStatisticsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d(TAG, "onDateSet: year: " + year + ", month: " + month + ", dayOfMonth: " + dayOfMonth);

                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                tv_test_time.setText(DateUtils.date2String(calendar.getTime(), DateUtils.YMD_FORMAT));
                                updateTestTimeByTestId(test_id, tv_test_time.getText().toString());
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });


        //给EditText添加监听
        addListener(et_excellent_score_line);
        addListener(et_full_mark);

        addListener(et_11);
        addListener(et_12);

        addListener(et_21);
        addListener(et_22);

        addListener(et_31);
        addListener(et_32);

        addListener(et_41);
        addListener(et_42);

        addListener(et_51);
        addListener(et_52);
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        titleBarView.setTitle(test_name);
        titleBarView.setTitleSize(20);
        titleBarView.setLeftDrawable(R.drawable.ic_back);
        titleBarView.setLeftText(getString(R.string.back));
        titleBarView.setLeftTextColor(Color.parseColor("#FFFFFF"));
        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void tvRightClick() {
                //暂不处理
            }

            @Override
            public void ivRightClick(View view) {
                //暂不处理
            }
        });
    }

    /**
     * 初始化该测验的基本信息
     */
    private void initTestInfo() {

        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        String sql = "select test_name,test_full_mark,test_type,test_time "
                + "from StudentTest where test_id = " + test_id;

        Cursor cursor = db.rawQuery(sql, new String[]{});

        if (cursor.moveToNext()) {
            test_name = cursor.getString(cursor.getColumnIndex("test_name"));
            test_full_mark = cursor.getInt(cursor.getColumnIndex("test_full_mark"));
            test_type = cursor.getInt(cursor.getColumnIndex("test_type"));
            test_time = cursor.getString(cursor.getColumnIndex("test_time"));
        }

        cursor.close();
        db.close();

    }

    /**
     * 初始化指定测试id的所有总成绩信息
     */
    private void initScoreInfo() {

        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        String sql = "select total_score from StudentMark where test_id = " + test_id;

        Cursor cursor = db.rawQuery(sql, new String[]{});

        while (cursor.moveToNext()) {
            int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));

            scores[count++] = total_score;
        }

        cursor.close();
        db.close();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        //测试时间
        tv_test_time.setText(test_time);

        //平均分
        tv_average_score.setText(String.format("%s分", getAverageScore()));


        //优秀分数线、人数
        if (!TextUtils.isEmpty(et_excellent_score_line.getText().toString())) {
            tv_excellent_3.setText(String.valueOf(getNumByRange(Integer.parseInt(et_excellent_score_line.getText().toString()),
                    test_full_mark+1) + "人"));
        }


        //满分、人数
        et_full_mark.setText(String.valueOf(test_full_mark));
        tv_full_score_3.setText(String.valueOf(getNumByRange(test_full_mark, test_full_mark+1) + "人"));

        //1
        tv_13.setText(String.format("%s人", getNumByRange(et_11, et_12)));
        //2
        tv_23.setText(String.format("%s人", getNumByRange(et_21, et_22)));
        //
        tv_33.setText(String.format("%s人", getNumByRange(et_31, et_32)));
        //4
        tv_43.setText(String.format("%s人", getNumByRange(et_41, et_42)));
        //5
        tv_53.setText(String.format("%s人", getNumByRange(et_51, et_52)));

        //未参试或0分    暂时只统计0分的人数
        tv_63.setText(String.valueOf(getNumByRange(0, 1) + "人"));
    }

    /**
     * 设置监听
     * @param et 传入一个EditText
     */
    private void addListener(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //EditText在addTextChangedListener添加的TextWatcher中
                // 如果在afterTextChanged方法中又重新调用了setText,
                // 那么会重复触发对此方法的递归死循环调用, 产生ANR
                //在执行逻辑前先remove掉EditText绑定的监听器，等逻辑执行完毕后再绑定该监听器.
                et.removeTextChangedListener(this);
                et.setText(s.toString());
                et.setSelection(s.length());
                et.addTextChangedListener(this);
            }
        });
    }

    /**
     * 获取平均分
     * @return 返回平均分
     */
    private String getAverageScore() {
        int total = 0;
        for (int score : scores) {
            total += score;
        }
        return new DecimalFormat("0.00").format((double) total / count);
    }

    /**
     * 获取指定成绩范围的学生人数
     * @param low 含最低分数
     * @param high 不含最高分数
     * @return 返回人数
     */
    private int getNumByRange(int low, int high) {
        int num = 0;

        if (low > high) {
            return num;
        }

        for (int score : scores) {
            if (score >= low && score < high) {
                num++;
            }
        }
        return num;
    }

    /**
     * 获取指定成绩范围的学生人数字符串，增加了检查空字符串""的操作
     * @param lowEdit 含最低分数EditText
     * @param highEdit 不含最高分数EditText
     * @return 返回人数字符串
     */
    private String getNumByRange(EditText lowEdit, EditText highEdit) {

        int low = lowEdit.getText().toString().equals("") ? 0 :Integer.parseInt(lowEdit.getText().toString());
        int high = highEdit.getText().toString().equals("") ? 0 : Integer.parseInt(highEdit.getText().toString());

        int num = 0;

        if (low > high) {
            return num + "";
        }

        for (int score : scores) {
            if (score >= low && score < high) {
                num++;
            }
        }
        return num + "";
    }

    /**
     * 实现接口监听
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy_info:
                copyInfo();
                break;

            case R.id.btn_refresh:
                //更新满分
                if (!TextUtils.isEmpty(et_full_mark.getText().toString())) {
                    test_full_mark = Integer.parseInt(et_full_mark.getText().toString());
                    updateTestFullMarkByTestId(test_id, test_full_mark);
                }

                initUI();
                break;
            default:
                break;
        }
    }

    /**
     * 复制信息button 逻辑
     */
    private void copyInfo() {

        String s = test_name + " 成绩反馈"
                + "\r\n"
                + "测试时间：[" + test_time + "]"
                + "\r\n" + "\r\n"
                + "满分共[" + tv_full_score_3.getText().toString().replace("人", "") + "]人："
                + "\r\n"
                + getFullScoreStudent(Integer.parseInt(tv_full_score_3.getText().toString().replace("人", "")))
                + "\r\n" + "\r\n"
                + "非满分优秀共[" + tv_excellent_3.getText().toString().replace("人", "") + "]人："
                + "\r\n"
                + getExcellentStudent(Integer.parseInt(tv_excellent_3.getText().toString().replace("人", "")))
                + "\r\n" + "\r\n"
                + "班级均分：" + tv_average_score.getText().toString()
                + "\r\n" + "\r\n"
                + "分数段情况："
                + "\r\n"
                + et_full_mark.getText().toString() + " 共：" + tv_full_score_3.getText().toString()
                + "\r\n"
                + et_11.getText().toString() + "--" + et_12.getText().toString() + " 共：" + tv_13.getText().toString() + "（不含" + et_12.getText().toString() + "）"
                + "\r\n"
                + et_21.getText().toString() + "--" + et_22.getText().toString() + " 共：" + tv_23.getText().toString() + "（不含" + et_22.getText().toString() + "）"
                + "\r\n"
                + et_31.getText().toString() + "--" + et_32.getText().toString() + " 共：" + tv_33.getText().toString() + "（不含" + et_32.getText().toString() + "）"
                + "\r\n"
                + et_41.getText().toString() + "--" + et_42.getText().toString() + " 共：" + tv_43.getText().toString() + "（不含" + et_42.getText().toString() + "）"
                + "\r\n"
                + et_51.getText().toString() + "--" + et_52.getText().toString() + " 共：" + tv_53.getText().toString() + "（不含" + et_52.getText().toString() + "）";

        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", s);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);

        Toast.makeText(this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取满分学生字符串
     * @param num 满分人数
     * @return 字符串
     */
    private String getFullScoreStudent(int num) {
        if (num <= 0) return "";

        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        String sql = "select stu_name from Student "
                + "inner join StudentMark on Student.stu_id = StudentMark.stu_id "
                + "where test_id = " + test_id
                + " and total_score = " + test_full_mark;

        Cursor cursor = db.rawQuery(sql, new String[]{});

        StringBuilder stringBuilder = new StringBuilder();

        int i = 0;

        while (cursor.moveToNext()) {
            i++;

            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));

            stringBuilder.append(stu_name);

            if (i == num) break;

            if (i % 5 == 0) {
                stringBuilder.append("\r\n");
            } else {
                stringBuilder.append(" ");
            }
        }

        cursor.close();
        db.close();

        return stringBuilder.toString();
    }

    /**
     * 获取优秀学生字符串
     * @param num 优秀学生数
     * @return 字符串
     */
    private String getExcellentStudent(int num) {
        if (num <= 0) return "";

        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        String sql = "select stu_name from Student inner join StudentMark "
                + "on Student.stu_id = StudentMark.stu_id "
                + "where test_id = " + test_id
                + " and total_score >= " + et_excellent_score_line.getText().toString();

        Cursor cursor = db.rawQuery(sql, new String[]{});

        int i = 0;

        StringBuilder stringBuilder = new StringBuilder();

        while (cursor.moveToNext()) {
            i++;

            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));

            stringBuilder.append(stu_name);

            if (i == num) break;

            if (i % 5 == 0) {
                stringBuilder.append("\r\n");
            } else {
                stringBuilder.append(" ");
            }
        }

        cursor.close();
        db.close();

        return stringBuilder.toString();
    }

    /**
     * 更新测试时间test_time通过测试号test_id
     * @param test_id
     * @param test_time
     */
    private void updateTestTimeByTestId(long test_id, String test_time) {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        ContentValues values = new ContentValues();

        values.put("test_time", test_time);

        db.update("StudentTest", values, "test_id = ?", new String[]{"" + test_id + ""});
    }

    /**
     * 更新总分test_full_mark通过测试号test_id
     * @param test_id 测试号
     * @param test_full_mark 总分
     */
    private void updateTestFullMarkByTestId(long test_id, int test_full_mark) {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        ContentValues values = new ContentValues();

        values.put("test_full_mark", test_full_mark);

        db.update("StudentTest", values, "test_id = ?", new String[] {"" + test_id + ""});
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        //用于点击非EditText区域收起软键盘和清除光标
        new TouchEmptyCloseKeyBoardUtils().autoClose(this, ev);

        return super.dispatchTouchEvent(ev);
    }

    /**
     * 传进来一个测试id，则可根据测试id查出对应test_time，test_full_score，连表查询出测试
     * @param context 上下文
     */
    public static void actionStart(Context context, long test_id) {
        Intent intent = new Intent(context, FractionalStatisticsActivity.class);
        intent.putExtra("test_id", test_id);
        context.startActivity(intent);
    }
}
