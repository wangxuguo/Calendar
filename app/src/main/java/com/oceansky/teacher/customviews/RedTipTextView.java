package com.oceansky.teacher.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.utils.DisplayUtils;
import com.oceansky.teacher.utils.LogHelper;

/**
 * User: 王旭国
 * Date: 16/6/23 13:21
 * Email:wangxuguo@jhyx.com.cn
 */
public class RedTipTextView extends TextView {
    public static final int RED_TIP_INVISIBLE = 0;
    public static final int RED_TIP_VISIBLE = 1;
    public static final int RED_TIP_GONE = 2;
    private static final String TAG = RedTipTextView.class.getSimpleName();
    private int tipVisibility = 0;
    private int showRightBoundDivider = 0;
    public RedTipTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(null);
    }

    public RedTipTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(attrs);
    }

    public RedTipTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        if(attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RedTipTextView);
            showRightBoundDivider = array.getInt(R.styleable.RedTipTextView_showRightBoundDivider,1);
            tipVisibility = array.getInt(R.styleable.RedTipTextView_redTipsVisibility, 0);
            array.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if(tipVisibility == 1) {
            int width = getWidth();
            int paddingRight = getPaddingRight();
            Paint p = new Paint();
            p.setTextSize(getTextSize());

            int textlong = (int) p.measureText(getText().toString());
            int x = getWidth()/2+(getWidth()/2-textlong);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            float circle_radius = DisplayUtils.dip2px(getContext(),3f);
            LogHelper.d(TAG,"9  ?  "+circle_radius+"  width: "+width);
            int xPos = x+20;
            if(width <= 240){
                xPos+=10;
                circle_radius = circle_radius*4/5;
            }
            canvas.drawCircle(xPos, getHeight()/2 - getTextSize()/2+1,circle_radius, paint);//width - getPaddingRight()
        }
        if(showRightBoundDivider == 1){
            int width = getWidth();
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#dbdbdb"));
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawLine(getWidth()-2,0,getWidth(),getHeight(),paint);
        }
    }

    public void setRedTipVisibility(int visibility) {
        tipVisibility = visibility;
        invalidate();
    }

    public int getTipVisibility() {
        return tipVisibility;
    }

    public void setShowRightBoundDivider(int divider){
        showRightBoundDivider = divider;
        invalidate();
    }
}
