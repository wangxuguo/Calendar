package com.oceansky.teacher.customviews.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.CaldroidCustomConstant;
import com.oceansky.teacher.customviews.CellView;
import com.oceansky.teacher.customviews.ViewMode;
import com.oceansky.teacher.utils.CalendarHelper;
import com.oceansky.teacher.utils.LogHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 月，周适配器
 * Created by 王旭国 on 16/6/12 11:04
 */
public class CaldroidGridAdapter extends BaseAdapter {
    private final String TAG ;
    private Map<String, Object> extraData;
    private Map<String, Object> caldroidData;
    private final ViewMode viewMode;
    protected LayoutInflater localInflater;
    private ArrayList<DateTime> datetimeList;
    protected DateTime minDateTime;
    protected DateTime maxDateTime;
    private DateTime today;
    private Context context;
    private int month;
    private int year;
    private int day;
    private DateTime dateTime;//代替year，month,day  日期,如果为当前显示的View的adaptor，则为选定的日期
    protected Resources resources;
    protected ArrayList<DateTime> disableDates;
    protected ArrayList<DateTime> selectedDates;
    // Use internally, to make the search for date faster instead of using
    // indexOf methods on ArrayList
    protected Map<DateTime, Integer> disableDatesMap = new HashMap<DateTime, Integer>();
    protected Map<DateTime, Integer> selectedDatesMap = new HashMap<DateTime, Integer>();
    protected int defaultCellBackgroundRes = -1;
    protected ColorStateList defaultTextColorRes;
    private boolean squareTextViewCell = false;
    private int themeResource;


    public CaldroidGridAdapter(Context activity, ViewMode viewMode, int day, int month, int year, Map<String, Object> caldroidData, Map<String, Object> extraData) {
        super();

        this.context = activity;
        this.viewMode = viewMode;
        if(viewMode == ViewMode.MONTH){
            TAG  = "MONTH"+CaldroidGridAdapter.class.getSimpleName();
        }else{
            TAG  = "WEEK"+CaldroidGridAdapter.class.getSimpleName();
        }
        this.day = day;
        this.month = month;
        this.year = year;
        this.dateTime = new DateTime(year,month,day,0,0,0,0);
        this.caldroidData = caldroidData;
        this.extraData = extraData;
        this.resources = context.getResources();

        // Get data from caldroidData
        populateFromCaldroidData();
        getDefaultResources();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        localInflater = getThemeInflater(context, inflater, themeResource);
    }

    public static LayoutInflater getThemeInflater(Context context, LayoutInflater origInflater, int themeResource) {
        Context wrapped = new android.view.ContextThemeWrapper(context, themeResource);
        return origInflater.cloneInContext(wrapped);
    }

    /**
     * Retrieve internal parameters from caldroid data
     */
    @SuppressWarnings("unchecked")
    private void populateFromCaldroidData() {
        disableDates = (ArrayList<DateTime>) caldroidData
                .get(CaldroidCustomConstant.DISABLE_DATES);
        if (disableDates != null) {
            disableDatesMap.clear();
            for (DateTime dateTime : disableDates) {
                disableDatesMap.put(dateTime, 1);
            }
        }

        selectedDates = (ArrayList<DateTime>) caldroidData
                .get(CaldroidCustomConstant.SELECTED_DATES);
        if (selectedDates != null) {
            selectedDatesMap.clear();
            for (DateTime dateTime : selectedDates) {
//                LogHelper.d(TAG, "selectedDate: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                selectedDatesMap.put(dateTime, 1);
            }
        }
        //月份的最大值最小值由传入的datetime决定
        if (viewMode == ViewMode.WEEK) {
            minDateTime = dateTime.dayOfWeek().withMinimumValue().millisOfDay().withMinimumValue();
            maxDateTime = dateTime.dayOfWeek().withMaximumValue().millisOfDay().withMaximumValue();
        } else {
            minDateTime = dateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue();
            maxDateTime = dateTime.dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue();
        }
        // Get theme
        themeResource = (Integer) caldroidData
                .get(CaldroidCustomConstant.THEME_RESOURCE);
        if (viewMode == ViewMode.WEEK) {
            this.datetimeList = CalendarHelper.getFullWeek(this.day, this.month, this.year, 1, true);
        } else {
            this.datetimeList = CalendarHelper.getFullWeeks(this.month, this.year,
                    1, true);
            if(datetimeList.size() != 42){
                LogHelper.e(TAG,"datetimeList.size() != 42");
                this.datetimeList = CalendarHelper.getFullWeeks(this.month, this.year,
                        1, true);
                if(datetimeList.size() != 42) {
                    LogHelper.e(TAG, "getDataList again   datetimeList.size() != 42");
                }
            }
        }
    }

