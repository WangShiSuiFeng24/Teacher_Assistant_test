package com.example.teacher_assistant_test;

public class Mark {
    private int stu_id;
    private String score;
    private int total_score;

    public Mark(int stu_id, String score, int total_score) {
        this.stu_id = stu_id;
        this.score = score;
        this.total_score = total_score;
    }

    public int getStu_id() {
        return stu_id;
    }

    public String getScore() {
        return score;
    }

    public int getTotal_score() {return total_score;}
}
