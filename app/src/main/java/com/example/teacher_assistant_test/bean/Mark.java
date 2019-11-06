package com.example.teacher_assistant_test.bean;

import androidx.annotation.Nullable;

public class Mark {
//    private int mark_id;
//    private int test_id;
    private String stu_id;
    private String score;
    private int total_score;

    public Mark(String stu_id, String score, int total_score) {
//        this.mark_id = mark_id;
//        this.test_id = test_id;
        this.stu_id = stu_id;
        this.score = score;
        this.total_score = total_score;
    }


//    public int getMark_id() { return mark_id; }
//    public void setMark_id(int mark_id) { this.mark_id = mark_id; }
//
//    public int getTest_id() { return test_id; }
//    public void setTest_id(int test_id) { this.test_id = test_id; }

    public String getStu_id() { return stu_id; }
    public void setStu_id(String stu_id) { this.stu_id = stu_id; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public int getTotal_score() { return total_score; }
    public void setTotal_score(int total_score) { this.total_score = total_score; }

    //去重关键,重写equals方法
    @Override
    public boolean equals(@Nullable Object obj) {
        Mark mark = (Mark) obj;
        return mark.stu_id.equals(stu_id);
    }

    //去重关键,把User的id当做对象的哈希表值返回
    @Override
    public int hashCode() {
        return stu_id.hashCode();
    }
}