    // This method retrieve default resources for background and text color,
    // based on the Caldroid theme
    private void getDefaultResources() {
        Context wrapped = new ContextThemeWrapper(context, themeResource);

        // Get style of normal cell or square cell in the theme
        Resources.Theme theme = wrapped.getTheme();
        TypedValue styleCellVal = new TypedValue();
        if (squareTextViewCell) {
            theme.resolveAttribute(R.attr.styleCaldroidSquareCell, styleCellVal, true);
        } else {
            theme.resolveAttribute(R.attr.styleCaldroidNormalCell, styleCellVal, true);
        }

        // Get default background of cell
        TypedArray typedArray = wrapped.obtainStyledAttributes(styleCellVal.data, R.styleable.Cell);
        defaultCellBackgroundRes = typedArray.getResourceId(R.styleable.Cell_android_background, -1);
        defaultTextColorRes = typedArray.getColorStateList(R.styleable.Cell_android_textColor);
        typedArray.recycle();
    }

    @Override
    public int getCount() {
        return this.datetimeList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.datetimeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            final int squareDateCellResource = squareTextViewCell ? R.layout.square_date_cell : R.layout.normal_date_cell;
            View v = localInflater.inflate(squareDateCellResource, parent, false);
            viewHolder.cellView = (CellView) v.findViewById(R.id.calendar_tv);
            viewHolder.dot = v.findViewById(R.id.dot);
            convertView = v;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        customizeDotView(position, viewHolder.dot);
        customizeTextView(position, viewHolder.cellView,viewHolder.dot);



        return convertView;
    }

    public class ViewHolder {
        public CellView cellView;
        public View dot;
    }

