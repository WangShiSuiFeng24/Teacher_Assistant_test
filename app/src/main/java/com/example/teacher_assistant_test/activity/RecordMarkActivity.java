package com.example.teacher_assistant_test.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.MarkAdapter;
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.IDUSTool;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecordMarkActivity extends AppCompatActivity {
    private static final String TAG = "RecordMarkActivity";

    private List<Mark> markList = new ArrayList<>();

    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;

    private MarkAdapter markAdapter;

    private boolean isIdSortPressed;
    private ImageView id_sort;
    private boolean isScoreSortPressed;
    private ImageView score_sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mark);

        ImmersiveStatusBar.setImmersiveStatusBar(this);

        TitleBarView titleBarView = findViewById(R.id.title3);
        titleBarView.setTitleSize(20);
        titleBarView.setTitle("园丁小帮手");
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


        isIdSortPressed = false;
        id_sort = findViewById(R.id.id_sort);
        isScoreSortPressed = false;
        score_sort = findViewById(R.id.score_sort);

        final RecyclerView recyclerView = findViewById(R.id.Recycler_View_Mark);

        //设置分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RecordMarkActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        markAdapter = new MarkAdapter(markList);
        recyclerView.setAdapter(markAdapter);

        markAdapter.setOnStuIdFillListener(new MarkAdapter.OnStuIdFillListener() {
            @Override
            public void onStuIdFill(int position, String stu_id) {
                //编辑学号监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < markList.size()) {
                    markList.get(position).setStu_id(stu_id);
                }
            }
        });

        markAdapter.setOnScoreFillListener(new MarkAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(final int position, String score) {
                //编辑成绩监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < markList.size()) {
                    if(!TextUtils.isEmpty(score)) {
                        markList.get(position).setScore(score);
                        //先要判断编辑的score字符串是否符合规则，是则计算total_score,否则不计算
                        if(new CheckExpression().checkExpression(score)) {
//                        score = score.replaceAll(" ","");
                            int total_score = (int) new Calculator().calculate(score);
                            markList.get(position).setTotal_score(total_score);

//                            //换个方式刷新页面...还是有问题，刷新一次后editText失去焦点
//                            recyclerView.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    markAdapter.notifyDataSetChanged();
//                                }
//                            },1000);

                            //在一个ViewHolder.onBind()里面用通过bus发消息去通知刷新列表notifyDataSetChanged()，
                            // 这个时候刚好列表在滚动或者在layout()，那么就会报错。
                            // Caused by java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing
                            //此法原理：
                            //主线程刷新UI是通过消息队列，当列表正在滚动或者layout时调用notifyDataSetChanged()，
                            //那么notifyDataSetChanged()里面的代码是和正在滚动或者layout同一消息里面的，如果加上Handler.post()，
                            //那么就是新建立消息放入消息队列末尾，这样两个刷新不在同一个消息，就完美避开了这个问题。

                            if(!recyclerView.isComputingLayout()) {
                                markAdapter.notifyDataSetChanged();
                            }
//                            new Handler().post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // 刷新操作
//                                    markAdapter.notifyItemChanged(position);
//    //                                markAdapter.notifyDataSetChanged();
//    //                                notifyDataSetChanged();
//                                }
//                            });

                        }
                    }
                }
            }
        });

        //    private FloatingActionButton fab;
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
                    //使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
                    mIatDialog = new RecognizerDialog(RecordMarkActivity.this, mInitListener);

