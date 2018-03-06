package com.oceansky.teacher.customviews.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.oceansky.teacher.constant.CaldroidCustomConstant;
import com.oceansky.teacher.customviews.DateCalendar;
import com.oceansky.teacher.fragments.CoursesFragment;
import com.oceansky.teacher.utils.LogHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * User: 王旭国
 * Date: 16/6/16 10:43
 * Email:wangxuguo@jhyx.com.cn
 */
public class CoursesPageChangedLister implements ViewPager.OnPageChangeListener {
    private final InfiniteCoursePageAdapter infiniteCoursePageAdapter;
    private int currentPage = 1000;
    private String tag = getClass().getSimpleName();
    private DateCalendar dateCalendar;
    private DateTime dateTime;
    private ArrayList<CoursesFragment> fragments;

    public CoursesPageChangedLister(DateCalendar dateCalendar, InfiniteCoursePageAdapter infiniteCoursePageAdapter, DateTime dateTime) {
        this.infiniteCoursePageAdapter = infiniteCoursePageAdapter;
        this.dateCalendar = dateCalendar;
        this.dateTime = dateTime;
    }

    /**
     * 强制刷新时间 设置当前的时间
     *
     * @param dateTime
     */
    public void setCurDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
        LogHelper.d(tag, "setCurDateTime " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        refreshFragment(currentPage);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
//        if(currentPage >position){
//            dateTime = dateTime.minusDays(1);
//        }else{
//            dateTime = dateTime.plusDays(1);
//        }
        CoursesFragment courseFragment = (CoursesFragment) infiniteCoursePageAdapter.getCurrentFragment();
        if(courseFragment!=null&&courseFragment.getDateTime()!=null) {
            LogHelper.d(tag, "onPageSelected: " + position + courseFragment.getDateTime().toString(CaldroidCustomConstant.simpleFormator));
        }
        refreshFragment(position);
        currentPage = position;
        dateCalendar.setSelectedDateTimeByCoursePagers(dateTime);
    }

    public void refreshFragment(int position) {
        LogHelper.d(tag, "refreshFragmetn: position: " + position + " currentPage: " + currentPage
                + " dateTime: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        // Get adapters to refresh
        CoursesFragment currentFragment = fragments
                .get(getCurrent(position));
        if(currentFragment!=null&&currentFragment.getDateTime()!=null) {
            LogHelper.d(tag, "currentFragment :" + currentFragment.getDateTime().toString(CaldroidCustomConstant.simpleFormator));
        }
        CoursesFragment prevFragment = fragments
                .get(getPrevious(position));
        CoursesFragment nextFragment = fragments
                .get(getNext(position));

        if (position == currentPage) {
            // Refresh current adapter
            currentFragment.changeDateTime(dateTime);
            prevFragment.changeDateTime(dateTime.minusDays(1));
            nextFragment.changeDateTime(dateTime.plusDays(1));
        }
        // Detect if swipe right or swipe left
        // Swipe right
        else if (position > currentPage) {
            // Update current date time to next day
            dateTime = dateTime.plusDays(1);
            nextFragment.changeDateTime(dateTime.plusDays(1));

        }
        // Swipe left
        else {
            // Update current date time to previous day
            dateTime = dateTime.minusDays(1);
            prevFragment.changeDateTime(dateTime.minusDays(1));
        }

        // Update current page

    }

    public ArrayList<CoursesFragment> getFragments() {
        return fragments;
    }

    public void setFragments(ArrayList<CoursesFragment> fragments) {
        this.fragments = fragments;
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
}
