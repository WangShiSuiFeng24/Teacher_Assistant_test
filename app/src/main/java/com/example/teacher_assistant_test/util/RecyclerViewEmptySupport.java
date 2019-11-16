package com.example.teacher_assistant_test.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 用来演示如何在Recyclerview里面添加setEmptyView
 */
public class RecyclerViewEmptySupport extends RecyclerView {
    private static final String TAG = "RecyclerViewEmptySupport";

    /**
     * 当数据为空时展示的View
     */
    private View mEmptyView;

    /**
     * 创建一个观察者
     * *为什么要在onChanged里面写？
     * *因为每次notifyDataChanged的时候，系统都会调用这个观察者的onChange方法
     * *我们大可以在这个观察者这里判断我们的逻辑，就是显示隐藏
     */
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @SuppressLint("LongLogTag")
        @Override
        public void onChanged() {
            Log.i(TAG, "onChanged：000");
            Adapter<?> adapter = getAdapter();//这种写法和ListView的是一样的，判断数据为空否，再进行显示或隐藏
            if(adapter != null && mEmptyView != null) {
                if(adapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     *
     * @param emptyView 展示的空view
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        Log.i(TAG, "setAdapter: adapter::" +adapter);
        if(adapter != null) {
            //这里用了观察者模式，同时把这个观察者添加进去
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        //当setAdapter的时候也调一次
        emptyObserver.onChanged();
    }
}
