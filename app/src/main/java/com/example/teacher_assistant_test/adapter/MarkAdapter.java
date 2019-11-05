package com.example.teacher_assistant_test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.Mark;

import java.util.List;

    public class MarkAdapter extends RecyclerView.Adapter<MarkAdapter.ViewHolder> {
    private List<Mark> mList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText student_id;
        EditText student_score;
        TextView total_score;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            student_id = itemView.findViewById(R.id.student_id);
            student_score = itemView.findViewById(R.id.student_score);
            total_score = itemView.findViewById(R.id.total_score);
        }
    }

    public MarkAdapter(List<Mark> mList) {this.mList = mList;}

    public void remove(int i) {
        mList.remove(i);
        notifyItemRemoved(i);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{  //自定义接口回调设置点击事件
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener=onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mark_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Mark mark = mList.get(position);
        holder.student_id.setText(mark.getStu_id());
        holder.student_score.setText(mark.getScore());
        holder.total_score.setText(String.valueOf(mark.getTotal_score()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int ps = holder.getLayoutPosition();
                mOnItemClickListener.onItemClick(ps);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                int ps=holder.getLayoutPosition();
                mOnItemClickListener.onItemLongClick(ps);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }




}
