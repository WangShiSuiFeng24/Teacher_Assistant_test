package com.example.teacher_assistant_test.bean;

import java.io.Serializable;

/**
 * Created by Andong Ming on 2019.12.26.
 */
public class NameAndScore implements Serializable {

    private String stu_name;
    private int total_score;

    public NameAndScore(String name, int total_score) {
        this.stu_name = name;
        this.total_score = total_score;
    }

    public void setStu_name(String name) { this.stu_name = name; }
    public String getStu_name() { return stu_name; }

    public void setTotal_score(int total_score) { this.total_score = total_score; }
    public int getTotal_score() { return total_score; }
}
