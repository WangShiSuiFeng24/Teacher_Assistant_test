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
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Record;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Record> mList;

    //默认isShowGender为false
    private boolean isShowGender = false;

    //默认editMode为RECORD_MODE_CHECK=0
    private static final int RECORD_MODE_CHECK = 0;
    int editMode = RECORD_MODE_CHECK;

    private boolean isSelectionsGone = false;

//    //editText的焦点，我们可以通过一个int变量记录他在adapter中的位置
//    int stuIdFocusPos = -1;
//    int scoreFocusPos = -1;

    //普通布局的type
    static final int TYPE_ITEM = 0;
    //脚布局
    static final int TYPE_FOOTER = 1;


    private String beforeStuId;
    private String beforeScore;

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText student_id;
        EditText student_score;
        TextView total_score;

        TextView student_name;
        TextView student_gender;

        ImageView correct;

        ImageView checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            student_id = itemView.findViewById(R.id.student_id);
            student_score = itemView.findViewById(R.id.student_score);

            student_score.setRawInputType(InputType.TYPE_CLASS_NUMBER);

            total_score = itemView.findViewById(R.id.total_score);

            student_name = itemView.findViewById(R.id.student_name);
            student_gender = itemView.findViewById(R.id.student_gender);

            correct = itemView.findViewById(R.id.correct);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        TextView statistical_info;

        public FootViewHolder(@NonNull View itemView) {
            super(itemView);
            statistical_info = itemView.findViewById(R.id.statistical_info);
        }
    }

    public RecordAdapter(List<Record> mList) {
        this.mList = mList;
        setHasStableIds(true);
    }

    public void remove(int i) {
        mList.remove(i);
        notifyItemRemoved(i);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{  //自定义接口回调设置点击事件
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    private RecordAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(RecordAdapter.OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
////        ViewHolder holder = new ViewHolder(view);
////        return holder;
//        return new RecordAdapter.ViewHolder(view);


        if (viewType == TYPE_ITEM){  //如果为普通布局
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
            return new ViewHolder(view);
        }else if (viewType == TYPE_FOOTER){  //脚布局
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view, parent, false);
            //自己定义FoodViewHolder
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
                    onFooterClickListener.onFooterClick(position);
                }
            });

            return;
        }

        ViewHolder holder = (ViewHolder) recyclerViewHolder;


        //当前holder的position
//        final int currentPosition = position;

        holder.student_id.setTag(position);
        holder.student_score.setTag(position);

        Record record = mList.get(position);
        holder.student_id.setText(record.getStu_id());
        holder.student_score.setText(record.getScore());
        holder.total_score.setText(String.valueOf(record.getTotal_score()));

        holder.student_name.setText(record.getStu_name());
        holder.student_gender.setText(record.getStu_gender());

        if (isShowGender) {
            holder.student_gender.setVisibility(View.VISIBLE);
        } else {
            holder.student_gender.setVisibility(View.GONE);
        }

        //根据adapter.setEditMode(int editMode)传入的editMode设置checkBox是否可见，默认不可见
        if(editMode == RECORD_MODE_CHECK) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);

            //根据isSelect()选中状态设置checkBox图片样式
            if(record.isSelect()) {
                holder.checkBox.setImageResource(R.mipmap.ic_checked);
            } else {
                holder.checkBox.setImageResource(R.mipmap.ic_uncheck);
            }
        }

        ///根据isCorrect()订正状态设置订正图片样式
        if (record.isCorrect()) {
            holder.correct.setImageResource(R.drawable.ic_light_star);
        } else {
            holder.correct.setImageResource(R.drawable.ic_dark_star);
        }

        //设置EditText光标模式，isSelectionsGone为true则清除光标
        if (isSelectionsGone) {
            holder.student_id.clearFocus();
            holder.student_score.clearFocus();
            isSelectionsGone = false;
        }


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

        holder.student_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeStuId = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Integer.parseInt(holder.student_id.getTag().toString()) == position) {
                    //设置Tag解决错乱问题
                    onStuIdFillListener.onStuIdFill(position, s.toString());
                }
            }
        });


        holder.student_score.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    beforeScore = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Integer.parseInt(holder.student_score.getTag().toString()) == position) {
                    onScoreFillListener.onScoreFill(position, s.toString());
                    holder.student_score.setSelection(s.length());
                }
            }
        });

        holder.correct.setOnClickListener(new View.OnClickListener() {
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

    //设置编辑模式，编辑或取消？
    public void setEditMode(int editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    //设置EditText光标模式，isSelectionsGone为true则清除光标
    public void setIsSelectionsGone(boolean isSelectionsGone) {
        this.isSelectionsGone = isSelectionsGone;
        notifyDataSetChanged();
    }

    private RecordAdapter.OnStuIdFillListener onStuIdFillListener;
    private RecordAdapter.OnScoreFillListener onScoreFillListener;

    public interface OnStuIdFillListener {
        void onStuIdFill(int position, String stu_id);
    }

    public interface OnScoreFillListener {
        void onScoreFill(int position, String score);
    }

    public void setOnStuIdFillListener(RecordAdapter.OnStuIdFillListener onStuIdFillListener) {
        this.onStuIdFillListener = onStuIdFillListener;
    }

    public void setOnScoreFillListener(RecordAdapter.OnScoreFillListener onScoreFillListener) {
        this.onScoreFillListener = onScoreFillListener;
    }

    private RecordAdapter.OnStarClickListener onStarClickListener;

    public interface OnStarClickListener {
        void onStarClick(int position);
    }

    public void setOnStarClickListener(RecordAdapter.OnStarClickListener onStarClickListener) {
        this.onStarClickListener = onStarClickListener;
    }

    private RecordAdapter.OnFooterClickListener onFooterClickListener;

    public interface OnFooterClickListener {
        void onFooterClick(int position);
    }

    public void setOnFooterClickListener(RecordAdapter.OnFooterClickListener onFooterClickListener) {
        this.onFooterClickListener = onFooterClickListener;
    }


    @Override
    public int getItemCount() {
//        return mList.size();
        //若mList为空，不显示脚布局，返回0
        if (mList.size() == 0) {
            return 0;
        }

        //mList不为空，添加脚布局，返回mList.size() + 1
        return mList.size() + 1; //返回数据项+1（加上脚布局）
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        //在这里进行判断，如果位置为mList长度+1，且mList不为空，则返回TYPE_FOOTER
        if(position == mList.size() && mList.size() != 0) {
            return TYPE_FOOTER;
        }
        //否则使用ITEM的布局
        return TYPE_ITEM;
    }
}
