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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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

import com.example.teacher_assistant_test.adapter.MarkAdapter;
import com.example.teacher_assistant_test.adapter.RecordAdapter;
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Record;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.StrProcess;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.TestAdapter;
import com.example.teacher_assistant_test.bean.Test;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;
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

import org.apache.log4j.chainsaw.Main;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private List<Test> testList = new ArrayList<>();
    private TestAdapter testAdapter;
    private RecyclerView recyclerView;
    private DividerItemDecoration dividerItemDecoration;

    private Point point = new Point();
    private FloatMenu floatMenu;




    private List<Record> recordList = new ArrayList<>();

    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;

    private RecordAdapter recordAdapter;

    private boolean isIdSortPressed;
    private ImageView id_sort;
    private boolean isScoreSortPressed;
    private ImageView score_sort;

    private FloatingActionButton record_fab;

    private boolean inRecordUI = false;

    private View include;
    private LinearLayout test_recycle;
    private LinearLayout test_fab;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏
        setContentView(R.layout.activity_main);

        ImmersiveStatusBar.setImmersiveStatusBar(this);

//        //隐藏标题栏
//        if(getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }

        final TitleBarView titlebarView= findViewById(R.id.title);
        titlebarView.setTitleSize(20);
        titlebarView.setTitle("园丁小帮手");
        titlebarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                //暂不作处理
            }

            @Override
            public void tvRightClick() {
                //暂不作处理
            }

            @Override
            public void ivRightClick(View view) {
                //弹出PopupMenu
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.show();
            }
        });

        importSheet();

        recyclerView = findViewById(R.id.Recycler_View_Test);

        //添加自定义分割线
//        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
//        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
//        recyclerView.addItemDecoration(divider);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        testAdapter = new TestAdapter(testList);
        recyclerView.setAdapter(testAdapter);

        dividerItemDecoration = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
//
//        if(testList.size() != 0) {
//            //添加Android自带的分割线
//            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        }

        testAdapter.setOnItemClickListener(new TestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                //拿适配器调用适配器内部自定义好的setThisPosition方法（参数写点击事件的参数的position）
                testAdapter.setThisPosition(position);
                testAdapter.notifyDataSetChanged();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //单击则查询
                        Test test = testList.get(position);
                        ShowAndEditActivity.actionStart(MainActivity.this, test.getTest_id(), test.getTest_name());
                    }
                },100);

//                //单击则查询
//                Test test = testList.get(position);
//                ShowAndEditActivity.actionStart(MainActivity.this, test.getTest_id(), test.getTest_name());
            }

            @Override
            public void onItemLongClick(final int position, View view) {

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
        });

        final FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先判断权限是否授予，没有则单独申请，有则跳转
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                    request_permissions(MainActivity.this);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    include = findViewById(R.id.record_mark);
                    test_recycle = findViewById(R.id.test_recycle);
                    test_fab = findViewById(R.id.test_fab);

                    test_recycle.setVisibility(View.GONE);
                    test_fab.setVisibility(View.GONE);
                    include.setVisibility(View.VISIBLE);

                    inRecordUI = true;

                    //已进入当前页面就执行点击事件
                    record_fab.performClick();

//                    RecordMarkActivity.actionStart(MainActivity.this);
                }
            }
        });

        request_permissions(this);



        isIdSortPressed = false;
        id_sort = findViewById(R.id.id_sort);
        isScoreSortPressed = false;
        score_sort = findViewById(R.id.score_sort);

