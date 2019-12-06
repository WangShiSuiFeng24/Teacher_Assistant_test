package com.example.teacher_assistant_test.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.adapter.RecordAdapter;
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Record;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.ExportSheet;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.MyDividerItemDecoration;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.StrProcess;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.TestAdapter;
import com.example.teacher_assistant_test.bean.Test;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
import com.example.teacher_assistant_test.util.TouchEmptyCloseKeyBoardUtils;
import com.github.clans.fab.FloatingActionButton;
import com.github.jokar.floatmenu.FloatMenu;
import com.github.jokar.floatmenu.OnMenuItemClickListener;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 2;

    //使用SharedPreferences来记录程序的使用次数
    private SharedPreferences preferences;
    private boolean isShowGender;

    //标题栏
    private TitleBarView titleBarView;

    //在recordUI则为true,初始为false(不在)
    private boolean inRecordUI = false;

    //打开一个test则为true，初始为false
    private boolean isOpenATest = false;

    //当前点击test_id
    private long current_test_id = -1;

    //当前点击test_name
    private String current_test_name;

    //testUI
    //recordUI中Visibility随需求改变的控件
    private LinearLayout test_recycle;
    private LinearLayout test_fab;

    //testUI Visibility 不随需求改变的控件
    private List<Test> testList = new ArrayList<>();
    private TestAdapter testAdapter;
    private RecyclerView testRecyclerView;
    private DividerItemDecoration testDividerItemDecoration;

    //仿微信长按popupWindow
    private Point point = new Point();
    private FloatMenu floatMenu;


    //recordUI
    //recordUI中Visibility随需求改变的控件

    //统计信息编辑总分按钮,编辑保存状态flag
    private int flag = 0;

    private View include;

    private LinearLayout record_title;
    private TextView stu_gender;
    private ImageView check_box;

    private Button save_to_db;
    private Button share_by_excel;

    //recordUI中编辑时弹出的"bottom_dialog"
    private LinearLayout my_collection_bottom_dialog;

    private TextView selectNum;
    private TextView selectAll;
    private Button btnDelete;

    //recordUI中子控件
    private boolean isIdSortPressed = false;
    private ImageView id_sort;
    private boolean isScoreSortPressed = false;
    private ImageView score_sort;

    //recordUI Visibility 不随需求改变的控件
    private RecyclerViewEmptySupport recordRecyclerView;
    private FloatingActionButton record_fab;

    private MyDividerItemDecoration recordDividerItemDecoration;

    private List<Record> recordList = new ArrayList<>();
    //备份，即使当前页面被编辑后，students始终保存刚进入页面时的数据
    private List<Record> backUpRecordList = new ArrayList<>();
    private RecordAdapter recordAdapter;

    //设置recordList更新默认为false
    private boolean isRecordListUpdate = false;

    //讯飞语音识别
    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;

    //设置编辑模式默认为RECORD_MODE_CHECK
    private static final int RECORD_MODE_CHECK = 0;
    private static final int RECORD_MODE_EDIT = 1;
    private int editMode = RECORD_MODE_CHECK;

    //默认全选状态为false,编辑状态为false，选中数量为0
    private boolean isSelectAll = false;
    private boolean editorStatus = false;
    private int index = 0;


//    private static final int COMPLETED = 0;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            if(msg.what == COMPLETED) {
//                testUI_to_recordUI();
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏

        setContentView(R.layout.activity_main);

        //设置沉浸式状态栏
        ImmersiveStatusBar.setImmersiveStatusBar(this);

        //初始化标题栏
        titleBarView = findViewById(R.id.title);
        initTitleBar();

        //使用SharedPreferences保存一个状态
        //定义一个变量count来判断程序是第几次运行，如果是第一次导入assets文件夹中的Excel数据到数据库
        //如果不是第一次则不导入
        preferences = getSharedPreferences("count", MODE_PRIVATE);
        int count = preferences.getInt("count", 0);
        isShowGender = preferences.getBoolean("isShowGender", false);

        //判断程序是第几次运行，如果是第一次则导入assets文件夹中的Excel数据到数据库
        if (count == 0) {
            importSheet();
        }

        SharedPreferences.Editor editor = preferences.edit();
        //存入数据
        editor.putInt("count", ++count);
        //提交修改
        editor.apply();


        //初始化RecyclerView
        testRecyclerView = findViewById(R.id.Recycler_View_Test);
        initTestRecyclerView();

        //请求权限
        request_permissions(this);


        //设置testAdapter的点击和长按事件
        testAdapter.setOnItemClickListener(new TestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //testAdapter点击事件
                testAdapterOnItemClick(position);
            }

            @Override
            public void onItemLongClick(int position, View view) {
                //testAdapter长按事件
                testAdapterOnItemLongClick(position, view);
            }
        });

        //testUI中fab的点击事件
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //先判断权限是否授予，没有则单独申请，有则跳转
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                    request_permissions(MainActivity.this);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    //由testUI界面变为RecordUI界面
                    testUI_to_recordUI();
                }

            }
        });


        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(MainActivity.this.getLocalClassName(), "SpeechRecognizer init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(MainActivity.this, getString(R.string.xunfei_record_init_fail_hint, code), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRecognizerDialogListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(this.getClass().getName(), "recognizer json result：" + results.getResultString());

                //处理讯飞语音听写返回的结果，将处理结果存入recordList,并更新adapter和相关控件的显示隐藏
                updateRecordListByRecognizer(results, isLast);


            }

            /**
             * 过滤掉没有说话的错误码显示
             */
            @Override
            public void onError(SpeechError error) {
                TextView tv_error = mIatDialog.getWindow().getDecorView().findViewWithTag("errtxt");
                if (tv_error != null) {
                    tv_error.setText(R.string.not_speak);
                }

//                View view = mIatDialog.getWindow().getDecorView().findViewWithTag("errview");
//                view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//                Toast.makeText(RecordMarkActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        };

        // 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5db04b35");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.lack_record_permission_hint, Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.lack_write_storage_permission_hint, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 返回刷新，小技巧是把需要更新的view 可以都放在initView（）中，在resume中调用这个方法即可。
     */
    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume()");
        super.onResume();
        //先清空testList
        testList.clear();
        //再重新add数据到testList
        initTestList();

        testRecyclerView.removeItemDecoration(testDividerItemDecoration);
        if(testList.size() != 0 ) {
            //添加Android自带的分割线
            testRecyclerView.addItemDecoration(testDividerItemDecoration);
        }
        //返回时重新设置thisPosition
        testAdapter.setThisPosition(-1);
        //通知RecyclerView，告诉它Adapter的数据发生了变化
        testAdapter.notifyDataSetChanged();

        isShowGender = preferences.getBoolean("isShowGender", false);
    }

    /**
     * 初始化testList数据
     */
    private void initTestList() {
        String sqlSelect = "SELECT StudentTest.test_id,StudentTest.test_name,StudentTest.test_full_mark FROM StudentTest";
        SQLiteDatabase database = MyDatabaseHelper.getInstance(MainActivity.this);
        Cursor cursor = database.rawQuery(sqlSelect, new String[]{});
        while(cursor.moveToNext()) {
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

            int test_full_mark = cursor.getInt(cursor.getColumnIndex("test_full_mark"));

            Test test = new Test(test_id, test_name, test_full_mark);
            testList.add(test);
        }
        cursor.close();
    }

    /**
     *点击testItem,初始化recordList
     */
    private void initRecordList() {
        //先清空，在添加
        recordList.clear();
        backUpRecordList.clear();

        Log.i("ShowAndEditActivity", "开始初始化Student。。。。。。");
        String sqlSelect="SELECT StudentMark.stu_id,Student.stu_name,Student.stu_gender,StudentMark.test_id,StudentTest.test_name,StudentMark.score,StudentMark.total_score,StudentMark.isCorrect "
                + "FROM StudentMark INNER JOIN Student ON StudentMark.stu_id = Student.stu_id "
                + "INNER JOIN StudentTest ON StudentMark.test_id = StudentTest.test_id";
        //扫描数据库，将信息放入markList
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(MainActivity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});

        //打印cursor中的行数
        Log.i("ShowAndEditActivity", "cursor.getCount()："+cursor.getCount());

        while(cursor.moveToNext()){
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            Log.i("ShowAndEditActivity", "数据库test_id："+test_id);

            //只有当查询出的条目的test_id等于传入的this.test_id时才将该条目add到List<Student>
            if(test_id == current_test_id) {
                String stu_id = String.valueOf(cursor.getInt(cursor.getColumnIndex("stu_id")));
                String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));
                String score = cursor.getString(cursor.getColumnIndex("score"));
                int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));

                //读取SQLite studentMark表订正列
                boolean isCorrect = ((cursor.getInt(cursor.getColumnIndex("isCorrect")) == 1) ? true : false);

                Record record = new Record(stu_id, stu_name, stu_gender, test_name, score, total_score, isCorrect);

                //此处非常关键，若add同一个Record，则改变recordList中Record对象的成员方法或变量时，
                // backUpRecordList中的对应的Record对象的成员方法或变量也会随之改变
                //因为两个List中的Record在添加的时候就是同一个Record（地址一样）
                Record backUpRecord = new Record(stu_id, stu_name, stu_gender, test_name, score, total_score, isCorrect);

                recordList.add(record);
                backUpRecordList.add(backUpRecord);


