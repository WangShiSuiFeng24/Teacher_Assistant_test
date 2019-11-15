package com.example.teacher_assistant_test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.StudentInfo;

import java.util.List;

public class StudentInfoAdapter extends RecyclerView.Adapter<StudentInfoAdapter.ViewHolder> {
    private List<StudentInfo> studentInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView stu_id;
        private TextView stu_name;
        private TextView stu_gender;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stu_id = itemView.findViewById(R.id.student_info_id);
            stu_name = itemView.findViewById(R.id.student_info_name);
            stu_gender = itemView.findViewById(R.id.student_info_gender);
        }
    }

    public StudentInfoAdapter(List<StudentInfo> studentInfoList) {
        this.studentInfoList = studentInfoList;
//        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_info_item, parent, false);
//        ViewHolder viewHolder = new ViewHolder(view);
//        return viewHolder;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        StudentInfo studentInfo = studentInfoList.get(position);
        holder.stu_id.setText(String.valueOf(studentInfo.getStu_id()));
        holder.stu_name.setText(studentInfo.getStu_name());
        holder.stu_gender.setText(studentInfo.getStu_gender());
    }

    @Override
    public int getItemCount() {
        return studentInfoList.size();
    }
}