    protected void customizeDotView(int position, View dot) {
        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        if(dateTime.getMillis()>=minDateTime.getMillis()&&dateTime.getMillis()<=maxDateTime.getMillis()){

        }else {
            return;
        }
        // Set custom text underline
        Map<DateTime, Integer> textUnderlineForDateTimeMap = (Map<DateTime, Integer>) caldroidData
                .get(CaldroidCustomConstant._TEXT_DOT_FOR_DATETIME_MAP);
        if (textUnderlineForDateTimeMap != null) {
            // Get textColor for the dateTime
            Integer count = textUnderlineForDateTimeMap.get(dateTime);
            // Set it
            if (count != null && count > 0) {
                dot.setVisibility(View.VISIBLE);
            } else {
                dot.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Customize colors of text and background based on states of the cell
     * (disabled, active, selected, etc)
     * <p/>
     * To be used only in getView method
     *
     * @param position
     * @param cellView
     */
    protected void customizeTextView(int position, CellView cellView ,View dot) {
        // Get the padding of cell so that it can be restored later
        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);

        cellView.resetCustomStates();
        resetCustomResources(cellView);

        if (dateTime.equals(getToday())) {
            LogHelper.d(TAG, "dateTime Today!!");
            if(dateTime.getMillis()>=minDateTime.getMillis()&&dateTime.getMillis()<=maxDateTime.getMillis()){
                cellView.addCustomState(CellView.STATE_TODAY);
            }else {
                cellView.addCustomState(CellView.STATE_PREV_NEXT_MONTH);
                LogHelper.d(TAG, "dateTime Today!!  STATE_PREV_NEXT_MONTH");
            }

        }

        // Set color of the dates in previous / next month
        if (dateTime.getMonthOfYear() != month && viewMode == ViewMode.MONTH) {
            cellView.addCustomState(CellView.STATE_PREV_NEXT_MONTH);
        }

        // Customize for disabled dates and date outside min/max dates
//        if ((minDateTime != null && dateTime.lt(minDateTime))
//                || (maxDateTime != null && dateTime.gt(maxDateTime))
//                || (disableDates != null && disableDatesMap
//                .containsKey(dateTime))) {
//
//            cellView.addCustomState(CellView.STATE_DISABLED);
//        } ???????????
//        LogHelper.i(TAG,"minDateTime: "+minDateTime.toString(CaldroidCustomConstant.simpleFormator)
//                +" maxDateTime "+maxDateTime.toString(CaldroidCustomConstant.simpleFormator)
//                +"curDateTime: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        if ((minDateTime != null && dateTime.isBefore(minDateTime.getMillis()))
                || (maxDateTime != null && dateTime.isAfter(maxDateTime.getMillis()))
                || (disableDates != null && disableDatesMap
                .containsKey(dateTime))) {
            dot.setVisibility(View.GONE);
            cellView.addCustomState(CellView.STATE_DISABLED);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                cellView.setBackgroundColor(context.getColor(R.color.caldroid_black));
//            }
        }
        // Customize for selected dates
        if (selectedDates != null && selectedDatesMap.containsKey(dateTime)) {
//            LogHelper.d(tag, "selectedDates: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
            cellView.addCustomState(CellView.STATE_SELECTED);
        } else if (selectedDates != null) {
            if (selectedDates.size() >= 1) {
//                LogHelper.d(tag, "selectedDates none" + selectedDates.get(0).toString(CaldroidCustomConstant.simpleFormator));
                for (DateTime datetime : selectedDatesMap.keySet()) {
//                    LogHelper.d(tag, "selectedDatesMap: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                }
            } else {
//                LogHelper.d(tag, "selectedDates none");
            }

        }

        cellView.refreshDrawableState();

        // Set text
        cellView.setText(String.valueOf(dateTime.getDayOfMonth()));

        // Set custom color if required
        setCustomResources(dateTime, cellView, cellView);

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);
    }

    @SuppressWarnings("unchecked")
    protected void setCustomResources(DateTime dateTime, View backgroundView,
                                      TextView textView) {
        // Set custom background resource
        Map<DateTime, Drawable> backgroundForDateTimeMap = (Map<DateTime, Drawable>) caldroidData
                .get(CaldroidCustomConstant._BACKGROUND_FOR_DATETIME_MAP);
        if (backgroundForDateTimeMap != null) {
            // Get background resource for the dateTime
            Drawable drawable = backgroundForDateTimeMap.get(dateTime);
            // Set it
            if (drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    //backgroundView.setBackground(drawable);

                    textView.setBackground(drawable);
                } else {
                    textView.setBackgroundDrawable(drawable);
                }
            }
        }

        // Set custom text color
        Map<DateTime, Integer> textColorForDateTimeMap = (Map<DateTime, Integer>) caldroidData
                .get(CaldroidCustomConstant._TEXT_COLOR_FOR_DATETIME_MAP);
        if (textColorForDateTimeMap != null) {
            // Get textColor for the dateTime
            Integer textColorResource = textColorForDateTimeMap.get(dateTime);

            // Set it
            if (textColorResource != null) {
                textView.setTextColor(resources.getColor(textColorResource));
            }
        }

        // Set custom text underline
        Map<DateTime, Integer> textUnderlineForDateTimeMap = (Map<DateTime, Integer>) caldroidData
                .get(CaldroidCustomConstant._TEXT_DOT_FOR_DATETIME_MAP);
        if (textUnderlineForDateTimeMap != null) {
            // Get textColor for the dateTime
            Integer isShow = textUnderlineForDateTimeMap.get(dateTime);
            View underLine = (View) backgroundView.getTag();
            // Set it
            if (isShow != null && isShow == 1) {
                if (underLine != null) {
                    underLine.setVisibility(View.VISIBLE);
                }
            } else {
                if (underLine != null) {
                    underLine.setVisibility(View.GONE);
                }
            }
        }
    }


