package com.oceansky.calendar.library.adapter;

import android.support.v4.view.ViewPager;

import com.oceansky.calendar.library.constant.CaldroidCustomConstant;
import com.oceansky.calendar.library.customviews.DateCalendar;
import com.oceansky.calendar.library.customviews.InfiniteViewPager;
import com.oceansky.calendar.library.customviews.ViewMode;
import com.oceansky.calendar.library.utils.CalendarHelper;
import com.oceansky.calendar.library.utils.LogHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

/**
 * DatePageChangeListener refresh the date grid views when user swipe the calendar
 * @author thomasdao
 * User: 王旭国
 * Date: 16/6/15 10:24
 * Email:wangxuguo@jhyx.com.cn
 */
public class DatePageChangeListener implements ViewPager.OnPageChangeListener {
    private final static String TAG = DatePageChangeListener.class.getSimpleName();
    private int currentPage = InfiniteViewPager.OFFSET;
    private DateTime currentDateTime;
    private ArrayList<CaldroidGridAdapter> caldroidGridAdapters;
    private DateCalendar dateCalendar;


    public DatePageChangeListener(DateCalendar dateCalendar){
        this.dateCalendar = dateCalendar;
    }
    /**
     * Return currentPage of the dateViewPager
     *
     * @return
     */
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Return currentDateTime of the selected page
     *
     * @return
     */
    public DateTime getCurrentDateTime() {
        return currentDateTime;
    }


    public void setCurrentDateTime(DateTime dateTime) {
        this.currentDateTime = dateTime;
//            setCurSelectedDateTime(currentDateTime);////通过其他方式进行设置，解决这类直接进行变化
    }

    /**
     * Return 4 adapters
     *
     * @return
     */
    public ArrayList<CaldroidGridAdapter> getCaldroidGridAdapters() {
        return caldroidGridAdapters;
    }

    public void setCaldroidGridAdapters(
            ArrayList<CaldroidGridAdapter> caldroidGridAdapters) {
        this.caldroidGridAdapters = caldroidGridAdapters;
    }

    /**
     * Return virtual next position
     *
     * @param position
     * @return
     */
    private int getNext(int position) {
        return (position + 1) % CaldroidCustomConstant.NUMBER_OF_PAGES;
    }

    /**
     * Return virtual previous position
     *
     * @param position
     * @return
     */
    private int getPrevious(int position) {
        return (position + 3) % CaldroidCustomConstant.NUMBER_OF_PAGES;
    }

    /**
     * Return virtual current position
     *
     * @param position
     * @return
     */
    public int getCurrent(int position) {
        return position % CaldroidCustomConstant.NUMBER_OF_PAGES;
    }

    @Override
    public void onPageScrollStateChanged(int position) {
//        LogHelper.d(TAG,"onPageScrollStateChanged: "+position);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
//        LogHelper.d(TAG,"onPageScrolled: "+arg0+"  "+arg1+"  "+arg2);
    }

    public void refreshAdapters(int position) {
        // Get adapters to refresh
        CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                .get(getCurrent(position));
        CaldroidGridAdapter prevAdapter = caldroidGridAdapters
                .get(getPrevious(position));
        CaldroidGridAdapter nextAdapter = caldroidGridAdapters
                .get(getNext(position));

        if (position == currentPage) {
            // Refresh current adapter

            currentAdapter.setAdapterDateTime(currentDateTime);
            currentAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            currentAdapter.notifyDataSetChanged();

            // Refresh previous adapter
            prevAdapter.setAdapterDateTime(currentDateTime.minusMonths(1));
            prevAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            prevAdapter.notifyDataSetChanged();

            // Refresh next adapter
            nextAdapter.setAdapterDateTime(currentDateTime.plusMonths(1));
            nextAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            nextAdapter.notifyDataSetChanged();
        }
        // Detect if swipe right or swipe left
        // Swipe right
        else if (position > currentPage) {
            // Update current date time to next month
            currentDateTime = currentDateTime.plusMonths(1);

            // Refresh the adapter of next gridview
            nextAdapter.setAdapterDateTime(currentDateTime.plusMonths(1));
            nextAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            nextAdapter.notifyDataSetChanged();

        }
        // Swipe left
        else {
            // Update current date time to previous month
            currentDateTime = currentDateTime.minusMonths(1);

            // Refresh the adapter of previous gridview
            prevAdapter.setAdapterDateTime(currentDateTime.minusMonths(1));
            prevAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            prevAdapter.notifyDataSetChanged();
        }

        // Update current page
        currentPage = position;
    }

