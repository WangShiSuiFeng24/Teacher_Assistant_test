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
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.util.TitleBarView;
import com.example.teacher_assistant_test.adapter.TestAdapter;
import com.example.teacher_assistant_test.bean.Test;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.ImmersiveStatusBar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private List<Test> testList = new ArrayList<>();
    TestAdapter testAdapter;

    private ImageView fab;

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

        TitleBarView titlebarView= findViewById(R.id.title);
        titlebarView.setTitleSize(20);
        titlebarView.setTitle("园丁小帮手");
        titlebarView.setOnViewClick(new TitleBarView.onViewClick() {
            @Override
            public void leftClick() {
                //不作处理
//                Toast.makeText(MainActivity.this,"左边", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void tvRightClick() {
                //不作处理
//                Toast.makeText(MainActivity.this,"右边",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void ivRightClick(View view) {
                //不作处理
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.show();
            }
        });

//        initTest();
        importSheet();
//        initDataBase();

        RecyclerView recyclerView = findViewById(R.id.Recycler_View_Test);

        //添加自定义分割线
//        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
//        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
//        recyclerView.addItemDecoration(divider);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        testAdapter = new TestAdapter(testList);
        recyclerView.setAdapter(testAdapter);
        //添加Android自带的分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        testAdapter.setOnItemClickListener(new TestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //单击则查询
                Test test = testList.get(position);
                ShowAndEditActivity.actionStart(MainActivity.this, test.getTest_id(), test.getTest_name());

//                Intent intent = new Intent(MainActivity.this, ShowAndEditActivity.class);
//                Test test = testList.get(position);
//                Log.i("MainActivity", "test_id:"+test.getTest_id());
//                intent.putExtra("test_id", test.getTest_id());
//                intent.putExtra("test_name", test.getTest_name());
//                startActivity(intent);
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
                //先判断权限是否授予，没有则单独申请，有则跳转
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                    request_permissions(MainActivity.this);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    RecordMarkActivity.actionStart(MainActivity.this);
                }
            }
        });

        request_permissions(this);
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

    //返回刷新，小技巧是把需要更新的view 可以都放在initview（）中，在resume中调用这个方法即可。
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

    public boolean editStudentInfo(MenuItem item) {
        EditStudentInfoActivity.actionStart(this);
        return true;
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



//    private void initDataBase() {
//        IDUSTool idusTool = new IDUSTool(MainActivity.this);
//        idusTool.insertStuDB("1", "Dan", "girl");
//        idusTool.insertStuDB("2", "Jow", "boy");
//        idusTool.insertStuDB("3", "Marry", "girl");
//        idusTool.insertStuDB("4", "Jorge", "boy");
//        idusTool.insertStuDB("5", "Judy", "girl");
//        for (int i=1; i<=99; i++) {
//            idusTool.insertStuDB(""+i+"", "", "");
//        }
//    }
//
}
