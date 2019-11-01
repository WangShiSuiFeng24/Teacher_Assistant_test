package com.example.teacher_assistant_test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<Student> studentList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView stu_id;
        private TextView stu_name;
        private TextView stu_gender;
        private TextView score;
        private TextView total_score;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stu_id = itemView.findViewById(R.id.stu_id);
            stu_name = itemView.findViewById(R.id.stu_name);
            stu_gender = itemView.findViewById(R.id.stu_gender);
            score = itemView.findViewById(R.id.stu_score);
            total_score = itemView.findViewById(R.id.stu_total_score);
        }
    }

    public StudentAdapter(List<Student> studentList) {this.studentList = studentList;}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.stu_id.setText(String.valueOf(student.getStu_id()));
        holder.stu_name.setText(student.getStu_name());
        holder.stu_gender.setText(student.getStu_gender());
        holder.score.setText(student.getScore());
        holder.total_score.setText(String.valueOf(student.getTotal_score()));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}