    /**
     * Refresh the fragments
     */
    @Override
    public void onPageSelected(int position) {
        LogHelper.d(TAG, "onPageSelected: " + position+"  currentPage: "+currentPage);
        DateTime curDateTime;
        if(currentPage > position){
            curDateTime = dateCalendar.getCurSelectedDateTime().minusMonths(1);
            Date date = CalendarHelper.convertDateTimeToDate(curDateTime);
            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            dateCalendar.clearSelectedDates();
            dateCalendar.setSelectedDate(date);
//            refreshLesson(date, dateStr);
        }else{
            curDateTime = dateCalendar.getCurSelectedDateTime().plusMonths(1);
            Date date = CalendarHelper.convertDateTimeToDate(curDateTime);
            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            dateCalendar.clearSelectedDates();
            dateCalendar.setSelectedDate(date);
//            refreshLesson(date, dateStr);
        }
        dateCalendar.refreshWeekViewToSelectedDateTime(curDateTime);
        refreshAdapters(position);//??? 是否不需要???
        // Update current date time of the selected page
        dateCalendar.setCurSelectedDateTime(curDateTime);
        dateCalendar.refreshMonthView();
        setCurrentDateTime(curDateTime);
        // Update all the dates inside current month
        CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                .get(position % CaldroidCustomConstant.NUMBER_OF_PAGES);

        // Refresh dateInMonthsList
        dateCalendar.getDateInMonthsList().clear();
        dateCalendar.getDateInMonthsList().addAll(currentAdapter.getDatetimeList());
        if(dateCalendar.getPageChangeListener()!=null){
            dateCalendar.getPageChangeListener().onPageChanged(curDateTime);
        }
        dateCalendar.refreshTeacherCourse(curDateTime);
    }

