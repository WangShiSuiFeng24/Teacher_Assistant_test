package com.example.teacher_assistant_test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.StrProcess;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class Main3Activity extends AppCompatActivity {
    private static final String TAG = "Main3Activity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private List<Mark> markList = new ArrayList<>();
    private Button clear_data;
    private Button save_to_db;

    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;
//    private FloatingActionButton fab;
    private Button fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        final RecyclerView recyclerView = findViewById(R.id.Recycler_View_Mark);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Main3Activity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        final MarkAdapter markAdapter = new MarkAdapter(markList);
        recyclerView.setAdapter(markAdapter);

        clear_data = findViewById(R.id.clear_data);
        save_to_db = findViewById(R.id.save_to_db);

        //清除当前页面数据
        clear_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markList.clear();
                markAdapter.notifyDataSetChanged();
                Toast.makeText(Main3Activity.this, "已清空！", Toast.LENGTH_SHORT).show();
            }
        });

        //保存当前页面数据到数据库
        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final SQLiteDatabase db = MyDatabaseHelper.getInstance(Main3Activity.this);
                Iterator<Mark> iterator = markList.iterator();
                while(iterator.hasNext()) {
                    //先用preMark保存当前页面mark条目
                    final Mark preMark = iterator.next();

                    //查询数据库中是否存在与将要插入的preMark相同的stu_id,若相同，则警告是否修改，否则直接插入
                    String sqlSelect = "SELECT StudentMark.stu_id,StudentMark.score FROM StudentMark";
                    SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main3Activity.this);
                    Cursor cursor = sd.rawQuery(sqlSelect, new String[] {});
                    if(cursor.isLast()) {
                        //StudentMark表为空，第一次更新
                        int stu_id = preMark.getStu_id();
                        String score = preMark.getScore();
                        int total_score = preMark.getTotal_score();

                        new IDUSTool(Main3Activity.this).insertStuMarkDB(stu_id, score, total_score);
                    } else {
                        //StudentMark表不为空
                        //flag初始设为false,代表学号不相同
                        boolean flag = false;
                        while(cursor.moveToNext()) {
                            final int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
                            String score = cursor.getString(cursor.getColumnIndex("score"));
                            if(preMark.getStu_id() == stu_id) {
                                if(!preMark.getScore().equals(score)) {
                                    flag = true;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                                    builder.setCancelable(false)
                                            .setTitle("Alarm")
                                            .setMessage("数据库学号:"+preMark.getStu_id()+" 已存在,"+
                                                    "是否需要更改成绩:"+score+"为:"+preMark.getScore())
                                            .setPositiveButton("确定更改!", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //更改数据库中相同学号的成绩score
                                                    ContentValues values = new ContentValues();
                                                    values.put("score", preMark.getScore());
                                                    values.put("total_score", preMark.getTotal_score());
                                                    db.update("StudentMark", values, "stu_id=?", new String[]{String.valueOf(stu_id)});
                                                    Toast.makeText(Main3Activity.this, stu_id+"号更改成功!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .setNegativeButton("取消更改!", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //取消更改成绩
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                    break;
                                }
                            }
                        }

                        if(!flag) {
                            int stu_id = preMark.getStu_id();
                            String score = preMark.getScore();
                            int total_score = preMark.getTotal_score();

                            new IDUSTool(Main3Activity.this).insertStuMarkDB(stu_id, score, total_score);
                            Toast.makeText(Main3Activity.this, "已保存!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        fab = findViewById(R.id.fab);
        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(Main3Activity.this.getLocalClassName(), "SpeechRecognizer init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");                }
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

                    //处理resultStr
                    List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

                    if(newMarkList == null) {
                        //处理结果为null，无有效成绩
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                        builder.setTitle("Tip")
                                .setMessage("语音识别结果为:"+resultStr+"\r\n无有效成绩数据，请重新录音！\r\n录音规则请参照:\"10号 90+9\"")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        //调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
//                        dialog.setCanceledOnTouchOutside(false);
                        //调用这个方法时，按对话框以外的地方不起作用。按返回键也不起作用
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        //不为空，先显示数据，可修改，后由用户选择是否保存数据到数据库中
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
                                    if(newMark.getStu_id() == mark.getStu_id()) {
                                        //语音识别stu_id相同，这里处理
                                        //弹出dialog,是否更改数据，
                                        flag = true;
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("Alarm").
                                                setMessage("学号:"+newMark.getStu_id()+" 已存在,"+
                                                        "是否需要更改成绩:"+mark.getScore()+"为:"+newMark.getScore())
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //学号相同，更改成绩
                                                        mark.setScore(newMark.getScore());
                                                        markAdapter.notifyDataSetChanged();
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //取消更改成绩
                                                        dialog.dismiss();
                                                    }
                                                }).show();
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
                showTip(error.getPlainDescription(true));
            }
        };

        // 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5db04b35");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(Main3Activity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Main3Activity.this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(Main3Activity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted

                    //初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
                    //使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
                    mIatDialog = new RecognizerDialog(Main3Activity.this, mInitListener);

//                if(mIatDialog!=null)
                    mIatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");
                    mIatDialog.setParameter("dwa", "wpgs");

                    mIatDialog.setListener(mRecognizerDialogListener);

                    mIatDialog.show();
                }
            }
        });

//        initScore();
    }

    //在Activity中重写onSaveInstanceState(Bundle outState)方法实现横竖屏切换保存数据
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

//    private void initScore() {
//        String sqlSelect="SELECT * FROM StudentMark";
//        //扫描数据库，将信息放入markList
////        MyDatabaseHelper mdb = new MyDatabaseHelper(this, "Mark.db", null, 2);//打开数据库
////        SQLiteDatabase sd = mdb.getReadableDatabase();//获取数据库
//        SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main3Activity.this);
//        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});
//        while(cursor.moveToNext()){
//            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
//            String score = cursor.getString(cursor.getColumnIndex("score"));
//            int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));
//
//            Mark mark = new Mark(stu_id, score, total_score);
//            markList.add(mark);
//        }
//        cursor.close();
//    }

    // 读取动态修正返回结果
    private String updateResult(RecognizerResult results) {

        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d(Main3Activity.this.getLocalClassName(), "parseIatResult：" + text);

        String sn = null;
        String pgs = null;
        String rg = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
            pgs = resultJson.optString("pgs");
            rg = resultJson.optString("rg");
            Log.d(this.getClass().getName(), "sn:"+sn+"\r\npgs:"+pgs+"\r\ng:"+rg);
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
                Log.d(this.getClass().getName(), "修正mIatResults"+mIatResults.toString());
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

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(fab, str, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        });
    }
}