//                Student student = new Student(stu_id, stu_name, stu_gender, test_name, score, total_score);
//                studentList.add(student);
//                backUpStudentList.add(student);
            }
        }
        cursor.close();
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        titleBarView.setTitleSize(20);
        titleBarView.setTitle(getString(R.string.app_name));
        titleBarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                //进入学生基本信息页面
                if (!inRecordUI) {
                    EditStudentInfoActivity.actionStart(MainActivity.this);
                }

                //由recordUI返回testUI
                if (inRecordUI) {

                    if (isRecordListUpdate) {

                        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this, getString(R.string.save),
                                getString(R.string.recordUI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                save_to_db.performClick();

                                if (!isRecordListUpdate) {
                                    recordUI_to_testUI();
                                }

                                alertDialog.dismiss();

                            }
                        });

                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                recordUI_to_testUI();

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

                        recordUI_to_testUI();
                    }
                }
            }

            @Override
            public void tvRightClick() {
                if (inRecordUI && recordList.size() != 0) {
                    updateEditMode();
                }
            }

            @Override
            public void ivRightClick(View view) {
                //弹出PopupMenu
//                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
//                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
//                popupMenu.show();
                SettingActivity.actionStart(MainActivity.this);
            }
        });
    }

    /**
     * 导入assets文件夹中的Excel数据到数据库，id_name_info.xls
     */
    private void importSheet() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
        ContentValues values = new ContentValues();
        try {
            InputStream is = getResources().getAssets().open(getString(R.string.import_excel_file_name));

            Workbook workbook = Workbook.getWorkbook(is);

            Sheet sheet = workbook.getSheet(0);

            for (int j=1; j < sheet.getRows(); j++) {

                values.put("stu_id", sheet.getCell(0, j).getContents());
                values.put("stu_name", sheet.getCell(1, j).getContents());
                values.put("stu_gender", sheet.getCell(3, j).getContents());
                db.insert("Student", null, values);
                values.clear();
            }
            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化TestRecyclerView
     */
    private void initTestRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);

        testRecyclerView.setLayoutManager(linearLayoutManager);

        testAdapter = new TestAdapter(testList);

        testRecyclerView.setAdapter(testAdapter);

        testDividerItemDecoration = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
    }

    /**
     * 请求权限
     * @param context 传入一个context
     */
    private void request_permissions(Context context) {
        //创建一个权限列表，把需要使用而没有授权的权限存放在这里
        List<String> permissionList = new ArrayList<>();

        //判断权限是否已经授予，没有就把该权限添加到列表中
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if(!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1002);
        }
    }

    /**
     * testAdapter单击事件
     * @param position 当前点击位置
     */
    private void testAdapterOnItemClick(final int position) {
        //拿适配器调用适配器内部自定义好的setThisPosition方法（参数写点击事件的参数的position）
        testAdapter.setThisPosition(position);
        testAdapter.notifyDataSetChanged();

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                //单击则查询
//                Test test = testList.get(position);
//                ShowAndEditActivity.actionStart(MainActivity.this, test.getTest_id(), test.getTest_name());
//            }
//        },100);
        //设置打开recordUI方式isOpenATest为true
        isOpenATest = true;
        //设置当前点击的test_id以及test_name
        current_test_id = testList.get(position).getTest_id();
        current_test_name = testList.get(position).getTest_name();


        //Android系统中的视图组件并不是线程安全的，如果要更新视图，必须在主线程中更新，不可以在子线程中执行更新的操作。
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //处理比较耗时的操作
//                //处理完成后给handler发送消息
//                Message msg = new Message();
//                msg.what = COMPLETED;
//                handler.sendMessage(msg);
//            }
//        }).start();

        //运行在主线程中
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                testUI_to_recordUI();
            }
        }, 300);
    }

    /**
     * testAdapter长按事件
     * @param position 当前点击位置
     * @param view 当前点击的itemView
     */
    private void testAdapterOnItemLongClick(final int position, View view) {
        //拿适配器调用适配器内部自定义好的setThisPosition方法（参数写点击事件的参数的position）
        testAdapter.setThisPosition(position);
        testAdapter.notifyDataSetChanged();

        floatMenu = new FloatMenu(MainActivity.this);

        floatMenu.inflate(R.menu.menu_chat);

        floatMenu.setMOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(MenuItem menuItem) {
                //这里设置Menu的item点击事件
                showDeleteDialog(position);
            }
        });

        floatMenu.show(view, point.x, point.y);

        floatMenu.setFocusable(true);
        floatMenu.setOutsideTouchable(false);

        floatMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                floatMenu.dismiss();
                onResume();
            }
        });
    }

    /**
     * 由testUI界面变为recordUI界面
     */
    private void testUI_to_recordUI() {

        //设置标题栏右图片隐藏
        titleBarView.setRightDrawable(0);

        titleBarView.setLeftDrawable(R.drawable.ic_back);
        titleBarView.setLeftText(getString(R.string.back));
        titleBarView.setLeftTextColor(Color.WHITE);

        //绑定testUI Visibility随需求改变的控件
        test_recycle = findViewById(R.id.test_recycle);
        test_fab = findViewById(R.id.test_fab);


        //如果isOpenATest为true，则为点击testRecyclerView的item进入，于是初始化recordList
        if (isOpenATest) {
            initRecordList();
            titleBarView.setTitle(current_test_name);
        }

        //初始化recordUI
        initRecordUI();

        //隐藏testUI
        test_recycle.setVisibility(View.GONE);
        test_fab.setVisibility(View.GONE);

        //显示recordUI
        include.setVisibility(View.VISIBLE);

        record_title.setVisibility(View.GONE);
        check_box.setVisibility(View.GONE);

        save_to_db.setVisibility(View.GONE);
        share_by_excel.setVisibility(View.GONE);


        if (recordList.size() != 0) {
            titleBarView.setRightText(getString(R.string.edit));
            titleBarView.setRightTextColor(Color.WHITE);

            record_title.setVisibility(View.VISIBLE);

            save_to_db.setVisibility(View.VISIBLE);
            share_by_excel.setVisibility(View.VISIBLE);
        }

        //初始化recordUI的监听器
        initListener();

        //inRecordUI设置为true，表明现在recordUI
        inRecordUI = true;

        //判断打开recordUI方式，若非点击testItem打开，则执行record_fab.performClick()
        if (!isOpenATest) {
            recordList.clear();
            backUpRecordList.clear();
            record_fab.performClick();
        }

        recordAdapter.setIsShowGender(isShowGender);
        if (isShowGender) {
            stu_gender.setVisibility(View.VISIBLE);
        } else {
            stu_gender.setVisibility(View.GONE);
        }
    }

    /**
     * 由recordUI界面变为testUI界面
     */
    private void recordUI_to_testUI() {
        //隐藏recordUI
        save_to_db.setVisibility(View.VISIBLE);
        share_by_excel.setVisibility(View.VISIBLE);
        record_fab.setVisibility(View.VISIBLE);

        include.setVisibility(View.GONE);

        my_collection_bottom_dialog.setVisibility(View.GONE);


        /*
         * 恢复recordUI默认设置
         */

        selectNum.setText(String.valueOf(0));
        selectAll.setText(R.string.select_all);
        setDeleteBtnBackground(0);

        isIdSortPressed = false;
        isScoreSortPressed = false;

        //返回设置inRecordUI
        inRecordUI = false;
        //返回设置isOpenATest
        isOpenATest = false;

        //当前点击test_id
        current_test_id = -1;
        //当前点击test_name
        current_test_name = "";

        //返回设置移除recordRecyclerView分割线
        recordRecyclerView.removeItemDecoration(recordDividerItemDecoration);

        //返回设置清空recordList,backUpRecordList
        recordList.clear();
        backUpRecordList.clear();

        //返回设置recordList更新默认为false
        isRecordListUpdate = false;

        setSaveBtnBackground(false);

        //返回时重新设置thisPosition
        testAdapter.setThisPosition(-1);
        //通知RecyclerView，告诉它Adapter的数据发生了变化
        testAdapter.notifyDataSetChanged();


        //返回设置editorStatus
        editMode = RECORD_MODE_CHECK;

        //返回设置isSelectAll
        isSelectAll = false;
        //返回设置editorStatus
        editorStatus = false;

        //返回设置index
        index = 0;



        //显示testUI
        test_recycle.setVisibility(View.VISIBLE);
        test_fab.setVisibility(View.VISIBLE);


        //恢复标题栏设置
        titleBarView.setLeftDrawable(0);
        titleBarView.setLeftText("");

        titleBarView.setTitle(getString(R.string.app_name));

        titleBarView.setRightText("");
        titleBarView.setRightDrawable(R.drawable.ic_set_up);
        titleBarView.setLeftDrawable(R.drawable.ic_student_info);


        //返回设置先清空testList
        testList.clear();
        //再重新add数据到testList
        initTestList();

        if (testList.size() != 0) {
            testRecyclerView.removeItemDecoration(testDividerItemDecoration);
            testRecyclerView.addItemDecoration(testDividerItemDecoration);
        }
    }

    /**
     * 初始化recordUI
     */
    private void initRecordUI() {

        //绑定recordUI Visibility随需求改变的控件
        include = findViewById(R.id.record_mark);

        record_title = findViewById(R.id.record_title);
        stu_gender = findViewById(R.id.stu_gender);
        check_box = findViewById(R.id.check_box);

        save_to_db = findViewById(R.id.save_to_db);
        setSaveBtnBackground(isRecordListUpdate);

        share_by_excel = findViewById(R.id.share_by_excel);

        //绑定recordUI中编辑时弹出的"bottom_dialog"
        my_collection_bottom_dialog = findViewById(R.id.my_collection_bottom_dialog);

        selectNum = findViewById(R.id.tv_select_num);
        selectAll = findViewById(R.id.select_all);
        btnDelete = findViewById(R.id.btn_delete);

        //绑定子控件
        id_sort = findViewById(R.id.id_sort);
        score_sort = findViewById(R.id.score_sort);

        //绑定recordUI Visibility 不随需求改变的控件
        recordRecyclerView = findViewById(R.id.Recycler_View_Record);
        record_fab = findViewById(R.id.record_fab);

        //设置recordRecyclerView的空View
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyMessage = findViewById(R.id.empty_message);
        emptyMessage.setText(R.string.recordUI_empty_message);

        //设置分割线
        recordDividerItemDecoration = new MyDividerItemDecoration(this, MyDividerItemDecoration.VERTICAL, false);
        recordRecyclerView.addItemDecoration(recordDividerItemDecoration);

        LinearLayoutManager record_linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recordRecyclerView.setLayoutManager(record_linearLayoutManager);
        recordRecyclerView.setEmptyView(emptyView);
        recordAdapter = new RecordAdapter(recordList);
        recordRecyclerView.setAdapter(recordAdapter);

        //设置recordUI控件监听
        setRecordUIWidgetListener();
    }

    /**
     * 设置recordUI控件监听
     */
    private void setRecordUIWidgetListener() {
        //recordUI中点击和长按点击事件
        recordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //recordAdapter点击事件
                recordAdapterOnItemClick(position);
            }

            @Override
            public void onItemLongClick(int position) {
                //暂不处理
            }
        });

        //recordUI中学号id编辑框监听事件
        recordAdapter.setOnStuIdFillListener(new RecordAdapter.OnStuIdFillListener() {
            @Override
            public void onStuIdFill(int position, String stu_id) {
                //编辑学号监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {
                    recordList.get(position).setStu_id(stu_id);

                    //改 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);
                }
            }
        });

        //recordUI中成绩score编辑框监听事件
        recordAdapter.setOnScoreFillListener(new RecordAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(final int position, String score) {
                //编辑成绩监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {

                    //改 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);

                    if(!TextUtils.isEmpty(score)) {
                        recordList.get(position).setScore(score);
                        //先要判断编辑的score字符串是否符合规则，是则计算total_score,否则不计算
                        //增加判断score中是否存在空格，若存在则不计算，不存在则计算total_score，否则会发生异常
                        if(!score.contains(" ") && new CheckExpression().checkExpression(score)) {
//                        score = score.replaceAll(" ","");
                            int total_score = (int) new Calculator().calculate(score);//score中有空格会发生异常
                            recordList.get(position).setTotal_score(total_score);

                            if(!recordRecyclerView.isComputingLayout()) {
                                recordAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }
        });

        recordAdapter.setOnStarClickListener(new RecordAdapter.OnStarClickListener() {
            @Override
            public void onStarClick(int position) {
                recordAdapterOnStarClick(position);
            }
        });

        recordAdapter.setOnFooterClickListener(new RecordAdapter.OnFooterClickListener() {
            @Override
            public void onFooterClick(int position) {
                recordAdapterOnFooterClick(position);
            }
        });


        //recordUI中的fab点击事件
        record_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecognizerDialog();
            }
        });

        //保存当前页面数据到数据库
        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveDataToDatabase();
            }
        });

        //分享当前页面
        share_by_excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareExcel();
            }
        });

    }

    /**
     * recordAdapter点击事件
     * @param position 当前item位置
     */
    private void recordAdapterOnItemClick(int position) {
        if (editorStatus) {
            Record record = recordList.get(position);
            boolean isSelect = record.isSelect();
            if (!isSelect) {
                index++;
                record.setSelect(true);
                if (index == recordList.size()) {
                    isSelectAll = true;
                    selectAll.setText(R.string.cancel_select_all);
                }
            } else {
                index--;
                record.setSelect(false);
                isSelectAll = false;
                selectAll.setText(R.string.select_all);
            }
            setDeleteBtnBackground(index);
            selectNum.setText(String.valueOf(index));
            recordAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 订正列点亮事件
     * @param position 点击位置
     */
    private void recordAdapterOnStarClick(int position) {
        Record record = recordList.get(position);
        boolean isCorrect = record.isCorrect();
        if (!isCorrect) {
            record.setCorrect(true);
        } else {
            record.setCorrect(false);
        }
        setSaveBtnBackground(true);
        recordAdapter.notifyDataSetChanged();
    }

    /**
     *  每个成绩表页脚增加一个“统计信息”入口，点击后打开统计信息表，内容为：
     *     - 总分：xxx（分值要可以编辑）
     *     - 成绩尚缺：（列出姓名）
     *     - 订正尚缺：（列出姓名）
     * @param position 点击位置
     */
    private void recordAdapterOnFooterClick(int position) {

        View view = LayoutInflater.from(this).inflate(R.layout.statistical_info_dialog, null, false);

        EditText test_full_mark_edit = view.findViewById(R.id.test_full_mark_edit);
        test_full_mark_edit.setEnabled(false);

        Button edit_button = view.findViewById(R.id.edit_button);

        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (flag) {
                    case 0:
                        test_full_mark_edit.setEnabled(true);

                        test_full_mark_edit.setFocusable(true);
                        test_full_mark_edit.setFocusableInTouchMode(true);
                        test_full_mark_edit.requestFocus();

                        //如果是已经入某个界面就要立刻弹出输入键盘，可能会由于界面未加载完成而无法弹出，需要适当延迟，比如延迟500毫秒：
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask()
                        {
                            public void run()
                            {
                                InputMethodManager inputManager =(InputMethodManager)test_full_mark_edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.showSoftInput(test_full_mark_edit, 0);
                            }
                        },300);

                        test_full_mark_edit.setSelection(test_full_mark_edit.getText().length());

                        edit_button.setText(R.string.save);
                        flag = 1;
                        break;
                    case 1:
                        String input = test_full_mark_edit.getText().toString();

                        if (TextUtils.isEmpty(input)) break;

                        int test_full_mark = Integer.parseInt(input);
                        updateTestFullMarkByTestId(current_test_id, test_full_mark);

                        test_full_mark_edit.setEnabled(false);
                        edit_button.setText(R.string.edit);
                        flag = 0;
                        break;
                    default:
                        break;
                }

            }
        });

        TextView lack_score_name = view.findViewById(R.id.lack_score_name);
        TextView lack_correct_name = view.findViewById(R.id.lack_correct_name);

        AlertDialog alertDialog = GetAlertDialog.getAlertDialog(this, "统计信息",
                null, view, getString(R.string.confirm), null);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //恢复统计信息编辑总分按钮,编辑保存状态flag默认值0
                flag = 0;
                alertDialog.dismiss();
            }
        });


        SQLiteDatabase db = MyDatabaseHelper.getInstance(this);

        //根据current_test_id查询对应test_full_mark
        String sqlSelectTestFullMark = "select StudentTest.test_full_mark "
                + "from StudentTest where test_id = " + current_test_id;

        Cursor cursor0 = db.rawQuery(sqlSelectTestFullMark, new String[]{});

        while (cursor0.moveToNext()) {
            int test_full_mark = cursor0.getInt(cursor0.getColumnIndex("test_full_mark"));
            test_full_mark_edit.setText(String.valueOf(test_full_mark));
        }
        cursor0.close();


        //先查询Student表中所有学生的学号，对比去掉当前页面的学号，返回剩下学号对应的姓名
        String sqlSelectStuIdAndStuName = "select Student.stu_id, Student.stu_name "
                + "from Student";

        Cursor cursor = db.rawQuery(sqlSelectStuIdAndStuName, new String[]{});

        SparseArray<String> sparseArray = new SparseArray<>();

        while (cursor.moveToNext()) {
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));

            sparseArray.put(stu_id, stu_name);
        }
        cursor.close();

        for (int i=sparseArray.size()-1; i>=0; i--) {
            for (int j=0; j<recordList.size(); j++) {
                Record record = recordList.get(j);
                if (sparseArray.keyAt(i) == Integer.parseInt(record.getStu_id())) {
                    sparseArray.removeAt(i);
                    break;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<sparseArray.size(); i++) {
            String name = sparseArray.valueAt(i);
            stringBuilder.append(name);
            if (i != sparseArray.size()-1) {
                stringBuilder.append("、");
            }
        }

        lack_score_name.setText(stringBuilder.toString());

        //订正尚缺则只显示当前页面isCorrect为false的学生姓名
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int i=0; i<recordList.size(); i++) {
            Record record = recordList.get(i);
            if (!record.isCorrect()) {
                stringBuilder1.append(record.getStu_name());
                if (i != recordList.size()-1) {
                    stringBuilder1.append("、");
                }
            }
        }

        lack_correct_name.setText(stringBuilder1.toString());

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

    /**
     * 显示讯飞语音听写的dialog，并设置相关参数
     */
    private void showRecognizerDialog() {
        //初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        //使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);

//                if(mIatDialog!=null)
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");
        mIatDialog.setParameter("dwa", "wpgs");

        mIatDialog.setListener(mRecognizerDialogListener);

        mIatDialog.show();
        TextView txt = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText(R.string.tip);//更改内容
        txt.setTextColor(Color.WHITE);
        txt.getPaint().setFlags(Paint.SUBPIXEL_TEXT_FLAG);//去下划线

        TextView title = mIatDialog.getWindow().getDecorView().findViewWithTag("title");
        title.setTextColor(Color.WHITE);
    }

    /**
     * 保存当前页面数据到数据库
     */
    private void saveDataToDatabase() {

        final SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);

        if (checkRecordListLegality()) {

            //通过testItem点击进入recordUI
            if(isOpenATest) {
                //先删除数据库同backupRecordList中学号的学生，再将recordList直接插入数据库
                for (int i = 0; i < backUpRecordList.size(); i++) {
                    String delete_stu_id = backUpRecordList.get(i).getStu_id();
                    db.delete("StudentMark", "stu_id = ? AND test_id = ?", new String[]{"" + delete_stu_id + "", ""+current_test_id+""});
                }//先删
                for (Record record : recordList) {
                    String stu_id = record.getStu_id();

                    String score = record.getScore();
                    int total_score = record.getTotal_score();

                    boolean isCorrect = record.isCorrect();

                    new IDUSTool(MainActivity.this).insertStuMarkDB(stu_id, current_test_id, score, total_score, isCorrect);
                }//再插入
                db.close();
                Toast.makeText(MainActivity.this, R.string.save_successfully, Toast.LENGTH_SHORT).show();

                isRecordListUpdate = false;
                setSaveBtnBackground(false);

                return;
            }

            //非点击testItem，新建模式的保存方式
            final EditText edit = new EditText(MainActivity.this);
            //设置EditText的可视最大行数。
            edit.setMaxLines(1);
            //先弹出一个可编辑的AlertDialog，可以编辑test_name
            final AlertDialog alertDialog = GetAlertDialog
                    .getAlertDialog(MainActivity.this,getString(R.string.please_enter_the_test_name),
                            null, edit, getString(R.string.confirm), getString(R.string.cancel));
            //给edit设置焦点
            edit.setFocusable(true);
            edit.setFocusableInTouchMode(true);
            edit.requestFocus();
            //如果是已经入某个界面就要立刻弹出输入键盘，可能会由于界面未加载完成而无法弹出，需要适当延迟，比如延迟500毫秒：
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                public void run()
                {
                    InputMethodManager inputManager =(InputMethodManager)edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(edit, 0);
                }
            },300);

            //拿到按钮并判断是否是POSITIVE_BUTTON，然后我们自己实现监听
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = edit.getText().toString().trim();
                    //生成唯一id
