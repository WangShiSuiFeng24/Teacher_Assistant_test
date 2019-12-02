package com.example.teacher_assistant_test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.StudentInfo;

import java.util.List;

public class StudentInfoAdapter extends RecyclerView.Adapter<StudentInfoAdapter.ViewHolder> {
    private List<StudentInfo> studentInfoList;

    //默认editMode为RECORD_MODE_CHECK=0
    private static final int RECORD_MODE_CHECK = 0;
    int editMode = RECORD_MODE_CHECK;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView stu_id;
        private TextView stu_name;
        private TextView stu_gender;

        ImageView checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stu_id = itemView.findViewById(R.id.student_info_id);
            stu_name = itemView.findViewById(R.id.student_info_name);
            stu_gender = itemView.findViewById(R.id.student_info_gender);

            checkBox = itemView.findViewById(R.id.check_box);
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        StudentInfo studentInfo = studentInfoList.get(position);
        holder.stu_id.setText(String.valueOf(studentInfo.getStu_id()));
        holder.stu_name.setText(studentInfo.getStu_name());
        holder.stu_gender.setText(studentInfo.getStu_gender());

        //根据adapter.setEditMode(int editMode)传入的editMode设置checkBox是否可见，默认不可见
        if(editMode == RECORD_MODE_CHECK) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);

            //根据isSelect()选中状态设置checkBox图片样式
            if(studentInfo.isSelect()) {
                holder.checkBox.setImageResource(R.mipmap.ic_checked);
            } else {
                holder.checkBox.setImageResource(R.mipmap.ic_uncheck);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                onItemClickListener.onItemClick(position);
            }
        });
    }

    //设置编辑模式，编辑或取消？
    public void setEditMode(int editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return studentInfoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private StudentInfoAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(StudentInfoAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
