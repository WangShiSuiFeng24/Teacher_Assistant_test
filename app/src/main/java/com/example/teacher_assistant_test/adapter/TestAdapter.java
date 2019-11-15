package com.example.teacher_assistant_test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.Test;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private List<Test> mList;

    static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView test_id;
        TextView test_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            test_id = itemView.findViewById(R.id.test_id);
            test_name = itemView.findViewById(R.id.test_name);
        }
    }

    public TestAdapter(List<Test> mList) {this.mList = mList;}

    public void remove(int i) {
        mList.remove(i);
        notifyItemRemoved(i);
        notifyDataSetChanged();
    }

    //自定义接口回调设置点击事件
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item, parent, false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Test test = mList.get(position);
//        holder.test_id.setText(String.valueOf(test.getTest_id()));
        holder.test_name.setText(test.getTest_name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                mOnItemClickListener.onItemClick(ps);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int ps = holder.getLayoutPosition();
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
