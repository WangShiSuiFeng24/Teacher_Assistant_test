package com.example.teacher_assistant_test;

/**
 * 用于消息（事件）传输的实体类
 * Created by Andong Ming on 2019.12.24.
 */
public class MyEvent {
    private int test_full_mark;
    private String test_time;

    public MyEvent(int test_full_mark, String test_time) {
        this.test_full_mark = test_full_mark;
        this.test_time = test_time;
    }

    public int getTest_full_mark() {
        return test_full_mark;
    }

    public String getTest_time() {
        return test_time;
    }
}
