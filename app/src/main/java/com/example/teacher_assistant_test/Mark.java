package com.example.teacher_assistant_test;

public class Mark {
    private int stu_id;
    private double score;

    public Mark(int stu_id, double score) {
        this.stu_id = stu_id;
        this.score = score;
    }

    public int getStu_id() {
        return stu_id;
    }

    public double getScore() {
        return score;
    }
}
