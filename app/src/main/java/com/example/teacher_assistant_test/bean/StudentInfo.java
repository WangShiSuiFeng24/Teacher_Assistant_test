package com.example.teacher_assistant_test.bean;

public class StudentInfo {
    private int stu_id;
    private String stu_name;
    private String stu_gender;

    private boolean isSelect;

    public StudentInfo() {}
    public StudentInfo(int stu_id, String stu_name, String stu_gender) {
        this.stu_id = stu_id;
        this.stu_name = stu_name;
        this.stu_gender = stu_gender;
    }

    public int getStu_id() { return stu_id; }
    public void setStu_id(int stu_id) { this.stu_id = stu_id; }

    public String getStu_name() { return stu_name; }
    public void setStu_name(String stu_name) { this.stu_name = stu_name; }

    public String getStu_gender() { return stu_gender; }
    public void setStu_gender(String stu_gender) { this.stu_gender = stu_gender; }

    public boolean isSelect() { return isSelect; }
    public void setSelect(boolean isSelect) { this.isSelect = isSelect; }

}
