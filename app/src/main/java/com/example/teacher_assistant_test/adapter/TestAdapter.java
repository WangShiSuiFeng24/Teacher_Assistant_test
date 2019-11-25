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

public class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Test> mList;

    /**
     * viewType--分别为item以及空view
     */
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_EMPTY = 0;

    static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView test_id;
        TextView test_name;

         ViewHolder(@NonNull View itemView) {
            super(itemView);
//            test_id = itemView.findViewById(R.id.test_id);
            test_name = itemView.findViewById(R.id.test_name);
        }
    }

    public TestAdapter(List<Test> mList) { this.mList = mList; }

    public void remove(int i) {
        mList.remove(i);
        notifyItemRemoved(i);
        notifyDataSetChanged();
    }

    //自定义接口回调设置点击事件
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onItemLongClick(int position, View view);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //在这里根据不同的进行引入不同的布局
        if(viewType == VIEW_TYPE_EMPTY) {
            View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
            return new RecyclerView.ViewHolder(emptyView) {

            };
        }

        //其他的引入正常的
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item, parent, false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerViewHolder, int position) {
        if(recyclerViewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) recyclerViewHolder;
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
                    mOnItemClickListener.onItemLongClick(ps, v);
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        //同时这里也需要添加判断，如果mList.size()为0的话，只引入一个布局，就是emptyView
        //那么这个RecyclerView的itemCount为1
        if(mList.size() == 0) {
            return 1;
        }

        //如果不为0，按正常的流程跑
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //在这里进行判断，如果我们的集合长度为0时，我们就使用emptyView的布局
        if(mList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        //如果有数据，则使用ITEM的布局
        return VIEW_TYPE_ITEM;
    }
}
