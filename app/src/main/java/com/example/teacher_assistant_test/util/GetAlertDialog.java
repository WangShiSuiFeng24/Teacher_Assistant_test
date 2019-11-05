package com.example.teacher_assistant_test.util;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.View;

public class GetAlertDialog {

    public static AlertDialog getAlertDialog(Context context, String title, String message, View view, String positiveBtn, String negativeBtn  ) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(view)
                .setPositiveButton(positiveBtn, null)
                .setNegativeButton(negativeBtn, null)
                .show();
        return alertDialog;
    }
}
