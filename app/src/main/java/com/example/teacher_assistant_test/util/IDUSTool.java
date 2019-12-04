package com.example.teacher_assistant_test.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class IDUSTool {
    private SQLiteDatabase sqLiteDatabase;

    public IDUSTool(Context context) {
        //获取唯一实例
        sqLiteDatabase = MyDatabaseHelper.getInstance(context);
    }


    /**
     * 向sqLiteDatabase数据库中插入Student表项
     *
     * @param stu_id  学号
     * @param stu_name  姓名
     * @param stu_gender  性别
     */
    public void insertStuDB(String stu_id, String stu_name, String stu_gender) {
        //实例化常量值
        ContentValues contentValues = new ContentValues();
        //插入学号
        contentValues.put("stu_id", stu_id);
        //插入姓名
        contentValues.put("stu_name", stu_name);
        //插入性别
        contentValues.put("stu_gender", stu_gender);
        //调用insert插入数据
        sqLiteDatabase.insert("Student", null, contentValues);
    }

    /**
     * 向sqLiteDatabase数据库中插入StudentMark表项
     *
     * @param stu_id  学号
     * @param test_id 测试号
     * @param score  成绩
     * @param total_score  总成绩
     */
    public void insertStuMarkDB(String stu_id, long test_id, String score, int total_score) {
        //实例化常量值
        ContentValues contentValues = new ContentValues();
        //插入学号
        contentValues.put("stu_id", stu_id);
        //插入测试号
        contentValues.put("test_id", test_id);
        //插入成绩
        contentValues.put("score", score);
        //插入总成绩
        contentValues.put("total_score", total_score);
        //调用insert插入数据
        sqLiteDatabase.insert("StudentMark", null, contentValues);
    }

    /**
     * 向sqLiteDatabase数据库中插入StudentMark表项
     *
     * @param stu_id  学号
     * @param test_id 测试号
     * @param score  成绩
     * @param total_score  总成绩
     * @param isCorrect 订正
     */
    public void insertStuMarkDB(String stu_id, long test_id, String score, int total_score, boolean isCorrect) {
        //实例化常量值
        ContentValues contentValues = new ContentValues();
        //插入学号
        contentValues.put("stu_id", stu_id);
        //插入测试号
        contentValues.put("test_id", test_id);
        //插入成绩
        contentValues.put("score", score);
        //插入总成绩
        contentValues.put("total_score", total_score);
        //插入订正项
        contentValues.put("isCorrect", (isCorrect ? 1 : 0));
        //调用insert插入数据
        sqLiteDatabase.insert("StudentMark", null, contentValues);
    }

    public void insertStuTest(long test_id, String test_name) {
        //实例化常量值
        ContentValues contentValues = new ContentValues();
        //插入测试ID
        contentValues.put("test_id", test_id);
        //插入测试名
        contentValues.put("test_name", test_name);
        sqLiteDatabase.insert("StudentTest", null, contentValues);
    }

    /**
     * 删除数据
     *
     * @param stu_id
     */
    public void del(int stu_id) {
        sqLiteDatabase.execSQL("delete from Student where _stu_id=?", new String[] { stu_id + "" });
    }

    /**
     * 修改数据
     *
     * @param stu_name 姓名
     * @param stu_gender  性别
     * @param stu_id   学号
     */
    public void updateStu(String stu_name, String stu_gender, String stu_id) {
        sqLiteDatabase.execSQL("update Student set stu_name=?,stu_gender=? where stu_id=?",
                new Object[] { stu_name, stu_gender, stu_id });
    }

    /**
     * 修改数据
     *
     * @param score 成绩
     * @param stu_id   学号
     */
    public void updateStuMark(String score, String stu_id) {
        sqLiteDatabase.execSQL("update StudentMark set score=? where stu_id=?",
                new Object[]{score, stu_id});
    }


}
