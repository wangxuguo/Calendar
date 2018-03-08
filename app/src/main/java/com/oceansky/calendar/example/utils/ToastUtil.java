package com.oceansky.calendar.example.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.oceansky.calendar.example.R;

/**
 * copy..
 */
public class ToastUtil {

    private static Toast mToast;

    public static void showToastMiddle(Context context, String msg, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, duration);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    public static void showToastBottom(Context context, int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, duration);
        }
        int yOffset = context.getResources().getDimensionPixelSize(R.dimen.toast_y_offset);
        mToast.setText(resId);
        mToast.setGravity(Gravity.BOTTOM, 0, yOffset);
        mToast.show();
    }

    public static void showToastBottom(Context context, String toastMsg, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, toastMsg, duration);
        }
        int yOffset = context.getResources().getDimensionPixelSize(R.dimen.toast_y_offset);
        mToast.setText(toastMsg);
        mToast.setGravity(Gravity.BOTTOM, 0, yOffset);
        mToast.show();
    }
}
