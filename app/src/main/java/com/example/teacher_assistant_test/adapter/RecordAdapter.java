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
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Record;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{
    private List<Record> mList;

//    //editText的焦点，我们可以通过一个int变量记录他在adapter中的位置
//    int stuIdFocusPos = -1;
//    int scoreFocusPos = -1;

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText student_id;
        EditText student_score;
        TextView total_score;

        TextView student_name;
        TextView student_gender;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            student_id = itemView.findViewById(R.id.student_id);
            student_score = itemView.findViewById(R.id.student_score);
            total_score = itemView.findViewById(R.id.total_score);

            student_name = itemView.findViewById(R.id.student_name);
            student_gender = itemView.findViewById(R.id.student_gender);
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

    private MarkAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(MarkAdapter.OnItemClickListener onItemClickListener){
        mOnItemClickListener=onItemClickListener;
    }

    @NonNull
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
        return new RecordAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordAdapter.ViewHolder holder, final int position) {
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


//        //当前holder是我们记录下的焦点位置时，我们给当前的editText设置焦点并设置光标位置
//        if (stuIdFocusPos == position) {
//            holder.student_id.requestFocus();
//            holder.student_id.setSelection(holder.student_id.getText().length());
//        }
//
//        //我们给当前holder中的editText添加touch事件监听，在action_up手指抬起时，记录下焦点position
//        holder.student_id.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    etFocusPos = position;
//                }
//                return false;
//            }
//        });
//
//        holder.student_score.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    etFocusPos = position;
//                }
//                return false;
//            }
//        });
    }

    private MarkAdapter.OnStuIdFillListener onStuIdFillListener;
    private MarkAdapter.OnScoreFillListener onScoreFillListener;

    public interface OnStuIdFillListener {
        void onStuIdFill(int position, String stu_id);
    }

    public interface OnScoreFillListener {
        void onScoreFill(int position, String score);
    }

    public void setOnStuIdFillListener(MarkAdapter.OnStuIdFillListener onStuIdFillListener) {
        this.onStuIdFillListener = onStuIdFillListener;
    }

    public void setOnScoreFillListener(MarkAdapter.OnScoreFillListener onScoreFillListener) {
        this.onScoreFillListener = onScoreFillListener;
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
