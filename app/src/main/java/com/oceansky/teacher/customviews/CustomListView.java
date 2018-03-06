package com.oceansky.teacher.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * User: dengfa
 * Date: 16/6/17
 * Tel:  18500234565
 */
public class CustomListView extends ListView {

    private float             mStartY;
    private float             mEndY;
    private float             mDiffY;
    private OnScrollYListener onScrollYListener;

    public CustomListView(Context context) {
        this(context,null);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mEndY = ev.getY();
                mDiffY = mEndY - mStartY;
                if (onScrollYListener != null) {
                    onScrollYListener.onScroll(mDiffY);
                }
                break;
        }
        mStartY = mEndY;
        return super.onTouchEvent(ev);
    }

    public interface OnScrollYListener {
        void onScroll(float scrollY);
    }

    public void setOnScrollYListener(OnScrollYListener onScrollYListener) {
        this.onScrollYListener = onScrollYListener;
    }

}
