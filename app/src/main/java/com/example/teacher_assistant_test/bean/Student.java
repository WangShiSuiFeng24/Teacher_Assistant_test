package com.example.teacher_assistant_test.bean;

import com.example.teacher_assistant_test.util.Calculator;

public class Student {
    private int stu_id;
    private String stu_name;
    private String stu_gender;
    private String test_name;
    private String score;
    private int total_score;

    public Student(int stu_id, String stu_name, String stu_gender, String test_name, String score, int total_score) {
        this.stu_id = stu_id;
        this.stu_name = stu_name;
        this.stu_gender = stu_gender;
        this.test_name = test_name;
        this.score = score;
        this.total_score = total_score;
    }

    public int getStu_id() {
        return stu_id;
    }
    public String getStu_name() {
        return stu_name;
    }
    public String getStu_gender() {
        return stu_gender;
    }
    public String getTest_name() { return test_name; }
    public String getScore() {
        return score;
    }
    public int getTotal_score() { return total_score; }
}