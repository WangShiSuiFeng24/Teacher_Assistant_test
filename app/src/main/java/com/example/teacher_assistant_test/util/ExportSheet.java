package com.example.teacher_assistant_test.util;

import android.content.Context;
import android.os.Environment;

import com.example.teacher_assistant_test.bean.Record;
import com.example.teacher_assistant_test.bean.Student;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *  Created by AndongMing on 2019/11/27
 *
 * 传入test_name 和studentOrRecordList,导出Excel表格
 */

public class ExportSheet {

    private Context context;
    private String test_name;
    private List<?> studentOrRecordList;

    //导出Excel
    private ArrayList<ArrayList<String>> arrayListInArrayList;
    private static String[] title = {"学号", "姓名", "性别", "成绩", "总成绩"};
    private File file;
    private String fileName;

    public ExportSheet(Context context, String test_name, List<?> studentOrRecordList) {
        this.context = context;
        this.test_name = test_name;
        this.studentOrRecordList = studentOrRecordList;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * 导出Excel
     * @param
     */
    public void exportSheet() {
        file = new File(getSDPath() + "/Record");
        makeDir(file);
//        ExcelUtils.initExcel(file.toString() + "/成绩表.xls", title);
//        fileName = getSDPath() + "/Record/成绩表.xls";

        ExcelUtils.initExcel(file.toString() + "/" + test_name + "成绩表.xls", title);//初始化表第一行
        fileName = getSDPath() + "/Record/" + test_name + "成绩表.xls";

        ExcelUtils.writeObjListToExcel(getRecordData(), fileName, context);//将ObjList写入Excel
    }

    /**
     * 获取sd卡路径
     * @return
     */
    private String getSDPath() {
        File sdDir = null;
        //getExternalStorageState(),返回File 获取外部内存当前状态
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在，MEDIA_MOUNTED，返回getExternalStorageState() ，表明对象是否存在并具有读/写权限
        if(sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
        }

        return sdDir.toString();
    }

    private void makeDir(File dir) {
        if(!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    /**
     * 将数据集合 转化为ArrayList<ArrayList<String>>
     * @return ArrayList<ArrayList<ArrayList>>
     */
    private ArrayList<ArrayList<String>> getRecordData() {
        arrayListInArrayList = new ArrayList<>();
        for(int i=0; i<studentOrRecordList.size(); i++) {
            if(studentOrRecordList.get(i) instanceof Student) {
                Student student = (Student) studentOrRecordList.get(i);
                ArrayList<String> beanList = new ArrayList<>();
                beanList.add(String.valueOf(student.getStu_id()));
                beanList.add(student.getStu_name());
                beanList.add(student.getStu_gender());
                beanList.add(student.getScore());
                beanList.add(String.valueOf(student.getTotal_score()));
                arrayListInArrayList.add(beanList);
            }
            if(studentOrRecordList.get(i) instanceof Record) {
                Record record = (Record) studentOrRecordList.get(i);
                ArrayList<String> beanList = new ArrayList<>();
                beanList.add(String.valueOf(record.getStu_id()));
                beanList.add(record.getStu_name());
                beanList.add(record.getStu_gender());
                beanList.add(record.getScore());
                beanList.add(String.valueOf(record.getTotal_score()));
                arrayListInArrayList.add(beanList);
            }

        }
        return arrayListInArrayList;
    }
}
