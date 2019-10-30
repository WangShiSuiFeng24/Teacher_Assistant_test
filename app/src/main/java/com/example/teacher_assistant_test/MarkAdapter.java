package com.example.teacher_assistant_test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MarkAdapter extends RecyclerView.Adapter<MarkAdapter.ViewHolder> {
    private List<Mark> mList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView student_id;
        TextView student_score;
        TextView total_score;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            student_id = itemView.findViewById(R.id.student_id);
            student_score = itemView.findViewById(R.id.student_score);
            total_score = itemView.findViewById(R.id.total_score);
        }
    }

    public MarkAdapter(List<Mark> mList) {this.mList = mList;}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mark_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mark mark = mList.get(position);
        holder.student_id.setText(String.valueOf(mark.getStu_id()));
        holder.student_score.setText(String.valueOf(mark.getScore()));
        holder.total_score.setText(String.valueOf(mark.getTotal_score()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
