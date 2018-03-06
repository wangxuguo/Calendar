package com.oceansky.teacher.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.oceansky.teacher.R;


/**
 * Created by dengfa on 16/3/15.
 * Des: ${TODO}
 */
public class PhotoDialog extends Dialog implements View.OnClickListener {

    private Context                mContext;
    private OnCustomDialogListener mCustomDialogListener;

    public PhotoDialog(Context context, OnCustomDialogListener customDialogListener) {
        super(context);
        mContext = context;
        this.mCustomDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_photo_select_methods);

        findViewById(R.id.dialog_select_photo).setOnClickListener(this);
        findViewById(R.id.dialog_take_photo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_select_photo:
                mCustomDialogListener.back(R.id.dialog_select_photo);
                PhotoDialog.this.dismiss();
                break;

            case R.id.dialog_take_photo:
                mCustomDialogListener.back(R.id.dialog_take_photo);
                PhotoDialog.this.dismiss();
                break;
        }
    }

    public interface OnCustomDialogListener {
        void back(int item);
    }
}

