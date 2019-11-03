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

        String str = resultStr.replaceAll("家|加", "+").replaceAll("吧|班", "8").replaceAll("呢", "0");
        Log.i("StrProcess", "str:"+str);

        List<Mark> markList = new ArrayList<>();
        //创建模式串,正则表达式！！！
//        String pattern = "[1-9][0-9]?号[，。]{0,2}[0-9]{1,3}[\\+][0-9]{1,2}";
//        String pattern = "[1-9][0-9]?号[，。]{0,2}[0-9]{1,3}([\\+](10|[0-9]))?";
        //将resultStr粗分成每一句，若符合规则则解析，不符合规则则打印
        String pattern = "[^。，！？.,!?号]+号[^号]*[。，！？.,!?]";
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
        Log.i("StrProcess:", "list.size():"+list.size());
        for(int i=0; i<list.size(); i++) {
//            String[] buff = list.get(i).replaceAll(" ", "").replaceAll("\\p{P}", "").split("号");
            Log.i("StrProcess", "粗加工后的item:"+list.toString());

            //若符合规则则解析，不符合规则则打印
            String rulePattern = "[1-9][0-9]?号[。，！？.,!?]{0,2}[0-9]{1,3}([\\+]([0-9]{1,2}))*";
            //创建Pattern对象
            Pattern ruleR = Pattern.compile(rulePattern);
            //创建Macher对象
            Matcher ruleMatcher = ruleR.matcher(list.get(i));

            //将匹配结果存入一个List<String>
            List<String> ruleList = new ArrayList<>();
            while(ruleMatcher.find()) {
                ruleList.add(ruleMatcher.group());
            }
            if(ruleList.size()==0) {

                //不符合规则，此处打印
                Log.i("SrtProcess", "开始打印不符合规则的item:");
                String[] buff = list.get(i).split("号");

                String stu_id = buff[0];
                Log.i("SrtProcess", "不符合规则stu_id:"+stu_id);

                String score = buff[1];
                Log.i("StrProcess", "不符合规则score:"+score);

                int total_score = 0;
                Log.i("StrProcess", "不符合规则total_score:"+total_score);

                Mark mark = new Mark(stu_id, score, total_score);
                markList.add(mark);


            } else {

                //处理List<String>中的每一条String
                Log.i("SrtProcess", "开始打印符合规则的item:");
                for(int j=0; j<ruleList.size(); j++) {
                    //符合规则，此处解析
                    String[] buff = ruleList.get(j).replaceAll(" ", "").replaceAll("[，。,.]", "").split("号");
                    String stu_id = buff[0];
                    Log.i("SrtProcess", "符合规则stu_id:"+stu_id);

                    String score = buff[1];
                    Log.i("StrProcess", "符合规则score:"+score);

                    Calculator calculator = new Calculator();
                    int total_score = (int) calculator.calculate(score);
                    Log.i("StrProcess", "符合规则total_score:"+total_score);

                    Mark mark = new Mark(stu_id, score, total_score);
                    markList.add(mark);
                }
            }
        }
        return markList;
    }
}
