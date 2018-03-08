package com.oceansky.calendar.example.customviews.adapter;

import android.support.v4.view.ViewPager;

import com.oceansky.calendar.example.constant.CaldroidCustomConstant;
import com.oceansky.calendar.example.customviews.DateCalendar;
import com.oceansky.calendar.example.customviews.InfiniteViewPager;
import com.oceansky.calendar.example.utils.CalendarHelper;
import com.oceansky.calendar.example.utils.LogHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

/**
 * DateWeekPageChangeListener refresh the date grid views when user swipe the calendar of Week Views
 *
 * @author thomasdao
 * User: 王旭国
 * Date: 16/6/15 10:33
 * Email:wangxuguo@jhyx.com.cn
 */
public class DateWeekPageChangeListener implements ViewPager.OnPageChangeListener {
    private int currentPage = InfiniteViewPager.OFFSET;
    private DateTime currentDateTime;
    private ArrayList<CaldroidGridAdapter> caldroidGridAdapters;
    private String tag = getClass().getSimpleName();
    private DateCalendar dateCalendar;

    public DateWeekPageChangeListener(DateCalendar dateCalendar){
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
//        setCurSelectedDateTime(currentDateTime);//???????
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
        LogHelper.d(tag,"onPageScrollStateChanged: "+position);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        LogHelper.d(tag,"onPageScrolled: "+arg0+"  "+arg1+"  "+arg2);
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
            prevAdapter.setAdapterDateTime(currentDateTime.minusWeeks(1));
            prevAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            prevAdapter.notifyDataSetChanged();

            // Refresh next adapter
            nextAdapter.setAdapterDateTime(currentDateTime.plusWeeks(1));
            nextAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            nextAdapter.notifyDataSetChanged();
        }
        // Detect if swipe right or swipe left
        // Swipe right
        else if (position > currentPage) {
            // Update current date time to next week
            currentDateTime = currentDateTime.plusWeeks(1);

            // Refresh the adapter of next gridview
            nextAdapter.setAdapterDateTime(currentDateTime.plusWeeks(1));
            nextAdapter.setCaldroidData(dateCalendar.getCaldroidData());
            nextAdapter.notifyDataSetChanged();

        }
        // Swipe left
        else {
            // Update current date time to previous week
            currentDateTime = currentDateTime.minusWeeks(1);

            // Refresh the adapter of previous gridview
            prevAdapter.setAdapterDateTime(currentDateTime.minusWeeks(1));
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
        LogHelper.d(tag, "onPageSelected: " + position);
        DateTime curDateTime;
        if(currentPage >position){
            curDateTime = dateCalendar.getCurSelectedDateTime().minusWeeks(1);
            Date date = CalendarHelper.convertDateTimeToDate(curDateTime);
            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            dateCalendar.clearSelectedDates();
            dateCalendar.setSelectedDate(date);
//            dateCalendar.refreshWeeksView();
//            refreshLesson(date, dateStr);
        }else{
            curDateTime = dateCalendar.getCurSelectedDateTime().plusWeeks(1);
            Date date = CalendarHelper.convertDateTimeToDate(curDateTime);
            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            dateCalendar.clearSelectedDates();
            dateCalendar.setSelectedDate(date);
//            dateCalendar.refreshWeeksView();
//            refreshLesson(date, dateStr);
        }


        refreshAdapters(position);

        // Update current date time of the selected page
        dateCalendar.setCurSelectedDateTime(curDateTime);
        dateCalendar.refreshWeeksView();
//        setCurSelectedDateTimerrentDateTime(curDateTime);
        // Update all the dates inside current week
        CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                .get(position % CaldroidCustomConstant.NUMBER_OF_PAGES);

        // Refresh dateInMonthsList
        dateCalendar.getDateInWeekList().clear();
        dateCalendar.getDateInWeekList().addAll(currentAdapter.getDatetimeList());

        dateCalendar.refreshMonthViewToSelectedDateTime(curDateTime);
        if(dateCalendar.getPageChangeListener()!=null){
            dateCalendar.getPageChangeListener().onPageChanged(curDateTime);
        }
        dateCalendar.refreshTeacherCourse(curDateTime);
    }

    /**
     * 强制转换当前的adaptor 为包含选定日期的界面
     *
     * @param dateTime
     */
    public void changedToCur(DateTime dateTime) {
        LogHelper.d(tag, "changedToCur " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        currentDateTime = dateTime;
        CaldroidGridAdapter currentAdapter = caldroidGridAdapters
                .get(getCurrent(currentPage));
        CaldroidGridAdapter prevAdapter = caldroidGridAdapters
                .get(getPrevious(currentPage));
        CaldroidGridAdapter nextAdapter = caldroidGridAdapters
                .get(getNext(currentPage));


        // Refresh current adapter
        currentAdapter.initWithSpecialDate(currentDateTime, dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

        // Refresh dateInMonthsList
        dateCalendar.getDateInWeekList().clear();
        dateCalendar.getDateInWeekList().addAll(currentAdapter.getDatetimeList());

        // Refresh previous adapter
        prevAdapter.initWithSpecialDate(currentDateTime.minusWeeks(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());

        // Refresh next adapter
        nextAdapter.initWithSpecialDate(currentDateTime.plusWeeks(1), dateCalendar.getCaldroidData(), dateCalendar.getExtraData());
    }
}
