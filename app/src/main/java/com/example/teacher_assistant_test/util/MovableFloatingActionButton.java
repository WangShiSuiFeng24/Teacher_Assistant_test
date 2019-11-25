package com.example.teacher_assistant_test.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

public class MovableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private static final float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.

    private float downRawX, downRawY;

    private float dX, dY;

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs ,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    //重写View的onTouchEvent(MotionEvent event)方法时
    // android会把触摸事件封装成MotionEvent成提供给开发者，MotionEvent提供了两组用于获取用户点击点坐标的方法：
    //getX()和getY()获取到的是相对于当前View左上角的坐标；
    //getRawX和getRawY()获取的是相对于屏幕左上角的坐标。
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int action = motionEvent.getAction();
        if(action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();

            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            return true;// Consumed
        }
        else if(action == MotionEvent.ACTION_MOVE) {

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View) view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(0, newX);// Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth, newX);// Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(0, newY);// Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight, newY);// Don't allow the FAB past the bottom of the parent

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();
            return true;// Consumed
        }
        else if(action == MotionEvent.ACTION_UP) {
            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if(Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            }
            else { // A drag
                return true; // Consumed
            }
        }
        else  {
            return super.onTouchEvent(motionEvent);
        }
    }
}