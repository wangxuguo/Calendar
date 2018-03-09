package com.oceansky.calendar.library.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.text.SimpleDateFormat;

/**
 * Created by 王旭国 on 16/6/8 11:45
 */
public class CalendarViewPager extends InfiniteViewPager{
    private SimpleDateFormat simpleDateFormat;

    public CalendarViewPager(Context paramContext)
    {
        super(paramContext);
        initComponent(paramContext);
    }

    public CalendarViewPager(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        initComponent(paramContext);
    }
    protected void initComponent(Context paramContext)
    {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if(getVisibility()!= View.VISIBLE){
//            return true;
//        }
//        else{
            return super.dispatchTouchEvent(ev);
//        }
    }
}
