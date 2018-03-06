package com.oceansky.teacher.customviews;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.oceansky.teacher.utils.CalendarHelper;
import com.oceansky.teacher.utils.LogHelper;

import java.util.ArrayList;
import org.joda.time.DateTime;


/**
 * A {@link ViewPager} that allows pseudo-infinite paging with a wrap-around
 * effect. Should be used with an {@link }.
 *
 * User: 王旭国
 * Email:wangxuguo@jhyx.com.cn
 */
public class InfiniteViewPager extends ViewPager {
    private String tag = getClass().getSimpleName();
    // ******* Declaration *********
    public static final int OFFSET = 1000;
    /**
     *
     */
    private int upMarginHeight;
    private int downMainHeight;
    /**
     * dates is required to calculate the height correctly
     */
    private ArrayList<DateTime> dates =new ArrayList<>();

    /**
     * Enable swipe
     */
    private boolean enabled = true;

    /**
     * A calendar height is not fixed, it may have 4, 5 or 6 rows. Set
     * fitAllMonths to true so that the calendar will always have 6 rows
     */
    private boolean sixWeeksInCalendar = false;

    /**
     * Use internally to decide height of the calendar
     */
    private int rowHeight = 0;
    private int rows;
    /**
     *
     */
    private boolean isShowDuringCollape;


    // ******* Setter and getters *********
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSixWeeksInCalendar() {
        return sixWeeksInCalendar;
    }

    public ArrayList<DateTime> getDates() {
        return dates;
    }

    public void setDates(ArrayList<DateTime> dates) {
        this.dates = dates;
    }

    public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
        this.sixWeeksInCalendar = sixWeeksInCalendar;
        rowHeight = 0;
    }

    // ************** Constructors ********************
    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfiniteViewPager(Context context) {
        super(context);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        // offset first element so that we can scroll to the left
        setCurrentItem(OFFSET);
    }

    /**
     *
     * @param marginTop
     * @param marginBottom
     */
    private void setMarginToTop(int marginTop, int marginBottom){
        ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.topMargin  = marginTop;
        layoutParams.bottomMargin = marginBottom;
        setLayoutParams(layoutParams);
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (enabled) {
            return super.onInterceptTouchEvent(event);
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * ViewPager does not respect "wrap_content". The code below tries to
     * measure the height of the child and set the height of viewpager based on
     * child height
     *
     * It was customized from
     * http://stackoverflow.com/questions/9313554/measuring-a-viewpager
     *
     * Thanks Delyan for his brilliant code
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Calculate row height
            rows = dates.size() / 7;
            if(rows==1||rows == 6){

            }else {
                LogHelper.e(tag,"rows error");
                if(rows == 5){
                    if(dates.size() == 35){
                        DateTime  dat  = dates.get(dates.size()/2);
                        dates = CalendarHelper.getFullWeeks(dat.getYear(),dat.getMonthOfYear(),dat.getDayOfMonth(),true);
                        LogHelper.d(tag,"CalendarHelper.getFullWeeks size: "+dates.size());
                    }
                    rows = 6;
                }
            }
        if (getChildCount() > 0 && rowHeight == 0) {
            View firstChild = getChildAt(0);
            int width = getMeasuredWidth();

            // Use the previously measured width but simplify the calculations
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                    MeasureSpec.EXACTLY);


            firstChild.measure(widthMeasureSpec, MeasureSpec
                    .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            rowHeight = firstChild.getMeasuredHeight();
        }

        // Calculate height of the calendar
        int calHeight;

        // If fit 6 weeks, we need 6 rows
        if (sixWeeksInCalendar) {
            calHeight = rowHeight * 6;
        } else { // Otherwise we return correct number of rows
            calHeight = rowHeight * rows;
        }

        // Prevent small vertical scroll
//        calHeight -= 12;
        LogHelper.i(tag,"calHeight : "+calHeight);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(calHeight,
                MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogHelper.i(tag,"onLayout  changed: "+changed+"   l: "+l+"  t:  "+t+"  r:  "+r+"   b: "+b);
        super.onLayout(changed, l, t, r, b);
    }

    public int getUpMarginHeight() {
        return upMarginHeight;
    }

    public void setUpMarginHeight(int upMarginHeight) {
        this.upMarginHeight = upMarginHeight;
    }

    public int getDownMainHeight() {
        return downMainHeight;
    }

    public void setDownMainHeight(int downMainHeight) {
        this.downMainHeight = downMainHeight;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }
}
