package com.oceansky.calendar.example.listeners;

import android.view.View;


import org.joda.time.DateTime;

import java.util.Date;
/**
 * Created by 王旭国 on 16/6/12 17:31
 */


/**
 * CaldroidListener inform when user clicks on a valid date (not within disabled
 * dates, and valid between min/max dates)
 * <p/>
 * The method onChangeMonth is optional, user can always override this to listen
 * to month change event
 *
 * @author thomasdao
 */
public abstract class CaldroidListener {
    /**
     * Inform client user has clicked on a date
     *
     * @param date
     * @param view
     * @param isFromContentView 是否来自内容展示页面的刷新
     */
    public abstract void onSelectMonthDate(DateTime date, View view, boolean isFromContentView);

    /**
     * Inform client user has clicked on a date on week view
     *
     * @param date
     * @param view
     * @param isFromContentView 是否来自内容展示页面的刷新
     */
    public abstract void onSelectWeekDate(DateTime date, View view, boolean isFromContentView);


    /**
     * Inform client user has long clicked on a date
     *
     * @param date
     * @param view
     */
    public void onLongClickDate(Date date, View view) {
        // Do nothing
    }


    /**
     * Inform client that calendar has changed month
     *
     * @param month
     * @param year
     */
    public void onChangeMonth(int month, int year) {
        // Do nothing
    }

    ;


    /**
     * Inform client that CaldroidFragment view has been created and views are
     * no longer null. Useful for customization of button and text views
     */
    public void onCaldroidViewCreated() {
        // Do nothing
    }
}
