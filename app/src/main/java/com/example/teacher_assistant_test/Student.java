package com.example.teacher_assistant_test;

public class Student {
    private String stu_id;
    private String stu_name;
    private String stu_gender;
    private String score;

    public Student(String stu_id, String stu_name, String stu_gender, String score) {
        this.stu_id = stu_id;
        this.stu_name = stu_name;
        this.stu_gender = stu_gender;
        this.score = score;
    }

    public String getStu_id() {
        return stu_id;
    }
    public String getStu_name() {
        return stu_name;
    }
    public String getStu_gender() {
        return stu_gender;
    }
    public String getScore() {
        return score;
    }
}
