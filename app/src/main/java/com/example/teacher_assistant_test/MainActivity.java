package com.example.teacher_assistant_test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.teacher_assistant_test.adapter.TestAdapter;
import com.example.teacher_assistant_test.bean.Test;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.StrProcess;
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

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private List<Test> testList = new ArrayList<>();
    TestAdapter testAdapter;

    private Button fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏
        setContentView(R.layout.activity_main);

//        initTest();

        initDataBase();

        RecyclerView recyclerView = findViewById(R.id.Recycler_View_Test);

        //添加自定义分割线
        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
        recyclerView.addItemDecoration(divider);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        testAdapter = new TestAdapter(testList);
        recyclerView.setAdapter(testAdapter);

        testAdapter.setOnItemClickListener(new TestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //单击则查询
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                Test test = testList.get(position);
                Log.i("MainActivity", "test_id:"+test.getTest_id());
                intent.putExtra("test_id", test.getTest_id());
                intent.putExtra("test_name", test.getTest_name());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(final int position) {
                //长按则删除
                final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(MainActivity.this,
                        "Alarm", "将要删除TEST为:"+testList.get(position).getTest_name()+"条目",
                        null, "Yes", "No");

                alertDialog.setCancelable(false);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        testAdapter.remove(position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted

                    //跳转到语音识别录成绩界面
                    Intent intent = new Intent(MainActivity.this, Main3Activity.class);
                    startActivity(intent);
                }
            }
        });
    }

    //小技巧是把需要更新的view 可以都放在initview（）中，在resume中调用这个方法即可。
    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume()");
        super.onResume();
        //先清空testList
        testList.clear();
        //再重新add数据到testList
        initTest();
        //通知RecyclerView，告诉它Adapter的数据发生了变化
        testAdapter.notifyDataSetChanged();
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

    private void initDataBase() {
        IDUSTool idusTool = new IDUSTool(MainActivity.this);
        idusTool.insertStuDB("1", "Dan", "girl");
        idusTool.insertStuDB("2", "Jow", "boy");
        idusTool.insertStuDB("3", "Marry", "girl");
        idusTool.insertStuDB("4", "Jorge", "boy");
        idusTool.insertStuDB("5", "Judy", "girl");
        for (int i=1; i<=99; i++) {
            idusTool.insertStuDB(""+i+"", "", "");
        }
    }
}