//                                final long unique_test_id = new Date().getTime();//太长
                    //每次先查询StudentTest表长getCount，将unique_test_id设置为表长+1
//                            final long unique_test_id = getCount() + 1;

                    final long unique_test_id = getTest_idMax() + 1;

                    Log.i("RecordMarkActivity", "unique_test_id:"+unique_test_id);
                    if (input.equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.test_name_cannot_be_empty) + input, Toast.LENGTH_SHORT).show();
                        return;
                    } else {
//                            String editText = edit.getText().toString().trim();

                        //将用户输入的Test_Name,unique_test_id和当前页面数据一起保存到数据库中

                        //查询数据库中是否存在与将要插入的preMark相同的test_name,若相同，则提示用户test_name已存在重新输入，否则直接新建插入全部数据
                        String sqlSelect = "SELECT test_name FROM StudentTest";
                        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

                        if(cursor.isLast()) {
                            //StudentTest表为空，第一次更新
                            new IDUSTool(MainActivity.this).insertStuTest(unique_test_id, input);
                            Toast.makeText(MainActivity.this, R.string.save_successfully, Toast.LENGTH_SHORT).show();
                        }

                        else {
                            //StudentTest表不为空
//                                //flag初始设为false,代表StudentTest表中有没相同的test_name
//                                boolean flag = false;
                            while(cursor.moveToNext()) {
                                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

                                if(input.equals(test_name)) {
                                    //StudentTest表中已有相同test_name,提示用户重新输入
                                    Toast.makeText(MainActivity.this, input+" 已存在，请重新输入", Toast.LENGTH_SHORT).show();
//                                        flag = true;
                                    return;
                                }
                            }

                            //循环检查完毕，此时没有相同的test_name,直接向StudentTest表中插入所有数据，不用判断

                            //先把test_id和test_name插入到StudentTest表中
                            new IDUSTool(MainActivity.this).insertStuTest(unique_test_id, input);
                            Log.i("RecordMarkActivity", "向StudentTest表中插入unique_test_id:"+unique_test_id+"，input:"+input+"成功");

                            //再向StudentMark表中插入当前页面数据
                            Iterator<Record> iterator = recordList.iterator();
                            while(iterator.hasNext()) {
                                //先用preMark保存当前页面mark条目
                                final Record preRecord = iterator.next();

                                String stu_id = preRecord.getStu_id();
//                                    long test_id = unique_test_id;
                                String score = preRecord.getScore();
                                int total_score = preRecord.getTotal_score();

                                boolean isCorrect = preRecord.isCorrect();

                                new IDUSTool(MainActivity.this).insertStuMarkDB(stu_id, unique_test_id, score, total_score, isCorrect);
//                                                Toast.makeText(RecordMarkActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                Log.i("RecordMarkActivity", "向StudentMark表中插入stu_id："+stu_id
                                        +"\r\nunique_test_id："+unique_test_id
                                        +"\r\nscore："+score
                                        +"\r\ntotal_score"+total_score+"成功");

                            }
//                                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();

                        Toast.makeText(MainActivity.this, R.string.save_successfully, Toast.LENGTH_SHORT).show();
                        titleBarView.setTitle(input);
                        isOpenATest = true;

                        isRecordListUpdate = false;
                        setSaveBtnBackground(true);

                        //让AlertDialog消失
                        alertDialog.cancel();
                    }

