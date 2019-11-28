package com.example.teacher_assistant_test.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.teacher_assistant_test.R;


public class TitleBarView extends RelativeLayout {
    private LinearLayout layout_left, layout_right;
    private TextView tv_left, tv_title, tv_right;
    private ImageView iv_left, iv_right;
    private onViewClick mClick;

    public TitleBarView(Context context) {
        this(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBarView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_title, this);
        initView();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.TitleBarView_leftTextColor:
                    tv_left.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.TitleBarView_leftDrawable:
                    iv_left.setImageResource(array.getResourceId(attr, 0));
                    break;
                case R.styleable.TitleBarView_leftText:
                    tv_left.setText(array.getString(attr));
                    break;
                case R.styleable.TitleBarView_centerTextColor:
                    tv_title.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.TitleBarView_centerTitle:
                    tv_title.setText(array.getString(attr));
                    break;
                case R.styleable.TitleBarView_rightDrawable:
                    iv_right.setImageResource(array.getResourceId(attr, 0));
                    break;
                case R.styleable.TitleBarView_rightText:
                    tv_right.setText(array.getString(attr));
                    break;
                case R.styleable.TitleBarView_rightTextColor:
                    tv_right.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
            }
        }
        array.recycle();//回收TypeArray
        layout_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mClick.leftClick();
            }
        });
//        layout_right.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mClick.rightClick();
//            }
//        });
        tv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.tvRightClick();
            }
        });

        iv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.ivRightClick(v);
            }
        });

    }

    private void initView() {
        tv_left = findViewById(R.id.tv_left);
        tv_title = findViewById(R.id.tv_title);
        tv_right = findViewById(R.id.tv_right);
        iv_left = findViewById(R.id.iv_left);
        iv_right = findViewById(R.id.iv_right);
        layout_left = findViewById(R.id.layout_left);
        layout_right = findViewById(R.id.layout_right);
    }

    public void setOnViewClick(onViewClick click) {
        this.mClick = click;
    }

    //设置标题
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
        }
    }

    //设置左标题
    public void setLeftText(String title) {
//        if (!TextUtils.isEmpty(title)) {
            tv_left.setText(title);
//        }
    }

    //设置右标题
    public void setRightText(String title) {
//        if (!TextUtils.isEmpty(title)) {
            tv_right.setText(title);
//        }
    }

    //设置标题大小
    public void setTitleSize(int size) {
        if (tv_title != null) {
            tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置左标题大小
    public void setLeftTextSize(int size) {
        if (tv_left != null) {
            tv_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置左标题颜色
    public void setLeftTextColor(int color) {
        if(tv_left != null) {
            tv_left.setTextColor(color);
        }
    }

    //设置右标题大小
    public void setRightTextSize(int size) {
        if (tv_right != null) {
            tv_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置右标题颜色
    public void setRightTextColor(int color) {
        if(tv_right != null) {
            tv_right.setTextColor(color);
        }
    }

    //设置左图标
    public void setLeftDrawable(int res) {
        if (iv_left != null) {
            iv_left.setImageResource(res);
        }
    }

    //设置右图标
    public void setRightDrawable(int res) {
        if (iv_right != null) {
            iv_right.setImageResource(res);
        }
    }

    public interface onViewClick {
        void leftClick();

//        void rightClick();

        void tvRightClick();

        void ivRightClick(View view);
    }

}
