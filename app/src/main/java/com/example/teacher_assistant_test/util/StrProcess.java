package com.example.teacher_assistant_test.util;

import android.util.Log;

import com.example.teacher_assistant_test.Mark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//本类为语音结果字符串的处理工具类，返回一个List<Mark>
public class StrProcess {

    private StrProcess() {}

    //使用正则表达式切割字符串resultStr成多条Mark,将多条Mark存入List<Mark>中
    public static List<Mark> StrToMarkList(String resultStr) {

        String str = resultStr.replaceAll("家|加", "+").replaceAll("吧|班", "8");
        List<Mark> markList = new ArrayList<>();
        //创建模式串,正则表达式！！！
//        String pattern = "[1-9][0-9]?号[，。]{0,2}[0-9]{1,3}[\\+][0-9]{1,2}";
        String pattern = "[1-9][0-9]?号[，。]{0,2}[0-9]{1,3}([\\+](10|[0-9]))?";
        //创建Pattern对象
        Pattern r = Pattern.compile(pattern);
        //创建Macher对象
        Matcher matcher = r.matcher(str);
//        Log.i("StrProcess", "m:"+ matcher.toString());

        //将匹配结果存入一个List<String>
        List<String> list = new ArrayList<>();
        while(matcher.find()) {
            list.add(matcher.group());
        }

        if(list.size()==0) return null;

        //处理List<String>中的每一条String
        for(int i=0; i<list.size(); i++) {
//            String[] buff = list.get(i).replaceAll(" ", "").replaceAll("\\p{P}", "").split("号");
            String[] buff = list.get(i).replaceAll(" ", "").replaceAll("[，。,.]", "").split("号");
            int stu_id = Integer.parseInt(buff[0]);
            Log.i("StrProcess","stu_id:"+stu_id);
            String score = buff[1];
            Log.i("StrProcess", "score:"+score);
            Calculator calculator = new Calculator();
            int total_score = (int) calculator.calculate(score);
            Log.i("StrProcess", "total_score:"+total_score);
            Mark mark = new Mark(stu_id, score, total_score);
            markList.add(mark);
        }
        return markList;
    }
}
