package com.example.teacher_assistant_test.bean;

public class Test {
    private long test_id;
    private String test_name;
    private int test_full_mark;

    public Test(long test_id, String test_name, int test_full_mark) {
        this.test_id = test_id;
        this.test_name = test_name;
        this.test_full_mark = test_full_mark;
    }

    public long getTest_id() {return test_id;}

//    public void setTest_id() {this.test_id = test_id;}

    public String getTest_name() {return test_name;}

    public void setTest_name(String test_name) {this.test_name = test_name;}

    public int getTest_full_mark() { return test_full_mark; }
    public void setTest_full_mark(int test_full_mark) { this.test_full_mark = test_full_mark; }
}
