package com.oceansky.calendar.library.constant;

/**
 * 日历中常量
 * Created by 王旭国 on 16/6/12 11:07
 */
public class CaldroidCustomConstant {
    /**
     * Initial params key
     */
    public final static String
            DAY = "day",
            MONTH = "month",
            YEAR = "year",
            DISABLE_DATES = "disableDates",
            SELECTED_DATES = "selectedDates",
            MIN_DATE = "minDate",
            MAX_DATE = "maxDate",
            ENABLE_SWIPE = "enableSwipe",
            START_DAY_OF_WEEK = "startDayOfWeek",
            ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates",
            THEME_RESOURCE = "themeResource";

    /**
     * For internal use
     */
    public final static String
            _MIN_DATE_TIME = "_minDateTime",
            _MAX_DATE_TIME = "_maxDateTime",
            _BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap",
            _TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap",
            _TEXT_DOT_FOR_DATETIME_MAP = "_textDotForDateTimeMap";
    /**
     * 划动的月（周） 日历的pager数量
     */
    // We need 4 gridviews for previous month, current month and next month,
    // and 1 extra fragment for fragment recycle
    public static final int NUMBER_OF_PAGES = 4;

//    dateTime.toString("MM/dd/yyyy hh:mm:ss.SSSa");
    public static final String simpleFormator = "dd-MM-yyyy HH:mm:ss";
//    dateTime.toString("EEEE dd MMMM, yyyy HH:mm:ssa");
//    dateTime.toString("MM/dd/yyyy HH:mm ZZZZ");
//    dateTime.toString("MM/dd/yyyy HH:mm Z");

}