//                    //获取test_id数据不一致的原因就是SQLite的INTEGER类型存储的是long类型的数据。
//                    long long_to_int_test_id = (int) unique_test_id;
//                    Log.i("RecordMarkActivity", "long转int的unique_test_id:"+long_to_int_test_id);


//                    ShowAndEditActivity.actionStart(MainActivity.this, long_to_int_test_id, input);

//                    if(inRecordUI) {
//                        inRecordUI = false;
//                        test_recycle.setVisibility(View.VISIBLE);
//                        test_fab.setVisibility(View.VISIBLE);
//                        include.setVisibility(View.GONE);
//                        titlebarView.setRightText("");
//                    }
                }
            });
        }
    }

    /**
     * 分享当前页面
     */
    private void shareExcel() {

        //先判断权限是否授予，没有则单独申请，有则继续
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
        } else {
            if (checkRecordListLegality()) {
                //生成唯一id
//          final long unique_test_id = new Date().getTime();//太长
                //每次先查询StudentTest表长getCount，将unique_test_id设置为表长+1
//          final long unique_test_id = getCount() + 1;
//                final long unique_test_id = getTest_idMax() + 1;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String the_only_test_name = dateFormat.format(Calendar.getInstance().getTime());

                if (isOpenATest) {
                    the_only_test_name = current_test_name;
                }

                //导出Excel
                ExportSheet exportSheet = new ExportSheet(MainActivity.this, the_only_test_name, recordList);

                exportSheet.exportSheet();

                try {
                    File file = new File(exportSheet.getFileName());

                    Uri shareFileUri = FileUtil.getFileUri(MainActivity.this, ShareContentType.FILE, file);

                    new Share2.Builder(MainActivity.this)
                            //指定分享的文件类型
                            .setContentType(ShareContentType.FILE)
                            //设置要分享的文件Uri
                            .setShareFileUri(shareFileUri)
                            //设置分享选择器的标题
                            .setTitle(getString(R.string.share_file))
                            .build()
                            //发起分享
                            .shareBySystem();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



            //将时间戳生成的the_only_test_name,unique_test_id和当前页面数据一起保存到数据库中

            //先把test_id和test_name插入到StudentTest表中
//            new IDUSTool(MainActivity.this).insertStuTest(unique_test_id, the_only_test_name);
//
//            //再向StudentMark表中插入当前页面数据
//            Iterator<Record> iterator = recordList.iterator();
//            while(iterator.hasNext()) {
//                //先用preMark保存当前页面mark条目
//                final Record preRecord = iterator.next();
//
//                String stu_id = preRecord.getStu_id();
////                                    long test_id = unique_test_id;
//                String score = preRecord.getScore();
//                int total_score = preRecord.getTotal_score();
//                new IDUSTool(MainActivity.this).insertStuMarkDB(stu_id, unique_test_id, score, total_score);
////                                                Toast.makeText(RecordMarkActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//                Log.i("RecordMarkActivity", "向StudentMark表中插入stu_id："+stu_id
//                        +"\r\nunique_test_id："+unique_test_id
//                        +"\r\nscore："+score
//                        +"\r\ntotal_score"+total_score+"成功");
//
//            }
////            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//
//            //获取test_id数据不一致的原因就是SQLite的INTEGER类型存储的是long类型的数据。
//            long long_to_int_test_id = (int) unique_test_id;
//            Log.i("RecordMarkActivity", "long转int的unique_test_id:"+long_to_int_test_id);
    }

    /**
     * 初始化recordUI的监听器
     */
    private void initListener() {

        selectAll.setOnClickListener(this);

        btnDelete.setOnClickListener(this);

    }

    public boolean editStudentInfo(MenuItem item) {
        EditStudentInfoActivity.actionStart(this);
        return true;
    }

    /**
     * 按back键时根据各种状态自定义返回效果
     */
    @Override
    public void onBackPressed() {
        if (inRecordUI) {
            if (editorStatus) {
                editorStatus = false;
                updateEditMode();
                my_collection_bottom_dialog.setVisibility(View.GONE);

                save_to_db.setVisibility(View.VISIBLE);
                setSaveBtnBackground(isRecordListUpdate);
//                clear_data.setVisibility(View.VISIBLE);
                share_by_excel.setVisibility(View.VISIBLE);
                record_fab.setVisibility(View.VISIBLE);

                return;
            } else {

                if (isRecordListUpdate) {

                    final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this, getString(R.string.save),
                            getString(R.string.recordUI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            save_to_db.performClick();

                            if (!isRecordListUpdate) {
                                recordUI_to_testUI();
                            }

                            alertDialog.dismiss();

                        }
                    });

                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            recordUI_to_testUI();

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

                    recordUI_to_testUI();
                }

//                recordUI_to_testUI();

//                inRecordUI = false;
//                test_recycle.setVisibility(View.VISIBLE);
//                test_fab.setVisibility(View.VISIBLE);
//                include.setVisibility(View.GONE);
//
//                titlebarView.setRightText("");
//                titlebarView.setRightDrawable(R.drawable.ic_more);
//
//                //返回设置isOpenATest
//                isOpenATest = false;
//                //返回设置移除recordRecyclerView分割线
//                recordRecyclerView.removeItemDecoration(recordDividerItemDecoration);
//                //返回设置标题
//                titlebarView.setTitle("园丁小帮手");
//
//                //返回设置左标题和左图标
//                titlebarView.setLeftDrawable(0);
//                titlebarView.setLeftText("");
//
//                //返回设置清空recordList,backUpRecordList
//                recordList.clear();
//                backUpRecordList.clear();
//
//                //返回设置current_test_id,current_test_name为默认
//                current_test_id = -1;
//                current_test_name = "";
//
//                //返回设置index为默认
//                index = 0;
//
//                //返回时重新设置thisPosition
//                testAdapter.setThisPosition(-1);
//                //通知RecyclerView，告诉它Adapter的数据发生了变化
//                testAdapter.notifyDataSetChanged();
//
//                //返回设置先清空testList
//                testList.clear();
//                //再重新add数据到testList
//                initTestList();

                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            point.x = (int)ev.getRawX();
            point.y = (int)ev.getRawY();
        }

//        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View view = getCurrentFocus();
//            if(isHideInput(view, ev)) {
//                hideSoftInput(view.getWindowToken());
//
//                //设置EditText光标模式，isSelectionsGone为true则清除光标
//                recordAdapter.setIsSelectionsGone(true);
//
//            }
//        }

        new TouchEmptyCloseKeyBoardUtils().autoClose(this, ev);

        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定是否需要隐藏软键盘
     * @param v 当前Focus View
     * @param ev MotionEvent
     * @return 需要隐藏键盘返回true,否则返回false
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if(v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if(ev.getX() > left && ev.getX() < right && ev.getY() > top
                    && ev.getY() < bottom) {
                return false;
            }else {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏软键盘
     * @param token 令牌
     */
    private void hideSoftInput(IBinder token) {
        if(token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(floatMenu != null && floatMenu.isShowing()) {
                floatMenu.dismiss();
                return true;
            }
//            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 检查当前页面数据id,score合法性
     * @return 合法返回true,不合法返回false
     */
    private boolean checkRecordListLegality() {
//        if (recordList.size() == 0) {
//            Toast.makeText(MainActivity.this, "数据为空，保存失败", Toast.LENGTH_LONG).show();
//            return false;
//        } else {
            //检查recordList中是否有非法stu_id或非法score
            boolean isStu_idAndScoreLegal = true;
            for(Record record : recordList) {
                if (!canParseInt(record.getStu_id())) {
                    isStu_idAndScoreLegal = false;
                    Toast.makeText(MainActivity.this, getString(R.string.illegal_stu_id_hint), Toast.LENGTH_SHORT).show();
                    break;
                }
                //判断表达式中是否含有空格
                if (record.getScore().contains(" ")) {
                    isStu_idAndScoreLegal = false;
                    Toast.makeText(MainActivity.this, getString(R.string.score_has_blank_space_hint, record.getScore()), Toast.LENGTH_SHORT).show();
                    break;
                }
                //判断表达式是否为算术表达式，但不能判断是否存在非法空格
                if (!(new CheckExpression().checkExpression(record.getScore()))) {
                    isStu_idAndScoreLegal = false;
                    Toast.makeText(MainActivity.this, getString(R.string.illegal_score_hint), Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if(!isStu_idAndScoreLegal) {
                return false;
            } else {
                //检查markList中是否有重复stu_id
                boolean hasSameStu_id = false;
                List<Record> checkList = new ArrayList<>();

                //list.contains(o)，系统会对list中的每个元素e调用o.equals(e)方法，假如list中有n个元素，
                // 那么会调用n次o.equals(e)，只要有一次o.equals(e)返回了true，那么list.contains(o)返回true，
                // 否则返回false。
                for (Record record : recordList) {
                    if (checkList.contains(record)) {
                        Toast.makeText(MainActivity.this, getString(R.string.same_stu_id_hint, record.getStu_id()), Toast.LENGTH_SHORT).show();
                        hasSameStu_id = true;
                        break;
                    }
                    checkList.add(record);
                }
                return !hasSameStu_id;
            }
//        }
    }

    /**
     * 删除dialog
     * @param position 当前长按位置
     */
    private void showDeleteDialog(final int position) {
        //长按则删除
        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this,
                getString(R.string.alarm), getString(R.string.test_long_click_delete_hint, testList.get(position).getTest_name()),
                null, getString(R.string.confirm), getString(R.string.cancel));

        alertDialog.setCancelable(false);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long test_id = testList.get(position).getTest_id();
                testAdapter.remove(position);
                SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
                db.delete("StudentTest", "test_id = ?", new String[]{""+test_id+""});
                db.delete("StudentMark", "test_id = ?", new String[]{""+test_id+""});

                //先去掉分割线，后看情况添加
                testRecyclerView.removeItemDecoration(testDividerItemDecoration);
                onResume();

                alertDialog.dismiss();
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同时通过AlertDialog源码或者Toast源码我们都可以发现它们实现的原理都是WindowManager.addView()来添加的，
                // 它们都是一个个view ,因此不会对activity的生命周期有任何影响。
                onResume();

                alertDialog.dismiss();
            }
        });
    }

    /**
     * xml中设置的监听，对id排序
     * @param v xml中view
     */
    public void idSortControl(View v) {
        if(!isIdSortPressed) {
            isIdSortPressed = true;
            id_sort.setImageResource(R.drawable.ic_drop_up);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        //是“比较o1和o2的大小”。
                        // 返回“负数”，意味着“o1比o2小”
                        // 返回“零”，意味着“o1等于o2”
                        // 返回“正数”，意味着“o1大于o2”
                        //需要先判断o1.getStu_id()和o2.getStu_id()的String类型能否转成int，否则会造成异常
                        boolean o1Can = canParseInt(o1.getStu_id());
                        boolean o2Can = canParseInt(o2.getStu_id());

                        if(o1Can) {
                            if(o2Can) {
                                //都有值,升序
                                return Integer.parseInt(o1.getStu_id()) - Integer.parseInt(o2.getStu_id());
                            } else {
                                //升序，定义非法的小
                                return 1;
                            }
                        } else if (o2Can) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                recordAdapter.notifyDataSetChanged();
            }
        } else {
            isIdSortPressed = false;
            id_sort.setImageResource(R.drawable.ic_drop_down);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        boolean o1Can = canParseInt(o1.getStu_id());
                        boolean o2Can = canParseInt(o2.getStu_id());

                        if(o1Can) {
                            if(o2Can) {
                                //都有值,降序
                                return Integer.parseInt(o2.getStu_id()) - Integer.parseInt(o1.getStu_id()) ;
                            } else {
                                //降序，定义非法的小
                                return -1;
                            }
                        } else if (o2Can) {
                            return 1;
                        } else {
                            return 0;
                        }

                    }
                });
                recordAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * xml中设置的监听，对score排序
     * @param v xml中view
     */
    public void scoreSortControl(View v) {
        if(!isScoreSortPressed) {
            isScoreSortPressed = true;
            score_sort.setImageResource(R.drawable.ic_drop_up);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o1.getTotal_score() - o2.getTotal_score();
                    }
                });
                recordAdapter.notifyDataSetChanged();
            }
        } else {
            isScoreSortPressed = false;
            score_sort.setImageResource(R.drawable.ic_drop_down);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return  o2.getTotal_score() - o1.getTotal_score();
                    }
                });
                recordAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * 读取动态修正返回结果
     * @param results 讯飞语音识别结果
     * @return 返回修正后的结果
     */
    private String updateResult(RecognizerResult results) {

        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d(MainActivity.this.getLocalClassName(), "parseIatResult：" + text);

        String sn = null;
        String pgs = null;
        String rg = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
            pgs = resultJson.optString("pgs");
            rg = resultJson.optString("rg");
            Log.d(this.getClass().getName(), "sn："+sn+"\r\npgs："+pgs+"\r\nrg："+rg);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
        if (pgs.equals("rpl")) {
            Log.d(this.getClass().getName(), "recognizer result replace：" + results.getResultString());
//
            String[] strings = rg.replace("[", "").replace("]", "").split(",");
            int begin = Integer.parseInt(strings[0]);
            int end = Integer.parseInt(strings[1]);
            for (int i = begin; i <= end; i++) {
                mIatResults.remove(i+"");
                Log.d(this.getClass().getName(), "修正mIatResults："+mIatResults.toString());
            }
        }

        mIatResults.put(sn, text);
        Log.d(this.getClass().getName(), "mIatResults"+mIatResults.toString());
        StringBuffer resultBuffer = new StringBuffer();

        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
        //mResultText.setText(resultBuffer.toString())
        //mResultText.setSelection(mResultText.length())
    }


    /**
     * SQLite查询test_id最大值
     * @return 数据库中test_id最大值
     */
    public long getTest_idMax() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
        Cursor cursor = db.rawQuery("select max(test_id) from StudentTest", null);
        cursor.moveToNext();
        Long maxTest_id = cursor.getLong(0);
        cursor.close();
        return maxTest_id;
    }


    /**
     * 使用正则表达式判断该字符串是否为数字，第一个\是转义符，\d+表示匹配1个或 //多个连续数字，"+"和"*"类似，"*"表示0个或多个
     * @param string 待验证字符串
     * @return 是数字则返回true，否则返回false
     */
    public boolean canParseInt(String string){
        if(string == null){ //验证是否为空
            return false;
        }
        return string.matches("\\d+");
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

    private void setSaveBtnBackground(boolean isRecordListUpdate) {
        if (isRecordListUpdate) {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            save_to_db.setEnabled(true);
            save_to_db.setTextColor(Color.WHITE);
        } else {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
            save_to_db.setEnabled(false);
            save_to_db.setTextColor(ContextCompat.getColor(this, R.color.color_b7b8bd));
        }
    }


    /**
     * 实现接口监听
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_delete:
                deleteRecordItem();
                break;
            case R.id.select_all:
                selectAllItem();
                break;
            default:
                break;
        }
    }

    /**
     * 全选和反选按钮逻辑
     */
    private void selectAllItem() {
        if(recordAdapter == null ) return;
        if(!isSelectAll) {
            for (int i = 0, j = recordList.size(); i < j; i++) {
                recordList.get(i).setSelect(true);
            }
            index = recordList.size();
            btnDelete.setEnabled(true);
            selectAll.setText(R.string.cancel_select_all);
            isSelectAll = true;
        } else {
            for (int i=0, j = recordList.size(); i < j; i++) {
                recordList.get(i).setSelect(false);
            }
            index = 0;
            btnDelete.setEnabled(false);
            selectAll.setText(R.string.select_all);
            isSelectAll = false;
        }
        recordAdapter.notifyDataSetChanged();
        setDeleteBtnBackground(index);
        selectNum.setText(String.valueOf(index));
    }

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
                for (int i = recordList.size(), j = 0; i > j; i--) {
                    Record record = recordList.get(i-1);
                    if (record.isSelect()) {

                        recordList.remove(record);
                        index--;
                    }
                }

                //删 更新
                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                index = 0;
                selectNum.setText(String.valueOf(0));
                setDeleteBtnBackground(index);
                if (recordList.size() == 0) {
                    updateEditMode();

                    my_collection_bottom_dialog.setVisibility(View.GONE);

                    if (isOpenATest) {

                        save_to_db.setVisibility(View.VISIBLE);

                        share_by_excel.setVisibility(View.VISIBLE);

                        record_title.setVisibility(View.GONE);

                        record_fab.setVisibility(View.VISIBLE);

                    } else {

                        save_to_db.setVisibility(View.GONE);

                        share_by_excel.setVisibility(View.GONE);

                        record_title.setVisibility(View.GONE);

                        record_fab.setVisibility(View.VISIBLE);

                        titleBarView.setRightText("");
                    }
                }
                recordAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

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
//            clear_data.setVisibility(View.GONE);
            share_by_excel.setVisibility(View.GONE);
            record_fab.setVisibility(View.GONE);

            editorStatus = true;
        } else {
            titleBarView.setRightText(getString(R.string.edit));
            my_collection_bottom_dialog.setVisibility(View.GONE);

            check_box.setVisibility(View.GONE);

            save_to_db.setVisibility(View.VISIBLE);
//            clear_data.setVisibility(View.VISIBLE);
            share_by_excel.setVisibility(View.VISIBLE);
            record_fab.setVisibility(View.VISIBLE);

            editorStatus = false;
            clearAll();
        }
        recordAdapter.setEditMode(editMode);
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
     * 处理讯飞语音听写返回的结果，将处理结果存入recordList,并更新adapter和相关控件的显示隐藏
     * @param results 讯飞语音听写返回的结果
     * @param isLast 是否结束
     */
    private void updateRecordListByRecognizer(RecognizerResult results, boolean isLast) {
        //读取动态修正返回结果
        String resultStr = updateResult(results);
        if(!isLast) return;
        Log.d("MainActivity", "recognizer result：" + resultStr);

        //处理resultStr,将处理后的结果markList中，并刷新页面，newMarkList中为将语音识别结果处理后的字符串
        List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

        if(newMarkList == null) {
            //处理结果为null，无有效成绩
            final AlertDialog alertDialog = GetAlertDialog
                    .getAlertDialog(MainActivity.this, getString(R.string.hint),
                            getString(R.string.no_valid_mark_hint, resultStr),
                            null, getString(R.string.confirm), getString(R.string.cancel));
            alertDialog.setCanceledOnTouchOutside(false);

        } else {
            //不为空，先显示数据，可编辑修改，后由用户选择是否保存数据到数据库中
            //遍历newMarkList，将其添加到markList

            final Iterator<Mark> iteratorNew = newMarkList.iterator();
            while(iteratorNew.hasNext()) {
                final Mark newMark = iteratorNew.next();


                //先根据学号stu_id查出姓名stu_name和stu_gender
                SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
                Cursor cursor = db.query("Student", new String[]{"stu_name", "stu_gender"}, "stu_id = ?",
                        new String[]{""+newMark.getStu_id()+""}, null, null, null);
                String stu_name = null;
                String stu_gender = null;
                while (cursor.moveToNext()) {
                    stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                    stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                }
                cursor.close();

                final Record newRecord = new Record();
                newRecord.setStu_id(newMark.getStu_id());
                newRecord.setScore(newMark.getScore());
                newRecord.setTotal_score(newMark.getTotal_score());
                newRecord.setStu_name(stu_name);
                newRecord.setStu_gender(stu_gender);


                if(recordList.size() == 0) {
                    //第一次添加
                    recordList.add(newRecord);
                    recordAdapter.notifyDataSetChanged();

                    titleBarView.setRightText(getString(R.string.edit));
                    titleBarView.setRightTextColor(Color.WHITE);

                    save_to_db.setVisibility(View.VISIBLE);
//                                clear_data.setVisibility(View.VISIBLE);
                    share_by_excel.setVisibility(View.VISIBLE);


                    record_title.setVisibility(View.VISIBLE);

                    //增 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);

                } else {
                    //flag初始设为false,代表学号不相同
                    boolean flag = false;
                    for(int i=0; i<recordList.size(); i++) {
                        final Record record = recordList.get(i);
                        if(newRecord.getStu_id().equals(record.getStu_id())) {
                            //语音识别stu_id相同，这里处理
                            //弹出dialog,是否更改数据，
                            flag = true;
                            final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this,
                                    getString(R.string.alarm),
                                    getString(R.string.recordUI_record_same_stu_id_hint, newMark.getStu_id(), record.getScore(), newRecord.getScore()),
                                    null, getString(R.string.confirm), getString(R.string.cancel));
                            alertDialog.setCanceledOnTouchOutside(false);

                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //学号相同，更改成绩
                                    record.setScore(newRecord.getScore());
                                    record.setTotal_score(newRecord.getTotal_score());

                                    //改 更新
                                    isRecordListUpdate = true;
                                    setSaveBtnBackground(true);

                                    recordAdapter.notifyDataSetChanged();
                                    alertDialog.dismiss();
                                }
                            });
                            break;
                        }
                    }

                    if(!flag) {
                        //语音识别stu_id没有一个相同，这里处理
                        recordList.add(newRecord);

                        //增 更新
                        isRecordListUpdate = true;
                        setSaveBtnBackground(true);

                        recordAdapter.notifyDataSetChanged();

                        titleBarView.setRightText(getString(R.string.edit));
                        titleBarView.setRightTextColor(Color.WHITE);

                        save_to_db.setVisibility(View.VISIBLE);
//                                    clear_data.setVisibility(View.VISIBLE);
                        share_by_excel.setVisibility(View.VISIBLE);

                        record_title.setVisibility(View.VISIBLE);

                    }
                }

            }
            //清空错误缓存
            mIatResults.clear();
        }
    }

//    @Override
//    public void onItemClick(int position) {
//        if (editorStatus) {
//            Record record = recordList.get(position);
//            boolean isSelect = record.isSelect();
//            if (!isSelect) {
//                index++;
//                record.setSelect(true);
//                if (index == recordList.size()) {
//                    isSelectAll = true;
//                    selectAll.setText("取消全选");
//                }
//            } else {
//                index--;
//                record.setSelect(false);
//                isSelectAll = false;
//                selectAll.setText("全选");
//            }
//            setDeleteBtnBackground(index);
//            selectNum.setText(String.valueOf(index));
//            recordAdapter.notifyDataSetChanged();
//        }
//    }

//    @Override
//    public void onItemLongClick(int position) {
//        //暂不处理
//    }
}