    /**
     * 周视图的选定日期改变后，改变月视图中的选定日期，可能月份已经改变，需要动态调整
     * 周视图下，可以显示的高度是一定的，但是monthPageView 缩时不一定，需要动态计算并设置
     * 待测试
     * @Time 20160615 15:15 当weekView选择跨度月时，setCurrentItem 改变会触发pageSelected事件，引起再次事件调用
     * @param dateTime
     */
    public void changedSelectDateToCur(DateTime dateTime) {
//        currentDateTime = dateTime;
        LogHelper.d(TAG,"changedSelectDateToCur curentDateTime: "+" currentDateTime  "+" dateTime: "+dateTime);
        DateTime maxTime =  currentDateTime.dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue();
        DateTime minTime = currentDateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue();
        if(dateTime.getMillis()<maxTime.getMillis()&&dateTime.getMillis()>minTime.getMillis()){
//                monthCalendar.setCurrentItem(monthCalendar.getCurrentItem(),false);
            dateCalendar.refreshMonthView();
            currentDateTime = dateTime;
            //日期发生部分，所在的位置发生变化？？？？？？？？？
        }else if(dateTime.getMillis()>maxTime.getMillis()){
            //会使viewpager再次刷新，停用，直接adapter数据调换   周视图下，月视图是不可见的
//            dateCalendar.monthCalendar.setCurrentItem(this.getCurrentPage()+1,false);
            /**********************************************************************/
            LogHelper.d(TAG, "changedSelectDateToCur " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
            currentDateTime = dateTime;
            CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                    .get(getCurrent(currentPage));
            CaldroidGridAdapter prevAdapter = caldroidGridAdapters
                    .get(getPrevious(currentPage));
            CaldroidGridAdapter nextAdapter = caldroidGridAdapters
                    .get(getNext(currentPage));


            // Refresh current adapter

            currentAdapter.initWithSpecialDateForMonthView(currentDateTime, dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh previous adapter
            prevAdapter.initWithSpecialDateForMonthView(currentDateTime.minusMonths(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh next adapter
            nextAdapter.initWithSpecialDateForMonthView(currentDateTime.plusMonths(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh dateInMonthsList
            dateCalendar.getDateInMonthsList().clear();
            dateCalendar.getDateInMonthsList().addAll(currentAdapter.getDatetimeList());

            if(dateCalendar.getPageChangeListener()!=null){
                dateCalendar.getPageChangeListener().onPageChanged(currentDateTime);
            }
            /**********************************************************************/
//            dateCalendar.monthCalendar.setY(0);
            //
        }else if(dateTime.getMillis()<minTime.getMillis()){
//            dateCalendar.monthCalendar.setCurrentItem(this.getCurrentPage()-1,false);

            /**********************************************************************/
            LogHelper.d(TAG, "changedSelectDateToCur " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
            currentDateTime = dateTime;
            CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                    .get(getCurrent(currentPage));
            CaldroidGridAdapter prevAdapter = caldroidGridAdapters
                    .get(getPrevious(currentPage));
            CaldroidGridAdapter nextAdapter = caldroidGridAdapters
                    .get(getNext(currentPage));
            // Refresh current adapter

            currentAdapter.initWithSpecialDateForMonthView(currentDateTime, dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh previous adapter
            prevAdapter.initWithSpecialDateForMonthView(currentDateTime.minusMonths(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh next adapter
            nextAdapter.initWithSpecialDateForMonthView(currentDateTime.plusMonths(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

            // Refresh dateInMonthsList
            dateCalendar.getDateInMonthsList().clear();
            dateCalendar.getDateInMonthsList().addAll(currentAdapter.getDatetimeList());

            if(dateCalendar.getPageChangeListener()!=null){
                dateCalendar.getPageChangeListener().onPageChanged(currentDateTime);
            }
            /**********************************************************************/
        }
        /**********************************************************************/
        //根据当前的日期变化，计算当前的monthCalendar 的y轴Y的值，和当前DateCaledar的高度
        //得知当前的day，周几，计算当前在第几行，然后计算出Y轴的值   周一为第一天
        int dayOfMonth = dateTime.getDayOfMonth();
        int dayOfWeek = dateTime.dayOfMonth().withMinimumValue().getDayOfWeek();
        int firstRowDays = 7-dayOfWeek+1;
        int daycountofmonth = dateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
        LogHelper.d(TAG,"dayofMonth: "+dayOfMonth+" dayofWeek: "+dayOfWeek+" firstRowDays: "+firstRowDays+" daycountofmonth: "+daycountofmonth);
        //计算这个月有几个周，五周或者四周，还有六周
        int rows = (int) Math.ceil((daycountofmonth-firstRowDays)/7)+1;//总行数
        int currentrow = (int )Math.ceil((dayOfMonth-firstRowDays)/7)+2;
        LogHelper.d(TAG,"rows: "+rows+" currentrow: "+currentrow);
        int y = -(currentrow-1)*dateCalendar.monthCalendar.getRowHeight();
        LogHelper.i(TAG,"curY: "+y+" dateCalendar.monthCalendar.Height: "+dateCalendar.monthCalendar.getHeight()+"  "+dateCalendar.monthCalendar.getRowHeight());
        LogHelper.i(TAG," dateCalendar.weekCalendar.Height: "+dateCalendar.weekCalendar.getHeight()+"  "+dateCalendar.weekCalendar.getRowHeight());
        if(dateCalendar.getViewMode() == ViewMode.WEEK) {
            dateCalendar.monthCalendar.setY(y);
        }
//        dateCalendar.getLayoutParams().height =dateCalendar.monthCalendar.getHeight()+y;
//        dateCalendar.setLayoutParams(dateCalendar.getLayoutParams());
    }
}
