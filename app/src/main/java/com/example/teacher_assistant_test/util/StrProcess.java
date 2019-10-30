package com.example.teacher_assistant_test.util;
//本类为语音结果字符串的处理工具类，可以获得学号stu_id和成绩score
public class StrProcess {
    private String resultStr;
    private String stu_id;
    private String score;

    public StrProcess(String resultStr) {
        this.resultStr = resultStr;
        preProcess();
    }

    public String getStu_id() {
        return stu_id;
    }

    public String getScore() {
        return score;
    }

    //清除所有空格及标点符号
    private void preProcess() {
        if(resultStr.contains("号") && resultStr.contains("+")) {
            String[] buff = resultStr.replaceAll(" ", "").replaceAll("\\p{P}", "").split("号");
            stu_id = buff[0];
            score = buff[1];
        } else {
            stu_id = null;
            score = null;
        }
    }
}
