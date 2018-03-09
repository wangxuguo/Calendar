package com.oceansky.calendar.library.utils;


import com.oceansky.calendar.library.constant.CaldroidCustomConstant;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

;

/**
 * Created by 王旭国 on 16/6/12 11:09
 */
public class CalendarHelper {
    private static final String TAG = CalendarHelper.class.getSimpleName();
    private static SimpleDateFormat yyyyMMddFormat;

    public static void setup() {
        yyyyMMddFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    }

    /**
     * Retrieve all the dates for a given calendar month Include previous week,
     * current week and next week.
     * @param day
     * @param month
     * @param year
     * @param startDayOfWeek : calendar can start from customized date instead of Sunday
     * @return
     */
    public static ArrayList<DateTime> getFullWeek(int day, int month, int year,
                                                  int startDayOfWeek, boolean sixWeeksInCalendar) {
        ArrayList<DateTime> datetimeList = new ArrayList<DateTime>();
        DateTime curDateTime = new DateTime(year,month,day,0,0,0,0);

        DateTime curFirstDateOfWeek = curDateTime.dayOfWeek().withMinimumValue();
        LogHelper.i(TAG,"curDateTime: "+curDateTime.toString(CaldroidCustomConstant.simpleFormator)+"  "+
                curFirstDateOfWeek.toString(CaldroidCustomConstant.simpleFormator));

        for(int i = 0; i<7 ;i++){
            datetimeList.add(curFirstDateOfWeek);
            curFirstDateOfWeek = curFirstDateOfWeek.plusDays(1);
        }

        return datetimeList;
    }
    /**
     * Retrieve all the dates for a given calendar month Include previous month,
     * current month and next month.
     *
     * @param month
     * @param year
     * @param startDayOfWeek : calendar can start from customized date instead of Sunday
     * @return
     */
    public static ArrayList<DateTime> getFullWeeks(int month, int year,
                                                   int startDayOfWeek, boolean sixWeeksInCalendar) {
        ArrayList<DateTime> datetimeList = new ArrayList<DateTime>();

        DateTime firstDateOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.dayOfMonth().withMaximumValue();

        // Add dates of first week from previous month
        int weekdayOfFirstDate = firstDateOfMonth.getDayOfWeek();

        // If weekdayOfFirstDate smaller than startDayOfWeek
        // For e.g: weekdayFirstDate is Monday, startDayOfWeek is Tuesday
        // increase the weekday of FirstDate because it's in the future
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }

        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - startDayOfWeek);
            if (!dateTime.isBefore(firstDateOfMonth)) {
                break;
            }

            datetimeList.add(dateTime);
            weekdayOfFirstDate--;
        }
        int  daysinmonth  = lastDateOfMonth.getDayOfMonth()-firstDateOfMonth.getDayOfMonth()+1;
        // Add dates of current month
        for (int i = 0; i < daysinmonth; i++) {
            datetimeList.add(firstDateOfMonth.plusDays(i));
        }

        // Add dates of last week from next month
        int endDayOfWeek = startDayOfWeek - 1;

        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }

        if (lastDateOfMonth.getDayOfWeek() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                datetimeList.add(nextDay);
                i++;
                if (nextDay.getDayOfWeek() == endDayOfWeek) {
                    break;
                }
            }
        }

        // Add more weeks to fill remaining rows
        if (sixWeeksInCalendar) {
            int size = datetimeList.size();
            int row = size / 7;
            int numOfDays = (6 - row) * 7;
            DateTime lastDateTime = datetimeList.get(size - 1);
            for (int i = 1; i <= numOfDays; i++) {
                DateTime nextDateTime = lastDateTime.plusDays(i);
                datetimeList.add(nextDateTime);
            }
        }

        return datetimeList;
    }

    /**
     * Get the DateTime from Date, with hour and min is 0
     *
     * @param date
     * @return
     */
    public static DateTime convertDateToDateTime(Date date) {
        // Get year, javaMonth, date
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        // javaMonth start at 0. Need to plus 1 to get datetimeMonth
        return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
    }

    public static Date convertDateTimeToDate(DateTime dateTime) {
        int year = dateTime.getYear();
        int datetimeMonth = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        // datetimeMonth start at 1. Need to minus 1 to get javaMonth
        calendar.set(year, datetimeMonth - 1, day);

        return calendar.getTime();
    }

    /**
     * Get the Date from String with custom format. Default format is yyyy-MM-dd
     *
     * @param dateString
     * @param dateFormat
     * @return
     * @throws ParseException
     */
    public static Date getDateFromString(String dateString, String dateFormat)
            throws ParseException {
        SimpleDateFormat formatter;
        if (dateFormat == null) {
            if (yyyyMMddFormat == null) {
                setup();
            }

            formatter = yyyyMMddFormat;
        } else {
            formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        }

        return formatter.parse(dateString);
    }

    /**
     * Get the DateTime from String with custom format. Default format is
     * yyyy-MM-dd
     *
     * @param dateString
     * @param dateFormat
     * @return
     */
    public static DateTime getDateTimeFromString(String dateString,
                                                 String dateFormat) {
        Date date;
        try {
            date = getDateFromString(dateString, dateFormat);
            return convertDateToDateTime(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> convertToStringList(
            ArrayList<DateTime> dateTimes) {
        ArrayList<String> list = new ArrayList<String>();
        for (DateTime dateTime : dateTimes) {
            list.add(dateTime.toString("YYYY-MM-DD"));
        }
        return list;
    }

}