//                if(mIatDialog!=null)
                    mIatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");
                    mIatDialog.setParameter("dwa", "wpgs");

                    mIatDialog.setListener(mRecognizerDialogListener);

                    mIatDialog.show();
                    TextView txt = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
                    txt.setText(R.string.tip);//更改内容
                    txt.getPaint().setFlags(Paint.SUBPIXEL_TEXT_FLAG);//去下划线

            }
        });

        markAdapter.setOnItemClickListener(new MarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(RecordMarkActivity.this, "Alarm",
                        "将要删除学号为："+markList.get(position).getStu_id()+"的条目", null, "yes", "no");
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markAdapter.remove(position);
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
                markList.clear();
                markAdapter.notifyDataSetChanged();
                Toast.makeText(RecordMarkActivity.this, "已清空！", Toast.LENGTH_SHORT).show();
            }
        });

        //保存当前页面数据到数据库
        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //检查markList是否为空
                if(markList.size() != 0) {
                    //检查markList中是否有非法stu_id或非法score
                    boolean isLegal = true;
                    for(Mark mark : markList) {
                        if(!canParseInt(mark.getStu_id())) {
                            isLegal = false;
                            Toast.makeText(RecordMarkActivity.this, "学号："+mark.getStu_id()+" 非法", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if(!(new CheckExpression().checkExpression(mark.getScore()))) {
                            isLegal = false;
                            Toast.makeText(RecordMarkActivity.this, "成绩："+mark.getScore()+" 非法", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if(isLegal) {
                        //检查markList中是否有重复stu_id
                        boolean flag = false;
                        List<Mark> checkList = new ArrayList<>();

                        //list.contains(o)，系统会对list中的每个元素e调用o.equals(e)方法，假如list中有n个元素，
                        // 那么会调用n次o.equals(e)，只要有一次o.equals(e)返回了true，那么list.contains(o)返回true，
                        // 否则返回false。
                        for(Mark mark : markList) {
                            if(checkList.contains(mark)) {
                                Toast.makeText(RecordMarkActivity.this, "有重复学号："+mark.getStu_id(), Toast.LENGTH_SHORT).show();
                                flag = true;
                                break;
                            }
                            checkList.add(mark);
                        }

                        if(!flag) {
                            final EditText edit = new EditText(RecordMarkActivity.this);
                            //设置EditText的可视最大行数。
                            edit.setMaxLines(1);
                            //先弹出一个可编辑的AlertDialog，可以编辑test_name
                            final AlertDialog alertDialog = GetAlertDialog
                                    .getAlertDialog(RecordMarkActivity.this,"测验名：",
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
                                        final SQLiteDatabase db = MyDatabaseHelper.getInstance(RecordMarkActivity.this);
                                        //查询数据库中是否存在与将要插入的preMark相同的test_name,若相同，则提示用户test_name已存在重新输入，否则直接新建插入全部数据
                                        String sqlSelect = "SELECT test_name FROM StudentTest";
                                        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

                                        if(cursor.isLast()) {
                                            //StudentTest表为空，第一次更新
                                            new IDUSTool(RecordMarkActivity.this).insertStuTest(unique_test_id, input);
                                            Toast.makeText(RecordMarkActivity.this, "数据库为空，保存成功", Toast.LENGTH_SHORT).show();
                                        }

                                        else {
                                            //StudentTest表不为空
//                                //flag初始设为false,代表StudentTest表中有没相同的test_name
//                                boolean flag = false;
                                            while(cursor.moveToNext()) {
                                                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

                                                if(input.equals(test_name)) {
                                                    //StudentTest表中已有相同test_name,提示用户重新输入
                                                    Toast.makeText(RecordMarkActivity.this, input+" 已存在，请重新输入", Toast.LENGTH_SHORT).show();
//                                        flag = true;
                                                    return;
                                                }
                                            }

                                            //循环检查完毕，此时没有相同的test_name,直接向StudentTest表中插入所有数据，不用判断

                                            //先把test_id和test_name插入到StudentTest表中
                                            new IDUSTool(RecordMarkActivity.this).insertStuTest(unique_test_id, input);
                                            Log.i("RecordMarkActivity", "向StudentTest表中插入unique_test_id:"+unique_test_id+"，input:"+input+"成功");

                                            //再向StudentMark表中插入当前页面数据
                                            Iterator<Mark> iterator = markList.iterator();
                                            while(iterator.hasNext()) {
                                                //先用preMark保存当前页面mark条目
                                                final Mark preMark = iterator.next();

                                                String stu_id = preMark.getStu_id();
//                                    long test_id = unique_test_id;
                                                String score = preMark.getScore();
                                                int total_score = preMark.getTotal_score();
                                                new IDUSTool(RecordMarkActivity.this).insertStuMarkDB(stu_id, unique_test_id, score, total_score);
                                                Toast.makeText(RecordMarkActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                                Log.i("RecordMarkActivity", "向StudentMark表中插入stu_id："+stu_id
                                                        +"\r\nunique_test_id："+unique_test_id
                                                        +"\r\nscore："+score
                                                        +"\r\ntotal_score"+total_score+"成功");

                                            }
                                        }
                                        cursor.close();
                                        //让AlertDialog消失
                                        alertDialog.cancel();
                                    }

                                    //获取test_id数据不一致的原因就是SQLite的INTEGER类型存储的是long类型的数据。
                                    long long_to_int_test_id = (int) unique_test_id;
                                    Log.i("RecordMarkActivity", "long转int的unique_test_id:"+long_to_int_test_id);

                                    ShowAndEditActivity.actionStart(RecordMarkActivity.this, long_to_int_test_id, input);

//                                    Intent intent = new Intent(RecordMarkActivity.this, ShowAndEditActivity.class);
//                                    intent.putExtra("test_id", long_to_int_test_id);
//                                    intent.putExtra("test_name", input);
//                                    startActivity(intent);

                                    finish();
                                }
                            });
                        }
                    }

                } else {
                    Toast.makeText(RecordMarkActivity.this, "保存数据为空", Toast.LENGTH_SHORT).show();
                }
            }
        });



        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(RecordMarkActivity.this.getLocalClassName(), "SpeechRecognizer init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(RecordMarkActivity.this, "初始化失败，错误码：" + code + "，请点击网址https://www.xfyun.cn/document/error-code查询解决方案", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mRecognizerDialogListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(this.getClass().getName(), "recognizer json result：" + results.getResultString());

                String resultStr = updateResult(results);
                if (isLast) {
                    Log.d(TAG, "recognizer result：" + resultStr);
//                    showTip(resultStr);

                    //处理resultStr,将处理后的结果markList中，并刷新页面，newMarkList中为将语音识别结果处理后的字符串
                    List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

                    if(newMarkList == null) {
                        //处理结果为null，无有效成绩
                        final AlertDialog alertDialog = GetAlertDialog
                                .getAlertDialog(RecordMarkActivity.this, "Tip",
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
                            if(markList.size() == 0) {
                                //第一次添加
                                markList.add(newMark);
                                markAdapter.notifyDataSetChanged();
                            } else {
                                //flag初始设为false,代表学号不相同
                                boolean flag = false;
                                for(int i=0; i<markList.size(); i++) {
                                    final Mark mark = markList.get(i);
                                    if(newMark.getStu_id().equals(mark.getStu_id())) {
                                        //语音识别stu_id相同，这里处理
                                        //弹出dialog,是否更改数据，
                                        flag = true;
                                        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(RecordMarkActivity.this,
                                                "Alarm", "学号："+newMark.getStu_id()+"已存在，"+
                                                "是否需要更改成绩："+mark.getScore()+"为："+newMark.getScore(),
                                                null, "OK", "CANCEL");
                                        alertDialog.setCanceledOnTouchOutside(false);

                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //学号相同，更改成绩
                                                mark.setScore(newMark.getScore());
                                                mark.setTotal_score(newMark.getTotal_score());
                                                markAdapter.notifyDataSetChanged();
                                                alertDialog.dismiss();
                                            }
                                        });
                                        break;
                                    }
                                }

                                if(!flag) {
                                    //语音识别stu_id没有一个相同，这里处理
                                    markList.add(newMark);
                                    markAdapter.notifyDataSetChanged();
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
//                Toast.makeText(RecordMarkActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        };

        // 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5db04b35");

        //已进入当前页面就执行点击事件
        fab.performClick();
    }

    //在Activity中重写onSaveInstanceState(Bundle outState)方法实现横竖屏切换保存数据
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            super.onSaveInstanceState(outState);
        }
    }

    public void idSortControl(View v) {
        if(!isIdSortPressed) {
            isIdSortPressed = true;
            id_sort.setImageResource(R.drawable.ic_drop_up);
            if(markList.size() != 0) {
                Collections.sort(markList, new Comparator<Mark>() {
                    @Override
                    public int compare(Mark o1, Mark o2) {
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
                markAdapter.notifyDataSetChanged();
            }
        } else {
            isIdSortPressed = false;
            id_sort.setImageResource(R.drawable.ic_drop_down);
            if(markList.size() != 0) {
                Collections.sort(markList, new Comparator<Mark>() {
                    @Override
                    public int compare(Mark o1, Mark o2) {
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
                markAdapter.notifyDataSetChanged();
            }
        }
    }

    public void scoreSortControl(View v) {
        if(!isScoreSortPressed) {
            isScoreSortPressed = true;
            score_sort.setImageResource(R.drawable.ic_drop_up);
            if(markList.size() != 0) {
                Collections.sort(markList, new Comparator<Mark>() {
                    @Override
                    public int compare(Mark o1, Mark o2) {
                        return o1.getTotal_score() - o2.getTotal_score();
                    }
                });
                markAdapter.notifyDataSetChanged();
            }
        } else {
            isScoreSortPressed = false;
            score_sort.setImageResource(R.drawable.ic_drop_down);
            if(markList.size() != 0) {
                Collections.sort(markList, new Comparator<Mark>() {
                    @Override
                    public int compare(Mark o1, Mark o2) {
                        return  o2.getTotal_score() - o1.getTotal_score();
                    }
                });
                markAdapter.notifyDataSetChanged();
            }
        }
    }


    // 读取动态修正返回结果
    private String updateResult(RecognizerResult results) {

        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d(RecordMarkActivity.this.getLocalClassName(), "parseIatResult：" + text);

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

    // 获取点击事件
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

    //SQLite查询test_id记录总数
    public long getCount() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(RecordMarkActivity.this);
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

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, RecordMarkActivity.class);
        context.startActivity(intent);
    }


//    private void showTip(final String str) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Snackbar.make(fab, str, Snackbar.LENGTH_INDEFINITE)
//                        .setAction("Action", null).show();
//            }
//        });
//    }
}