    private void resetCustomResources(CellView cellView) {
        cellView.setBackgroundResource(defaultCellBackgroundRes);
        cellView.setTextColor(defaultTextColorRes);
    }

    public void setAdapterDateTime(DateTime curdateTime) {
        this.dateTime = curdateTime;
        LogHelper.d(TAG,"setAdapterDate: "+dateTime.toString(CaldroidCustomConstant.simpleFormator));
        this.month = dateTime.getMonthOfYear();
        this.year = dateTime.getYear();
        this.day = dateTime.getDayOfMonth();
        if (viewMode == ViewMode.MONTH) {
            this.datetimeList = CalendarHelper.getFullWeeks(this.month, this.year,
                    1, true);
            if(datetimeList.size() != 42){
                LogHelper.e(TAG,"datetimeList.size() != 42");
                this.datetimeList = CalendarHelper.getFullWeeks(this.month, this.year,
                        1, true);
                if(datetimeList.size() != 42) {
                    LogHelper.e(TAG, "getDataList again   datetimeList.size() != 42");
                }
            }
        } else {
            this.datetimeList = CalendarHelper.getFullWeek(this.day, this.month, this.year,
                    1, false);
        }
        if (viewMode == ViewMode.WEEK) {
            minDateTime = dateTime.dayOfWeek().withMinimumValue().millisOfDay().withMinimumValue();
            maxDateTime = dateTime.dayOfWeek().withMaximumValue().millisOfDay().withMaximumValue();
        } else {
            minDateTime = dateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue();
            maxDateTime = dateTime.dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue();
        }

    }

    public ArrayList<DateTime> getDatetimeList() {
        return datetimeList;
    }

    public void setCaldroidData(Map<String, Object> caldroidData) {
        this.caldroidData = caldroidData;
        populateFromCaldroidData();
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public void updateToday() {
        today = CalendarHelper.convertDateToDateTime(new Date());
    }

    protected DateTime getToday() {
        if (today == null) {
            today = CalendarHelper.convertDateToDateTime(new Date()).withTime(0,0,0,0);
        }
        return today;
    }

    public void initWithSpecialDate(DateTime dateTime, Map<String, Object> caldroidData, Map<String, Object> extraData) {
        LogHelper.d(TAG,"initWithSpecialDate "+dateTime.toString(CaldroidCustomConstant.simpleFormator));
        this.dateTime = dateTime;
        this.day = dateTime.getDayOfMonth();
        this.month = dateTime.getMonthOfYear();
        this.year = dateTime.getYear();
        this.caldroidData = caldroidData;
        this.extraData = extraData;
        populateFromCaldroidData();
        notifyDataSetChanged();
    }

    public void initWithSpecialDateForMonthView(DateTime currentDateTime, Map<String, Object> caldroidData, Map<String, Object> extraData) {
        LogHelper.d(TAG,"initWithSpecialDateForMonthView "+currentDateTime.toString(CaldroidCustomConstant.simpleFormator));
        this.dateTime = currentDateTime;
        this.year = currentDateTime.getYear();
        this.month = currentDateTime.getMonthOfYear();
        this.day = currentDateTime.getDayOfMonth();
        this.caldroidData = caldroidData;
        this.extraData = extraData;
        populateFromCaldroidData();
        notifyDataSetChanged();
    }

    public DateTime getDateTime() {
        return dateTime;
    }

//    public void setDateTime(DateTime dateTime) {
//        this.dateTime = dateTime;
//        this.year = dateTime.getYear();
//        this.month = dateTime.getMonthOfYear();
//        this.day = dateTime.getDayOfMonth();
//    }
}
