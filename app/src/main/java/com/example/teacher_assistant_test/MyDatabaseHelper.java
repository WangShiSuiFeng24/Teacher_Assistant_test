package com.example.teacher_assistant_test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    /*integer 整型，real 浮点型，primary key 主键，autoincrement 自增长，text 文本类型，blob 二进制数，*/
    private static final String CREATE_STUDENT = "create table Student("
            + "stu_id integer primary key unique,"
            + "stu_name text,"
            + "stu_gender text)";

//    private static final String CREATE_CLASS = "create table StudentClass("
//            + "class_id integer primary key,"
//            + "class_name text)";

    private static final String CREATE_MARK = "create table StudentMark("
            + "mark_id integer primary key unique,"
            + "test_id integer,"
            + "stu_id integer,"
            + "score text,"
            + "total_score integer)";

    private static final String CREATE_TEST= "create table StudentTest("
            + "test_id integer ,"
            + "test_name text)";

    private static MyDatabaseHelper myDatabaseHelper;

    private MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getInstance(Context context) {
        if(myDatabaseHelper == null) {
            // 指定数据库名为Student.db，需修改时在此修改；此处使用默认工厂；指定版本为2
            myDatabaseHelper = new MyDatabaseHelper(context, "Student.db", null, 2);
        }
        return myDatabaseHelper.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STUDENT);
//        db.execSQL(CREATE_CLASS);
        db.execSQL(CREATE_MARK);
        db.execSQL(CREATE_TEST);

        Log.d("MyDatabaseHelper", "create success!");
//        Toast.makeText(mContext,"create success!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Student");
//        db.execSQL("drop table if exists StudentClass");
        db.execSQL("drop table if exists StudentMark");
        db.execSQL("drop table if exists StudentTest");
        onCreate(db);
    }
}
