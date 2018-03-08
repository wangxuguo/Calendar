package com.oceansky.calendar.example.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * User: dengfa
 * Date: 16/6/21
 * Tel:  18500234565
 */
public class CLinearLayout extends LinearLayout {
    private boolean mEditable;

    public CLinearLayout(Context context) {
        super(context, null);
    }

    public CLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEditable) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEditable) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
    }
}
