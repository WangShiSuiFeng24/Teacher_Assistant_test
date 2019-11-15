package com.example.teacher_assistant_test.util;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.teacher_assistant_test.R;

import java.lang.reflect.Field;

public class GetAlertDialog {

    public static AlertDialog getAlertDialog(Context context, String title, String message, View view, String positiveBtn, String negativeBtn  ) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(view)
                .setPositiveButton(positiveBtn, null)
                .setNegativeButton(negativeBtn, null)
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));

        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);

            //通过反射修改title字体大小和颜色
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(25);
            mTitleView.setTextColor(context.getResources().getColor(R.color.colorAccent));

            //通过反射修改message字体大小和颜色
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextSize(20);
//            mMessageView.setTextColor(Color.GREEN);

        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        }


        return alertDialog;
    }
}