//        final RecyclerView recyclerView = findViewById(R.id.Recycler_View_Mark);
        final RecyclerViewEmptySupport record_recyclerView = findViewById(R.id.Recycler_View_Mark);
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyMessage = findViewById(R.id.empty_message);
        emptyMessage.setText("暂无成绩数据，请点击下方录音按钮创建新成绩。");
        //设置分割线
        record_recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        LinearLayoutManager record_linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        record_recyclerView.setLayoutManager(record_linearLayoutManager);
        record_recyclerView.setEmptyView(emptyView);
        recordAdapter = new RecordAdapter(recordList);
        record_recyclerView.setAdapter(recordAdapter);

        recordAdapter.setOnStuIdFillListener(new MarkAdapter.OnStuIdFillListener() {
            @Override
            public void onStuIdFill(int position, String stu_id) {
                //编辑学号监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {
                    recordList.get(position).setStu_id(stu_id);
                }
            }
        });

        recordAdapter.setOnScoreFillListener(new MarkAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(final int position, String score) {
                //编辑成绩监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {
                    if(!TextUtils.isEmpty(score)) {
                        recordList.get(position).setScore(score);
                        //先要判断编辑的score字符串是否符合规则，是则计算total_score,否则不计算
                        if(new CheckExpression().checkExpression(score)) {
//                        score = score.replaceAll(" ","");
                            int total_score = (int) new Calculator().calculate(score);
                            recordList.get(position).setTotal_score(total_score);

                            if(!record_recyclerView.isComputingLayout()) {
                                recordAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }
        });

        record_fab = findViewById(R.id.record_fab);

        record_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        recordAdapter.setOnItemClickListener(new MarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this, "Alarm",
                        "将要删除学号为："+recordList.get(position).getStu_id()+"的条目", null, "yes", "no");
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordAdapter.remove(position);
                        alertDialog.dismiss();
                    }
                });
            }

            @Override
            public void onItemLongClick(int position) {
//                markAdapter.remove(position);
            }
        });

        Button clear_data = findViewById(R.id.clear_data);
        Button save_to_db = findViewById(R.id.save_to_db);

        //清除当前页面数据
        clear_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordList.clear();
                recordAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "已清空！", Toast.LENGTH_SHORT).show();
            }
        });

        //保存当前页面数据到数据库
        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //检查markList是否为空
                if(recordList.size() != 0) {
                    //检查markList中是否有非法stu_id或非法score
                    boolean isLegal = true;
                    for(Record record : recordList) {
                        if(!canParseInt(record.getStu_id())) {
                            isLegal = false;
                            Toast.makeText(MainActivity.this, "学号："+record.getStu_id()+" 非法", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if(!(new CheckExpression().checkExpression(record.getScore()))) {
                            isLegal = false;
                            Toast.makeText(MainActivity.this, "成绩："+record.getScore()+" 非法", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if(isLegal) {
                        //检查markList中是否有重复stu_id
                        boolean flag = false;
                        List<Record> checkList = new ArrayList<>();

                        //list.contains(o)，系统会对list中的每个元素e调用o.equals(e)方法，假如list中有n个元素，
                        // 那么会调用n次o.equals(e)，只要有一次o.equals(e)返回了true，那么list.contains(o)返回true，
                        // 否则返回false。
                        for(Record record : recordList) {
                            if(checkList.contains(record)) {
                                Toast.makeText(MainActivity.this, "有重复学号："+record.getStu_id(), Toast.LENGTH_SHORT).show();
                                flag = true;
                                break;
                            }
                            checkList.add(record);
                        }

                        if(!flag) {
                            final EditText edit = new EditText(MainActivity.this);
                            //设置EditText的可视最大行数。
                            edit.setMaxLines(1);
                            //先弹出一个可编辑的AlertDialog，可以编辑test_name
                            final AlertDialog alertDialog = GetAlertDialog
                                    .getAlertDialog(MainActivity.this,"测验名：",
                                            null, edit, "确定", "取消");
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
                                    final long unique_test_id = getCount() + 1;

                                    Log.i("RecordMarkActivity", "unique_test_id:"+unique_test_id);
                                    if (input.equals("")) {
                                        Toast.makeText(getApplicationContext(), "内容不能为空！" + input, Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
//                            String editText = edit.getText().toString().trim();

                                        //将用户输入的Test_Name,unique_test_id和当前页面数据一起保存到数据库中
                                        final SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
                                        //查询数据库中是否存在与将要插入的preMark相同的test_name,若相同，则提示用户test_name已存在重新输入，否则直接新建插入全部数据
                                        String sqlSelect = "SELECT test_name FROM StudentTest";
                                        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

                                        if(cursor.isLast()) {
                                            //StudentTest表为空，第一次更新
                                            new IDUSTool(MainActivity.this).insertStuTest(unique_test_id, input);
                                            Toast.makeText(MainActivity.this, "数据库为空，保存成功", Toast.LENGTH_SHORT).show();
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
                                                new IDUSTool(MainActivity.this).insertStuMarkDB(stu_id, unique_test_id, score, total_score);
//                                                Toast.makeText(RecordMarkActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                                Log.i("RecordMarkActivity", "向StudentMark表中插入stu_id："+stu_id
                                                        +"\r\nunique_test_id："+unique_test_id
                                                        +"\r\nscore："+score
                                                        +"\r\ntotal_score"+total_score+"成功");

                                            }
                                            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                        }
                                        cursor.close();
                                        //让AlertDialog消失
                                        alertDialog.cancel();
                                    }

                                    //获取test_id数据不一致的原因就是SQLite的INTEGER类型存储的是long类型的数据。
                                    long long_to_int_test_id = (int) unique_test_id;
                                    Log.i("RecordMarkActivity", "long转int的unique_test_id:"+long_to_int_test_id);

                                    ShowAndEditActivity.actionStart(MainActivity.this, long_to_int_test_id, input);

//                                    Intent intent = new Intent(RecordMarkActivity.this, ShowAndEditActivity.class);
//                                    intent.putExtra("test_id", long_to_int_test_id);
//                                    intent.putExtra("test_name", input);
//                                    startActivity(intent);

//                                    finish();
                                }
                            });
                        }
                    }

                } else {
                    Toast.makeText(MainActivity.this, "保存数据为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(MainActivity.this.getLocalClassName(), "SpeechRecognizer init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(MainActivity.this, "初始化失败，错误码：" + code + "，请点击网址https://www.xfyun.cn/document/error-code查询解决方案", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRecognizerDialogListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(this.getClass().getName(), "recognizer json result：" + results.getResultString());

                String resultStr = updateResult(results);
                if (isLast) {
                    Log.d("MainActivity", "recognizer result：" + resultStr);
//                    showTip(resultStr);

                    //处理resultStr,将处理后的结果markList中，并刷新页面，newMarkList中为将语音识别结果处理后的字符串
                    List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

                    if(newMarkList == null) {
                        //处理结果为null，无有效成绩
                        final AlertDialog alertDialog = GetAlertDialog
                                .getAlertDialog(MainActivity.this, "Tip",
                                        "语音识别结果为："+resultStr+"\r\n无有效成绩数据，请重新录音！\r\n语音录成绩格式请参照：＂8号 88(+8)分＂这样效果会更好哦！",
                                        null, "OK", "CANCEL");
                        alertDialog.setCanceledOnTouchOutside(false);

//                        //调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
////                        dialog.setCanceledOnTouchOutside(false);
//                        //调用这个方法时，按对话框以外的地方不起作用。按返回键也不起作用
//                        dialog.setCancelable(false);
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
                                                "Alarm", "学号："+newMark.getStu_id()+"已存在，"+
                                                        "是否需要更改成绩："+record.getScore()+"为："+newRecord.getScore(),
                                                null, "OK", "CANCEL");
                                        alertDialog.setCanceledOnTouchOutside(false);

                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //学号相同，更改成绩
                                                record.setScore(newRecord.getScore());
                                                record.setTotal_score(newRecord.getTotal_score());
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
                                    recordAdapter.notifyDataSetChanged();
                                }
                            }

                        }
                        //清空错误缓存
                        mIatResults.clear();
                    }
                }
            }

            @Override
            public void onError(SpeechError error) {
                /**
                 * 过滤掉没有说话的错误码显示
                 */
                TextView tv_error = mIatDialog.getWindow().getDecorView().findViewWithTag("errtxt");
                if (tv_error != null) {
                    tv_error.setText("您好像没有说话哦。。。");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 1002) {
//            // 1002请求码对应的是申请多个权限
//            if (grantResults.length > 0) {
//                // 因为是多个权限，所以需要一个循环获取每个权限的获取情况
//                for (int i = 0; i < grantResults.length; i++) {
//                    // PERMISSION_DENIED 这个值代表是没有授权，我们可以把被拒绝授权的权限显示出来
//                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                        Toast.makeText(MainActivity.this, permissions[i] + "权限被拒绝了", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }
        if(requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "缺少录制音频权限，可能会造成无法语音识别", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //返回刷新，小技巧是把需要更新的view 可以都放在initView（）中，在resume中调用这个方法即可。
    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume()");
        super.onResume();
        //先清空testList
        testList.clear();
        //再重新add数据到testList
        initTest();

        recyclerView.removeItemDecoration(dividerItemDecoration);
        if(testList.size() != 0 ) {
            //添加Android自带的分割线
            recyclerView.addItemDecoration(dividerItemDecoration);
        }
        //返回时重新设置thisPosition
        testAdapter.setThisPosition(-1);
        //通知RecyclerView，告诉它Adapter的数据发生了变化
        testAdapter.notifyDataSetChanged();
    }

    public boolean editStudentInfo(MenuItem item) {
        EditStudentInfoActivity.actionStart(this);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(inRecordUI) {
            inRecordUI = false;
            test_recycle.setVisibility(View.VISIBLE);
            test_fab.setVisibility(View.VISIBLE);
            include.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            point.x = (int)ev.getRawX();
            point.y = (int)ev.getRawY();
        }

        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if(isHideInput(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }

        return super.dispatchTouchEvent(ev);
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

    private void showDeleteDialog(final int position) {
        //长按则删除
        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this,
                "Alarm", "将要删除测试名为："+testList.get(position).getTest_name()+"的条目",
                null, "Yes", "No");

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
                recyclerView.removeItemDecoration(dividerItemDecoration);
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



    private void initTest() {
        String sqlSelect = "SELECT StudentTest.test_id,StudentTest.test_name FROM StudentTest";
        SQLiteDatabase database = MyDatabaseHelper.getInstance(MainActivity.this);
        Cursor cursor = database.rawQuery(sqlSelect, new String[]{});
        while(cursor.moveToNext()) {
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

            Test test = new Test(test_id, test_name);
            testList.add(test);
        }
        cursor.close();
    }


    private void importSheet() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
        ContentValues values = new ContentValues();
        try {
            InputStream is = getResources().getAssets().open("id_name_info.xls");

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

    // 读取动态修正返回结果
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

    //SQLite查询test_id记录总数
    public long getCount() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(MainActivity.this);
        Cursor cursor = db.rawQuery("select count(test_id) from StudentTest",null);
        cursor.moveToFirst();
        Long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    //使用正则表达式判断该字符串是否为数字，第一个\是转义符，\d+表示匹配1个或 //多个连续数字，"+"和"*"类似，"*"表示0个或多个
    public boolean canParseInt(String string){
        if(string == null){ //验证是否为空
            return false;
        }
        return string.matches("\\d+");
    }

    // 判定是否需要隐藏
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
    // 隐藏软键盘
    private void hideSoftInput(IBinder token) {
        if(token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
