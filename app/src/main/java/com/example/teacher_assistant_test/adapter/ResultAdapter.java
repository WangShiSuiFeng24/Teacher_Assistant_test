package com.example.teacher_assistant_test.adapter;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.bean.Result;

import java.util.List;

/**
 * Created by Andong Ming on 2019.12.10.
 */
public class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Result> mList;

    //普通布局的type
    static final int TYPE_ITEM = 0;
    //脚布局
    static final int TYPE_FOOTER = 1;

    //默认isShowGender为false
    private boolean isShowGender = false;

    //默认isRank为true
    private boolean isRank = false;

    //默认isSelectionsGone为false
    private boolean isSelectionsGone = false;


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stu_id;
        TextView stu_name;
        TextView stu_gender;

        EditText stu_score;

        TextView rank_score;

        TextView stu_total_score;

        ImageView stu_correct;

        ImageView stu_checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stu_id = itemView.findViewById(R.id.stu_id);
            stu_name = itemView.findViewById(R.id.stu_name);
            stu_gender = itemView.findViewById(R.id.stu_gender);

            stu_score = itemView.findViewById(R.id.stu_score);
            stu_score.setRawInputType(InputType.TYPE_CLASS_NUMBER);

            rank_score = itemView.findViewById(R.id.rank_score);

            stu_total_score = itemView.findViewById(R.id.stu_total_score);

            stu_correct = itemView.findViewById(R.id.stu_correct);
            stu_checkBox = itemView.findViewById(R.id.stu_check_box);
        }
    }

    static class FootViewHolder extends  RecyclerView.ViewHolder {
        TextView statistical_info;
        public FootViewHolder(@NonNull View itemView) {
            super(itemView);
            statistical_info = itemView.findViewById(R.id.statistical_info);
        }
    }

    public ResultAdapter(List<Result> mList) {
        this.mList = mList;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerViewHolder, int position) {

        if (recyclerViewHolder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) recyclerViewHolder;

            footViewHolder.statistical_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFooterClickListener.onFooterClick();
                }
            });
            return;
        }

        ViewHolder holder = (ViewHolder) recyclerViewHolder;

        holder.stu_score.setTag(position);

        Result result = mList.get(position);

        holder.stu_id.setText(result.getStu_id());
        holder.stu_name.setText(result.getStu_name());
        holder.stu_gender.setText(result.getStu_gender());
        holder.stu_score.setText(result.getScore());

        holder.rank_score.setText(result.getScore());

        holder.stu_total_score.setText(String.valueOf(result.getTotal_score()));

        if (isShowGender) {
            holder.stu_gender.setVisibility(View.VISIBLE);
        } else {
            holder.stu_gender.setVisibility(View.GONE);
        }

        if (!isRank) {
            holder.stu_score.setVisibility(View.VISIBLE);
            holder.stu_total_score.setVisibility(View.VISIBLE);
            holder.rank_score.setVisibility(View.GONE);
        } else {
            holder.stu_score.setVisibility(View.GONE);
            holder.stu_total_score.setVisibility(View.GONE);
            holder.rank_score.setVisibility(View.VISIBLE);
        }

        //根据isCorrect()订正状态设置订正图片样式
        if (result.isCorrect()) {
            holder.stu_correct.setImageResource(R.drawable.ic_light_star);
        } else {
            holder.stu_correct.setImageResource(R.drawable.ic_dark_star);
        }

        //设置EditText光标模式，isSelectionsGone为true则清除光标
        if (isSelectionsGone) {
            holder.stu_score.clearFocus();
            isSelectionsGone = false;
        }

        if (!isRank) {
            holder.stu_score.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Integer.parseInt(holder.stu_score.getTag().toString()) == position) {

                        onScoreFillListener.onScoreFill(position, s.toString());

                        holder.stu_score.setSelection(s.length());
                    }
                }
            });
        } else {
            holder.rank_score.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    onScoreClickListener.onScoreClick(pos);
                }
            });

        }


        holder.stu_correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                onStarClickListener.onStarClick(pos);
            }
        });

    }

    //设置isShowGender
    public void setIsShowGender(boolean isShowGender) {
        this.isShowGender = isShowGender;
        notifyDataSetChanged();
    }

    //设置isShowTotalScore
    public void setRankMode(boolean isRank) {
        this.isRank = isRank;
        notifyDataSetChanged();
    }

    //设置EditText光标模式，isSelectionsGone为true则清除光标
    public void setIsSelectionsGone(boolean isSelectionsGone) {
        this.isSelectionsGone = isSelectionsGone;
        notifyDataSetChanged();
    }


    //score 编辑框点击事件
    private ResultAdapter.OnScoreClickListener onScoreClickListener;

    public interface OnScoreClickListener {
        void onScoreClick(int position);
    }

    public void setOnScoreClickListener(ResultAdapter.OnScoreClickListener onScoreClickListener) {
        this.onScoreClickListener = onScoreClickListener;
    }

    private ResultAdapter.OnFooterClickListener onFooterClickListener;

    public interface OnFooterClickListener {
        void onFooterClick();
    }

    public void setOnFooterClickListener(ResultAdapter.OnFooterClickListener onFooterClickListener) {
        this.onFooterClickListener = onFooterClickListener;
    }

    private ResultAdapter.OnScoreFillListener onScoreFillListener;

    public interface OnScoreFillListener {
        void onScoreFill(int position, String score);
    }

    public void setOnScoreFillListener(ResultAdapter.OnScoreFillListener onScoreFillListener) {
        this.onScoreFillListener = onScoreFillListener;
    }


    private ResultAdapter.OnStarClickListener onStarClickListener;

    public interface OnStarClickListener {
        void onStarClick(int position);
    }

    public void setOnStarClickListener(ResultAdapter.OnStarClickListener onStarClickListener) {
        this.onStarClickListener = onStarClickListener;
    }


    @Override
    public int getItemCount() {

        return mList.size() == 0 ? mList.size() : mList.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        return (mList.size() !=0 && position == mList.size()) ? TYPE_FOOTER :TYPE_ITEM;
    }
}
