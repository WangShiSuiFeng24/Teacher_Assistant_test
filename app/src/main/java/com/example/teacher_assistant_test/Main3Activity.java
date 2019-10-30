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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
    private List<Student> studentList = new ArrayList<>();

    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;
    private FloatingActionButton fab;

//    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        myDatabaseHelper = new MyDatabaseHelper(this,"Student.db",null,2);
        setContentView(R.layout.activity_main3);

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
                Log.d(this.getClass().getName(), "recognizer result：" + results.getResultString());

                String resultStr = updateResult(results);
                if (isLast) {
                    Log.d(TAG, "recognizer result：" + resultStr);
//                    Toast.makeText(Main3Activity.this, resultStr, Toast.LENGTH_LONG).show();
//                    showTip(resultStr);
//                    showResultDialog();
                    //处理resultStr
                    StrProcess strProcess = new StrProcess(resultStr);
                    String stu_id = strProcess.getStu_id();
                    String score = strProcess.getScore();
                    Log.i(TAG,"stu_id:"+stu_id);
                    Log.i(TAG,"score:"+score);

                    if(stu_id == null) {
//                        Toast.makeText(Main3Activity.this, "请重新录音", Toast.LENGTH_LONG).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                        builder.setTitle("Tip")
                                .setMessage("不符合规则，请重新录音！\r\n录音规则请参照:\"10号 90+9\"")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        //不为空，插入数据
                        IDUSTool idusTool = new IDUSTool(Main3Activity.this);
                        idusTool.insertStuMarkDB(stu_id,score);

                        //自动刷新本页面
                        finish();
                        Intent intent = new Intent(Main3Activity.this, Main3Activity.class);
                        startActivity(intent);
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
                    mIatDialog = new RecognizerDialog(Main3Activity.this, null);

//                if(mIatDialog!=null)
                    mIatDialog.setParameter(SpeechConstant.VAD_EOS, "3000");
//                mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

                    mIatDialog.setListener(mRecognizerDialogListener);

                    mIatDialog.show();
                }
            }
        });

        initStudent();
        RecyclerView recyclerView = findViewById(R.id.Recycler_View_Student);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        StudentAdapter studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);
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

    private void showResultDialog() {
//        View view = LayoutInflater.from(Main3Activity.this).inflate(R.layout.)
    }

    private void initStudent() {
        String sqlSelect="SELECT Student.stu_id,Student.stu_name,Student.stu_gender,StudentMark.score FROM Student LEFT JOIN StudentMark ON Student.stu_id = StudentMark.stu_id";
        //扫描数据库，将信息放入markList
//        MyDatabaseHelper mdb = new MyDatabaseHelper(this, "Student.db", null, 2);//打开数据库
//        SQLiteDatabase sd = mdb.getReadableDatabase();//获取数据库
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(Main3Activity.this);
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});
        while(cursor.moveToNext()){
            String stu_id = cursor.getString(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
            String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
            String score = cursor.getString(cursor.getColumnIndex("score"));

            Student student = new Student(stu_id, stu_name, stu_gender, score);
            studentList.add(student);
        }
        cursor.close();
    }

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
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
        if (pgs.equals("rpl")) {
            Log.d(this.getClass().getName(), "recognizer result replace：" + results.getResultString());
//            Regex regex = new Regex(",");
            String[] strings = rg.replace("[", "").replace("]", "").split(",");

//            val strings = rg!!.replace("[", "").replace("]", "").split(",".toRegex())
//                    .dropLastWhile { it.isEmpty() }
//                .toTypedArray()
            int begin = Integer.parseInt(strings[0]);
            int end = Integer.parseInt(strings[1]);
//            for (i in begin..end) {
//                mIatResults.remove(i.toString() + "");
//            }
            int i = begin;
            if (begin <= end) {
                while(true) {
                    this.mIatResults.remove(i + "");
                    if (i == end) {
                        break;
                    }
                    ++i;
                }
            }
        }

        if(sn != null) mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();

        Iterator iterator = mIatResults.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            resultBuffer.append(mIatResults.get(key));
        }

//        for (key in mIatResults.keys) {
//            resultBuffer.append(mIatResults.get(key));
//        }

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
