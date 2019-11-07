package com.example.teacher_assistant_test.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<Student> studentList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView stu_id;
        private TextView stu_name;
        private TextView stu_gender;
        private EditText score;
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

    public StudentAdapter(List<Student> studentList) { this.studentList = studentList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.score.setTag(position);

        Student student = studentList.get(position);
        holder.stu_id.setText(String.valueOf(student.getStu_id()));
        holder.stu_name.setText(student.getStu_name());
        holder.stu_gender.setText(student.getStu_gender());
        holder.score.setText(student.getScore());
        holder.total_score.setText(String.valueOf(student.getTotal_score()));

        holder.score.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Integer.parseInt(holder.score.getTag().toString()) == position) {
                    //设置Tag解决错乱问题
                    onScoreFillListener.onScoreFill(position, s.toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    private OnScoreFillListener onScoreFillListener;

    public interface OnScoreFillListener {
        void onScoreFill(int position, String score);
    }

    public void setOnScoreFillListener(OnScoreFillListener onScoreFillListener) {
        this.onScoreFillListener = onScoreFillListener;
    }
}
