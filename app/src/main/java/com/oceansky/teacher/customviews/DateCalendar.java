package com.oceansky.teacher.customviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.a.a.V;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.CaldroidCustomConstant;
import com.oceansky.teacher.customviews.adapter.CaldroidGridAdapter;
import com.oceansky.teacher.customviews.adapter.CoursesPageChangedLister;
import com.oceansky.teacher.customviews.adapter.DatePageChangeListener;
import com.oceansky.teacher.customviews.adapter.DateWeekPageChangeListener;
import com.oceansky.teacher.customviews.adapter.InfiniteCoursePageAdapter;
import com.oceansky.teacher.customviews.adapter.InfinitePagerAdapter;
import com.oceansky.teacher.customviews.adapter.MonthWeekPagerAdapter;
import com.oceansky.teacher.fragments.DateGridFragment;
import com.oceansky.teacher.fragments.TimeTableFragment;
import com.oceansky.teacher.listeners.CaldroidListener;
import com.oceansky.teacher.listeners.SimpleAnimatorListener;
import com.oceansky.teacher.manager.TeacherCourseManager;
import com.oceansky.teacher.utils.CalendarHelper;
import com.oceansky.teacher.utils.DisplayUtils;
import com.oceansky.teacher.utils.LogHelper;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 周月视图类
 * User: 王旭国
 * Email:wangxuguo@jhyx.com.cn
 */
public class DateCalendar extends FrameLayout {

    private static final String tag = DateCalendar.class.getSimpleName();

    public CalendarViewPager monthCalendar;
    public CalendarViewPager weekCalendar;

    private ViewMode viewMode = ViewMode.WEEK;
    private SimpleDateFormat         simpleDateFormat;
    private DateFormat               dateFormator;
    private OnViewModeChangeListener onViewModeChnangeListener;

    private Context           context;
    private TimeTableFragment fragment;
    private int               year, month, day;
    private ArrayList<CaldroidGridAdapter>  monthDatePagerAdapters;
    private ArrayList<CaldroidGridAdapter>  weekDatePagerAdapters;
    private ArrayList<DateTime>             dateInMonthsList;
    private ArrayList<DateTime>             dateInWeekList;
    private DatePageChangeListener          monthPageListener;
    private DateWeekPageChangeListener      weekPageListener;
    private AdapterView.OnItemClickListener dateMonthItemClickListener;
    private AdapterView.OnItemClickListener dateWeekItemClickListener;
    private boolean enableClickOnDisabledDates = false;
    /**
     * 视图ViewPager切换回调
     */
    private PageChangeListener pageChangeListener;
    /**
     * caldroidListener inform library client of the event happens inside
     * Caldroid
     */
    private CaldroidListener caldroidListener = new CaldroidListener() {
        @Override
        public void onSelectMonthDate(DateTime date, View view, boolean isFromContentView) {
            if (view != null) {//不可点击的本月前后日期
                TextView tv1 = (TextView) view.findViewById(R.id.calendar_tv);
                int color = tv1.getCurrentTextColor();
                if (color == context.getResources().getColor(R.color.caldroid_darker_gray)
                        || color == context.getResources().getColor(R.color.white)) {
                    return;
                }
            }
//            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            clearSelectedDates();
            curSelectedDateTime = date;
            if (!isFromContentView) {
                refreshTeacherCourse(curSelectedDateTime);
            }
            setSelectedDate(CalendarHelper.convertDateTimeToDate(curSelectedDateTime));
            refreshMonthView();
            if (pageChangeListener != null) {
                pageChangeListener.onPageChanged(curSelectedDateTime);
            }
//            refreshLesson(date, dateStr);
        }

        @Override
        public void onSelectWeekDate(DateTime date, View view, boolean isFromContentView) {
            if (view != null) {
                TextView tv1 = (TextView) view.findViewById(R.id.calendar_tv);
                int color = tv1.getCurrentTextColor();
                if (color == context.getResources().getColor(R.color.caldroid_darker_gray)
                        || color == context.getResources().getColor(R.color.white)) {
                    return;
                }
            }
//            String dateStr = android.text.format.DateFormat.format("yyyyMMdd", date).toString();
            clearSelectedDates();
            curSelectedDateTime = date;
            if (!isFromContentView) {
                refreshTeacherCourse(curSelectedDateTime);
            }
            setSelectedDate(CalendarHelper.convertDateTimeToDate(curSelectedDateTime));
            refreshWeeksView();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            super.onChangeMonth(month, year);

        }
    };


    private int themeResource = R.style.CaldroidDefault;

    private EventBus eventBus;
    protected ArrayList<DateTime> disableDates  = new ArrayList<DateTime>();
    protected ArrayList<DateTime> selectedDates = new ArrayList<DateTime>();
    protected DateTime minDateTime;
    protected DateTime maxDateTime;
    /**
     * 当前选定的日期，周或者月划动切换或者点击选择选定日期切换，进行变化
     */
    protected DateTime curSelectedDateTime;
//    protected Date curSelectedDate;//Date  DateTime 同一个，冲突，仅使用一个
    /**
     * caldroidData belongs to Caldroid
     */
    protected Map<String, Object>     caldroidData             = new HashMap<String, Object>();
    /**
     * extraData belongs to client
     */
    protected Map<String, Object>     extraData                = new HashMap<String, Object>();
    /**
     * backgroundForDateMap holds background resource for each date
     */
    protected Map<DateTime, Drawable> backgroundForDateTimeMap = new HashMap<DateTime, Drawable>();

    /**
     * textColorForDateMap holds color for text for each date
     */
    protected Map<DateTime, Integer> textColorForDateTimeMap = new HashMap<DateTime, Integer>();

    /**
     * textDotForDateTimeMap holds color for text for each date
     */
    protected Map<DateTime, Integer> textDotForDateTimeMap = new HashMap<DateTime, Integer>();

    int[] locationInWindow = new int[2];
    //每次按下后距离父控件的距离
    float downY;
    //每次移动开始的距离
    float startScrolY;
    //快速滑动事件计数
    int fastFlingCount = 0;
    VelocityTracker velocityTracker;
    //Y轴方向累计距离
    float curScrollY = 0;
    //当前选中的日期可以划动的设置高度的距离
    float curScrollHeight;
    //sety 最小值
    float minScrollY = 0;
    private CoursesPageChangedLister  coursePageChangedListener;
    /**
     *
     */
    private ValueAnimator             localValueAnimator;
    private ValueAnimator             yChangeValueAnimator;
    private InfiniteCoursePageAdapter infiniteCoursePageAdapter;
    /**
     * 操作事件的第一次划动，获取信息
     */

    private boolean isFirstMotionEvent = true;
    /**
     * 日历行高
     */
//    private int rowHeight;

    /**
     * 选中行的位置高度
     */
//    private int selectedTopY;
//    private int selectedBottomY;
    public void clearSelectedDates() {
        selectedDates.clear();
    }

    public DateCalendar(Context paramContext) {
        super(paramContext);
        initComponent(paramContext);
    }

    public DateCalendar(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initComponent(paramContext);
    }

    public DateCalendar(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        initComponent(paramContext);
    }

    protected void initComponent(Context paramContext) {
        context = paramContext;
        ViewMode localViewMode;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD");
        dateFormator = DateFormat.getDateInstance(DateFormat.MEDIUM);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            localViewMode = ViewMode.WEEK;
        } else {
            localViewMode = ViewMode.MONTH;
        }
        this.viewMode = localViewMode;
        eventBus = EventBus.getDefault();
    }

    public void init(ViewMode viewmode) {
        monthCalendar = (CalendarViewPager) findViewById(R.id.calendar_view_month);
        weekCalendar = (CalendarViewPager) findViewById(R.id.calendar_view_week);
        this.viewMode = viewmode;
        if (ViewMode.MONTH == viewMode) {
            monthCalendar.setVisibility(VISIBLE);
//            weekCalendar.setVisibility(View.GONE);
            weekCalendar.setEnabled(false);
        } else {
//            monthCalendar.setVisibility(View.GONE);
            monthCalendar.setEnabled(false);
            weekCalendar.setVisibility(VISIBLE);
        }
        final MonthWeekPagerAdapter monthPagerAdapters = new MonthWeekPagerAdapter(fragment.getChildFragmentManager());
        final MonthWeekPagerAdapter weekPagerAdapters = new MonthWeekPagerAdapter(fragment.getChildFragmentManager());
        ArrayList<DateGridFragment> monthfragments = monthPagerAdapters.getFragments();
        ArrayList<DateGridFragment> weekfrgments = weekPagerAdapters.getFragments();
        monthDatePagerAdapters = new ArrayList<>();
        weekDatePagerAdapters = new ArrayList<>();
        // Setup adapters for the grid views
        // Current month

        if (curSelectedDateTime == null) {
            curSelectedDateTime = DateTime.now().withTime(0, 0, 0, 0);
            year = curSelectedDateTime.getYear();
            month = curSelectedDateTime.getMonthOfYear();
            day = curSelectedDateTime.getDayOfMonth();
        }
        setSelectedDate(CalendarHelper.convertDateTimeToDate(curSelectedDateTime));
        LogHelper.i(tag, "year : " + year + " month: " + month + " day " + day);
        DateTime currentDateTime = new DateTime(year, month, day, 0, 0, 0); //DateTime.now(TimeZone.getDefault());
        CaldroidGridAdapter adapter0 = getNewDatesGridAdapter(currentDateTime.getDayOfMonth(),
                currentDateTime.getMonthOfYear(), currentDateTime.getYear());
        CaldroidGridAdapter weekadapter0 = getNewWeekDatesGridAdapter(currentDateTime.getDayOfMonth(),
                currentDateTime.getMonthOfYear(), currentDateTime.getYear());
        monthPageListener = new DatePageChangeListener(this);
        monthPageListener.setCurrentDateTime(currentDateTime);
        weekPageListener = new DateWeekPageChangeListener(this);
        weekPageListener.setCurrentDateTime(currentDateTime);
        // Setup dateInMonthsList
        dateInMonthsList = adapter0.getDatetimeList();
        dateInWeekList = weekadapter0.getDatetimeList();
        monthCalendar.setDates(dateInMonthsList);
        weekCalendar.setDates(dateInWeekList);
        // Next month
        DateTime nextDateTime = currentDateTime.plusMonths(1);
        CaldroidGridAdapter adapter1 = getNewDatesGridAdapter(nextDateTime.getDayOfMonth(),
                nextDateTime.getMonthOfYear(), nextDateTime.getYear());
        // Next week
        DateTime nextweekDateTime = currentDateTime.plusWeeks(1);
        CaldroidGridAdapter weekadapter1 = getNewWeekDatesGridAdapter(nextweekDateTime.getDayOfMonth(),
                nextweekDateTime.getMonthOfYear(), nextweekDateTime.getYear());

        // Next 2 monthOnI
        DateTime next2DateTime = nextDateTime.plusMonths(1);
        CaldroidGridAdapter adapter2 = getNewDatesGridAdapter(next2DateTime.getDayOfMonth(),
                next2DateTime.getMonthOfYear(), next2DateTime.getYear());
        // Next 2 week
        DateTime next2weekDateTime = nextweekDateTime.plusWeeks(1);
        CaldroidGridAdapter weekadapter2 = getNewWeekDatesGridAdapter(next2weekDateTime.getDayOfMonth(),
                next2weekDateTime.getMonthOfYear(), next2weekDateTime.getYear());

        // Previous month
        DateTime prevDateTime = currentDateTime.minusMonths(1);
        CaldroidGridAdapter adapter3 = getNewDatesGridAdapter(prevDateTime.getDayOfMonth(),
                prevDateTime.getMonthOfYear(), prevDateTime.getYear());
        // Previous week
        DateTime prevweekDateTime = currentDateTime.minusWeeks(1);
        CaldroidGridAdapter weekadapter3 = getNewWeekDatesGridAdapter(prevweekDateTime.getDayOfMonth(),
                prevweekDateTime.getMonthOfYear(), prevweekDateTime.getYear());

        // Add to the array of adapters
//        ArrayList<CaldroidGridAdapter> datePagerAdapters =new ArrayList<>();
        monthDatePagerAdapters.add(adapter0);
        monthDatePagerAdapters.add(adapter1);
        monthDatePagerAdapters.add(adapter2);
        monthDatePagerAdapters.add(adapter3);
        weekDatePagerAdapters.add(weekadapter0);
        weekDatePagerAdapters.add(weekadapter1);
        weekDatePagerAdapters.add(weekadapter2);
        weekDatePagerAdapters.add(weekadapter3);
        monthPageListener.setCaldroidGridAdapters(monthDatePagerAdapters);
        weekPageListener.setCaldroidGridAdapters(weekDatePagerAdapters);
        for (int i = 0; i < CaldroidCustomConstant.NUMBER_OF_PAGES; i++) {
            DateGridFragment dateGridFragment = monthfragments.get(i);
            CaldroidGridAdapter adapter = monthDatePagerAdapters.get(i);
            dateGridFragment.setGridViewRes(getGridViewRes());
            dateGridFragment.setGridAdapter(adapter);
            dateGridFragment.setOnItemClickListener(getMonthDateItemClickListener());
//            dateGridFragment
//                    .setOnItemLongClickListener(getDateItemLongClickListener());
        }
        for (int i = 0; i < CaldroidCustomConstant.NUMBER_OF_PAGES; i++) {
            DateGridFragment dateGridFragment = weekfrgments.get(i);
            CaldroidGridAdapter adapter = weekDatePagerAdapters.get(i);
            dateGridFragment.setGridViewRes(getGridViewRes());
            dateGridFragment.setGridAdapter(adapter);
            dateGridFragment.setOnItemClickListener(getWeekDateItemClickListener());
//            dateGridFragment
//                    .setOnItemLongClickListener(getDateItemLongClickListener());
        }
        InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(
                monthPagerAdapters);
        monthCalendar.setEnabled(true);
        monthCalendar.setDates(dateInMonthsList);
        monthCalendar.setAdapter(infinitePagerAdapter);
        monthCalendar.addOnPageChangeListener(monthPageListener);
        InfinitePagerAdapter infiniteWeekPagerAdapter = new InfinitePagerAdapter(
                weekPagerAdapters);
        weekCalendar.setEnabled(true);
        weekCalendar.setDates(dateInWeekList);
        weekCalendar.setAdapter(infiniteWeekPagerAdapter);
        weekCalendar.addOnPageChangeListener(weekPageListener);
//        getLayoutParams().height = monthCalendar.getHeight();

        LogHelper.d(tag, "DateCalendar init finished");
    }


    private int[] getLocation() {
        int[] arrayOfInt = new int[2];
        getLocationInWindow(arrayOfInt);
//        getLocationOnScreen();
        return arrayOfInt;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (h < DisplayUtils.dip2px(context, 20)) {
            if (monthCalendar.getRowHeight() > DisplayUtils.dip2px(context, 20)) {
                getLayoutParams().height = monthCalendar.getRowHeight();
            } else if (weekCalendar.getRowHeight() > DisplayUtils.dip2px(context, 20)) {
                getLayoutParams().height = weekCalendar.getRowHeight();
            } else {
                getLayoutParams().height = DisplayUtils.dip2px(context, 39);
            }
            LogHelper.d(tag, "weekCalendarRowHeight: " + weekCalendar.getRowHeight() + " RowHeight: " + monthCalendar.getRowHeight() + "  " + getLayoutParams().height);
            LogHelper.d(tag, "onSizeChanged : w: " + oldw + "---->" + w + "  h: " + oldh + "----> " + h + "   weekCalendar:  " + DisplayUtils.dip2px(context, 39));
            weekCalendar.setVisibility(VISIBLE);
            weekCalendar.invalidate();
            setLayoutParams(getLayoutParams());
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        locationInWindow = getLocation();
        int deltaX = (int) event.getRawX() - locationInWindow[0];//相对于控件内部 的顶间距
        int deltaY = (int) event.getRawY() - locationInWindow[1];
        LogHelper.d(tag, "locationInWindow[0]: " + locationInWindow[0] + " locationInWindow[1]: " + locationInWindow[1] +
                "event.getRawX: " + event.getRawX() + " event.getRawY: " + event.getRawY() + "dX: " + deltaX + " dY: " + deltaY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogHelper.d(tag, "ACTION_DOWN: " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                fastFlingCount = 0;
                getCurScrollHeightScrollInfo();
                velocityTracker.clear();
                downY = startScrolY = deltaY;
                break;
            case MotionEvent.ACTION_UP:
                LogHelper.d(tag, "ACTION_UP: " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                LogHelper.d(tag, "velocityTracker  XvelocityTracker : " + velocityTracker.getXVelocity() + " YVelocityTracker: " + velocityTracker.getYVelocity());
                if (monthCalendar.getHeight() > 0 && Math.abs(curScrollY) >= monthCalendar.getHeight() - monthCalendar.getRowHeight()) {
                    onEvent(new PullCalEvent(true));
                } else {
                    onEvent(new PullCalEvent(false));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                LogHelper.d(tag, "ACTION_MOVE " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                if ((Math.abs(velocityTracker.getYVelocity()) < 50) || Math.abs(velocityTracker.getXVelocity()) >= Math.abs(velocityTracker.getYVelocity())) {
                    return super.dispatchTouchEvent(event);
                }
                // 根据当前视图状态，计算  判断对当前的MOVE事件是否进行处理

                LogHelper.d(tag, "velocityTracker  XvelocityTracker : " + velocityTracker.getXVelocity() + " YVelocityTracker: " + velocityTracker.getYVelocity());

                if (velocityTracker.getYVelocity() > 0 && curScrollY >= 0 && viewMode == ViewMode.MONTH
                        || velocityTracker.getYVelocity() < 0 && curScrollY <= 0 && viewMode == ViewMode.WEEK) {
                    return super.onTouchEvent(event);
                } else {
                    //有效的滑动
                    LogHelper.i(tag, "有效的滑动");
                }
                if (velocityTracker.getYVelocity() > -1000f && velocityTracker.getYVelocity() < 1000) {
                    float dy = deltaY - startScrolY;
                    LogHelper.d(tag, "dy: " + dy);
                    onEvent(new PullCalEvent(dy));
                } else {
                    float dy = deltaY - startScrolY;
                    fastFlingCount++;
                    LogHelper.d(tag, "YVelocity > 1000 dy: " + dy);

                    onEvent(new PullCalEvent(dy));////???????如何处理快速滑动的手势事件？？？？？？
                }
                startScrolY = deltaY;
                break;
            case MotionEvent.ACTION_CANCEL:
                LogHelper.d(tag, "ACTION_CANCEL " + " X:  " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                break;
            default:
                LogHelper.d(tag, "default " + "  X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Subscribe
    public void onEvent(PullCalEvent pullCalAgent) {
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        if (pullCalAgent.isEnded) {
            isFirstMotionEvent = true;
            LogHelper.d(tag, "PullEnd: " + pullCalAgent.threasholdReached + " " + viewMode + "  getLayoutParams().height:" + getLayoutParams().height + " curScrollY:" + curScrollY + " monthCalendar.getY: " + monthCalendar.getY()
                    + " minScrollY: " + minScrollY + " curScrollHeight: " + curScrollHeight);
            if (pullCalAgent.threasholdReached) {  //阈值  是否完成可以向对方划动的条件，切换到另一个视图
                if (viewMode == ViewMode.MONTH) {
                    // 此时移动已经完成，   只需要设置visiable或者gone
                    monthCalendar.setY(minScrollY);
                    getLayoutParams().height = monthCalendar.getRowHeight();
                    monthCalendar.setVisibility(View.GONE);
                    weekCalendar.setVisibility(View.VISIBLE);
                    viewMode = ViewMode.WEEK;
                    onViewModeChangedEvent(viewMode);
                    setLayoutParams(getLayoutParams());
                } else {
                    // 此时移动已经完成，   只需要设置visiable或者gone
                    monthCalendar.setY(0);
                    getLayoutParams().height = monthCalendar.getHeight();
                    weekCalendar.setVisibility(View.GONE);
                    monthCalendar.setVisibility(View.VISIBLE);
                    viewMode = ViewMode.MONTH;
                    onViewModeChangedEvent(viewMode);
                    setLayoutParams(getLayoutParams());
                }

            } else {//需要判断是否进行向另一页面滑动，或者回退到当前页面
                LogHelper.d(tag, "滑动已经结束，需要判断如何动画，回退还是切换到另一界面");
                if (viewMode == ViewMode.WEEK) {  //周视图下，只要下拉超过一个行距就默认可以滑动到月视图
                    if (curScrollY > monthCalendar.getRowHeight()) {//切换到月视图
                        LogHelper.d(tag, "切换到月视图   切换到月视图");
                        pullEndAnimateToMonthView();
                    } else if (curScrollY == 0) {   //周视图，未达有效移动，还是周视图
                        LogHelper.d(tag, "curScrollY = 0  do nothing   viewMode " + viewMode);
                        if (viewMode == ViewMode.WEEK) {
                            if (monthCalendar.getVisibility() != GONE) {
                                monthCalendar.setVisibility(GONE);
                            }
                            if (weekCalendar.getVisibility() != VISIBLE) {
                                weekCalendar.setVisibility(VISIBLE);
                            }
                            if (getLayoutParams().height <= 0) {   //最开始始，返回-2 有具体数值的高度
                                LogHelper.e(tag, "getLayoutParams().height 0 ");
                                if (monthCalendar.getHeight() == 0) {
                                    LogHelper.w(tag, "monthCalendar.getHeight()=0");
                                    if (monthCalendar.getRowHeight() == 0) {
                                        LogHelper.w(tag, "monthCalendar.getRowHeight() = 0");

                                    } else {
                                        LogHelper.w(tag, "monthCalendar.getRowHeight() != 0");
//                                    monthCalendar
//                                        getLayoutParams().height = monthCalendar.getRowHeight() * 6;

                                    }
                                } else {
                                    getLayoutParams().height = weekCalendar.getHeight();
                                    setLayoutParams(getLayoutParams());
                                }
                            } else if (getLayoutParams().height != monthCalendar.getRowHeight()) {
                                LogHelper.d(tag, "getLayoutParams().height != monthCalendar.getRowHeight()");
                                getLayoutParams().height = weekCalendar.getHeight();
                                setLayoutParams(getLayoutParams());
                            } else {
                                getLayoutParams().height = weekCalendar.getHeight();
                                setLayoutParams(getLayoutParams());
                                weekCalendar.setVisibility(View.VISIBLE);
                                monthCalendar.setVisibility(GONE);

                            }
                        } else {//viewMode MONTH
                            if (monthCalendar.getVisibility() != VISIBLE) {
                                monthCalendar.setVisibility(VISIBLE);
                            }
                            if (weekCalendar.getVisibility() != GONE) {
                                weekCalendar.setVisibility(GONE);
                            }
                        }
                    } else {//回退到周视图
                        LogHelper.d(tag, "回退到周视图");
                        pullEndAnimateToWeekView();
                    }


                } else {
                    LogHelper.d(tag, "滑动已经结束，月视图切换判断");
                    LogHelper.d(tag, "PullEnd: getLayoutParams().height:" + getLayoutParams().height + " monthCalendar.getY: " + monthCalendar.getY());
                    if (Math.abs(curScrollY) > 200) {
                        LogHelper.d(tag, "切换到周视图");
                        monthCalendar.setY(minScrollY);
                        if (monthCalendar.getRowHeight() == weekCalendar.getHeight()) {
                            getLayoutParams().height = monthCalendar.getRowHeight();
                        } else if (weekCalendar.getHeight() != 0) {
                            getLayoutParams().height = weekCalendar.getHeight();
                        }
                        if (monthCalendar.getHeight() != 0 && monthCalendar.getRowHeight() != 0 && getLayoutParams().height > monthCalendar.getRowHeight()) {
                            getLayoutParams().height = monthCalendar.getRowHeight();
                        }
                        LogHelper.d(tag, "getLayoutParams().height : " + getLayoutParams().height);
                        monthCalendar.setVisibility(View.GONE);
                        weekCalendar.setVisibility(View.VISIBLE);
                        viewMode = ViewMode.WEEK;
                        onViewModeChangedEvent(viewMode);
                        setLayoutParams(getLayoutParams());
                    } else if (curScrollY == 0) {
                        LogHelper.d(tag, "curScrollY = 0  do nothing");
                        if (monthCalendar.getVisibility() != VISIBLE) {
                            monthCalendar.setVisibility(VISIBLE);
                        }
                        if (getLayoutParams().height == 0) {
                            LogHelper.e(tag, "getLayoutParams().height 0 ");
                            if (monthCalendar.getHeight() == 0) {
                                LogHelper.w(tag, "monthCalendar.getHeight()=0");
                                if (monthCalendar.getRowHeight() == 0) {
                                    LogHelper.w(tag, "monthCalendar.getRowHeight() = 0");

                                } else {
//                                    monthCalendar
                                    getLayoutParams().height = monthCalendar.getRowHeight() * 6;

                                }
                            } else {
                                getLayoutParams().height = monthCalendar.getHeight();
                                setLayoutParams(getLayoutParams());
                            }
                        } else {

                        }
                    } else {  //回退到周视图
                        LogHelper.d(tag, "回退到月视图  回退月视图 无动画");
//                        pullEndAnimateToWeekView();
                        monthCalendar.setY(0);
                        getLayoutParams().height = monthCalendar.getHeight();
                        setLayoutParams(getLayoutParams());
                        monthCalendar.setVisibility(View.VISIBLE);
                        weekCalendar.setVisibility(View.GONE);
                    }
                }
            }
            curScrollY = 0;
            fastFlingCount = 0;
        } else {
            expandCollapse(pullCalAgent.distance);
        }

    }

    /**
     * 周视图向月视图切换，返回到周视图
     */
    private void pullEndAnimateToWeekView() {
        LogHelper.i(tag, "pullEndAnimateToWeekView curScrollY: " + curScrollY + " curScrollHeight " + curScrollHeight + " monthCalendar.getY: " + monthCalendar.getY()
                + " monthCalendar.getHeight():" + monthCalendar.getHeight());
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        if (curScrollY > 0) {
            LogHelper.d(tag, "pullEndAnimateToWeekView: curScrollY > 0");
            if (curScrollY < curScrollHeight) {
                final float[] arrayOfFloat = new float[2];
                arrayOfFloat[0] = curScrollY;// getLayoutParams().height;
                arrayOfFloat[1] = 0; //monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7)+monthCalendar.getY();
                LogHelper.i(tag, "animate  animateToMonthViwe: " + arrayOfFloat[0] + "  --->  " + arrayOfFloat[1]);
                localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
                localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // TODO Auto-generated method stub
                        getLayoutParams().height = monthCalendar.getRowHeight() + ((Float) animation.getAnimatedValue()).intValue();
                        LogHelper.d(tag, "animate  getLayoutParams().height: " + getLayoutParams().height);
                        setLayoutParams(getLayoutParams());
                    }
                });
                localValueAnimator.setDuration(125);
                localValueAnimator.setInterpolator(new LinearInterpolator());
                localValueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (viewMode == ViewMode.MONTH) {  //如果 当前不是月视图，就不继发当前动画
                            if (monthCalendar.getY() < 0) {
                                animateYChange(false, (int) monthCalendar.getY(), 0, monthCalendar.getHeight());
                            } else {
                                weekCalendar.setVisibility(View.GONE);
                            }
                            viewMode = ViewMode.WEEK;
                            onViewModeChangedEvent(ViewMode.WEEK);
                        } else {//周模式返回到周模式
                            weekCalendar.setVisibility(View.VISIBLE);
                            monthCalendar.setVisibility(View.GONE);
                        }
                    }
                });
//                weekCalendar.setVisibility(View.GONE);
                localValueAnimator.start();
            } else {
                animateYChange(false, (int) (monthCalendar.getY()), (int) minScrollY, getLayoutParams().height);
            }
        } else {//curScrollY < 0 //
            LogHelper.d(tag, " curScrollY < 0 回退到周视图");
            if (Math.abs(curScrollY) < curScrollHeight) {
                final float[] arrayOfFloat = new float[2];
                arrayOfFloat[0] = getLayoutParams().height;// getLayoutParams().height;
                arrayOfFloat[1] = weekCalendar.getHeight(); //monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7)+monthCalendar.getY();
                LogHelper.i(tag, "animate  animateToWeekViwe: " + arrayOfFloat[0] + "  --->  " + arrayOfFloat[1]);
                localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
                localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // TODO Auto-generated method stub
                        getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                        LogHelper.d(tag, "animate  getLayoutParams().height: " + getLayoutParams().height);
                        setLayoutParams(getLayoutParams());
                    }
                });
                localValueAnimator.setDuration(100);
                localValueAnimator.setInterpolator(new LinearInterpolator());
                localValueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewMode = ViewMode.WEEK;
                        onViewModeChangedEvent(ViewMode.WEEK);
                        weekCalendar.setVisibility(VISIBLE);
                        monthCalendar.setVisibility(GONE);
                        monthCalendar.setY(minScrollY);
                    }
                });
                localValueAnimator.start();
            } else {
                LogHelper.d(tag, "回退到周视图，无动画");
                getCurScrollHeightScrollInfo();
                monthCalendar.setY(minScrollY);
                monthCalendar.setVisibility(GONE);
                weekCalendar.setVisibility(VISIBLE);
                getLayoutParams().height = weekCalendar.getHeight();
                setLayoutParams(getLayoutParams());
            }
        }
    }

    /**
     * 手势滑动停止，滑动到相应视图
     */
    private void pullEndAnimateToMonthView() {
        LogHelper.i(tag, "pullEndAnimateToMonthView curScrollY: " + curScrollY + " curScrollHeight " + curScrollHeight + " monthCalendar.getY: " + monthCalendar.getY()
                + " monthCalendar.getHeight():" + monthCalendar.getHeight() + "minScrollY: " + minScrollY + "getLayoutParams().height:" + getLayoutParams().height);
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        if (curScrollY > 0) {//向下滑动
            if (curScrollY > curScrollHeight) {
                boolean isAnimationToMonthView = true;
                int starty = getLayoutParams().height - monthCalendar.getHeight();
                if (starty >= 0) {
                    starty = -1;
                }
                animateYChange(isAnimationToMonthView, starty, 0, monthCalendar.getHeight());//getLayoutParams().height//monthCalendar.getY()+(-minScrollY))//(int) (monthCalendar.getHeight()+monthCalendar.getY())
                viewMode = ViewMode.MONTH;
                onViewModeChangedEvent(ViewMode.MONTH);
            } else {
                int ychange = (int) (curScrollHeight - curScrollY);
                final float[] arrayOfFloat = new float[2];
                if (localValueAnimator != null && localValueAnimator.isRunning()) {
                    localValueAnimator.end();
                    localValueAnimator = null;
                }
                arrayOfFloat[0] = getLayoutParams().height;//curScrollY;//startScrolY;//;//
                arrayOfFloat[1] = curScrollHeight;//curScrollHeight - monthCalendar.getRowHeight();//; //monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7)+monthCalendar.getY();
                ////这进行判断  检查有效性
                if (monthCalendar.getY() < 0) {//Y 的动画还有，


                } else {//没有Y的动画了，最后的 arrayOfFloat[1] 就是 最后的
                    if (arrayOfFloat[1] < monthCalendar.getHeight() && monthCalendar.getHeight() > 0) {
                        LogHelper.d(tag, "arrayOfFloat[1] = monthCalendar.getHeight()");
                        arrayOfFloat[1] = monthCalendar.getHeight();
                    }
                }
                LogHelper.i(tag, "animate  animateToMonthViwe: " + arrayOfFloat[0] + "  --->  " + arrayOfFloat[1]);
                localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
                localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // TODO Auto-generated method stub
                        getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                        LogHelper.d(tag, "animate  getLayoutParams().height: " + getLayoutParams().height);
                        setLayoutParams(getLayoutParams());
                    }
                });
                localValueAnimator.setDuration(250);
                localValueAnimator.setInterpolator(new LinearInterpolator());
                localValueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (monthCalendar.getY() < 0) {
                            animateYChange(false, (int) monthCalendar.getY(), 0, monthCalendar.getHeight());
                        } else {
                            weekCalendar.setVisibility(View.GONE);
                        }
                        viewMode = ViewMode.MONTH;
                        onViewModeChangedEvent(ViewMode.MONTH);
                    }
                });
                weekCalendar.setVisibility(View.GONE);
                localValueAnimator.start();
            }
        } else {
            LogHelper.e(tag, "pullEndAnimateToMonthView curScrollY <0");
            monthCalendar.setVisibility(VISIBLE);
            weekCalendar.setVisibility(View.GONE);
            getLayoutParams().height = monthCalendar.getHeight();
            setLayoutParams(getLayoutParams());
//            if(Math.abs(curScrollY)>curScrollHeight){
//                animateYChange((int) (monthCalendar.getY()+curScrollY-curScrollHeight),0,getLayoutParams().height);
//            }else{
//
//            }
        }
    }

    /**
     * 根据手势移动Y距离收缩日历
     * 先移动选定日历上间距，再移动下间距
     *
     * @param distance
     */
    private void expandCollapse(float distance) {//正为向下 负为向上
        LogHelper.d(tag, "expandCollapse: " + distance + " curScrollY: " + curScrollY + " curScrollHeight " + curScrollHeight
                + " monthHeight:" + monthCalendar.getHeight() + " Layout.height " + getLayoutParams().height);
//        selectedTopY = (int) curScrollHeight;
        curScrollY += distance;
        if (viewMode == ViewMode.WEEK && curScrollHeight == 0 && minScrollY == 0) {
            LogHelper.d(tag, "week视图下，未初使化状态  ");
            getCurScrollHeightScrollInfo();
            monthCalendar.setY(minScrollY);
            LogHelper.d(tag, "checkuseful: " + curScrollHeight + "  " + minScrollY + "   " + monthCalendar.getRowHeight() + "  " + weekCalendar.getRowHeight());
            if (monthCalendar.getRowHeight() == 0 && monthCalendar.getHeight() == 0) {
                monthCalendar.setRowHeight(weekCalendar.getRowHeight());
                monthCalendar.getLayoutParams().height = weekCalendar.getRowHeight() * 6;
                monthCalendar.setLayoutParams(monthCalendar.getLayoutParams());
            }
        } else if (curScrollHeight == 0 && minScrollY == 0) {
            getCurScrollHeightScrollInfo();
        }
        if (isFirstMotionEvent) {
            isFirstMotionEvent = false;
            getCurScrollHeightScrollInfo();
        }
        if (monthCalendar.getHeight() != 0 && Math.abs(curScrollY) > monthCalendar.getHeight() - monthCalendar.getRowHeight()) {
            LogHelper.d(tag, "monthCalendar.getHeight()!=0&&Math.abs(curScrollY) > monthCalendar.getHeight() - monthCalendar.getRowHeight()");
            return;
        }
        if (Math.abs(curScrollY) > -minScrollY + curScrollHeight) {
            LogHelper.d(tag, "Math.abs(curScrollY)>-minScrollY+curScrollHeight");
            return;
        }
        if (curScrollY < 0) {  //需要考虑一种情况 ，先周下划到月然后再回退到周这种情况
            if (viewMode == ViewMode.WEEK) {
                monthCalendar.setY(minScrollY);
                LogHelper.d(tag, "curScrollY< 0 : 周下划到月然后再回退到周这种情况 " + distance + " curScrollY: " + curScrollY + " curScrollHeight " + curScrollHeight
                        + " monthHeight:" + monthCalendar.getHeight() + " Layout.height " + getLayoutParams().height + "minScrollY: " + minScrollY + " monthCalendar.getY: " + monthCalendar.getY());
                return;
            }
            if (Math.abs(curScrollY) <= curScrollHeight) {
                monthCalendar.setY(0);
                getLayoutParams().height = (int) (monthCalendar.getHeight() + curScrollY);
                setLayoutParams(getLayoutParams());
            } else {
                int dy = (int) (Math.abs(curScrollY) - curScrollHeight);
                if (dy > 0) {
                } else if (dy > -minScrollY) {
                    dy = (int) -minScrollY;
                } else {

                }
                monthCalendar.setY(-dy);
                getLayoutParams().height = (int) (monthCalendar.getHeight() + curScrollY);
                if (getLayoutParams().height <= monthCalendar.getRowHeight()) {
                    getLayoutParams().height = monthCalendar.getRowHeight();
                } else if (getLayoutParams().height >= monthCalendar.getHeight()) {
                    getLayoutParams().height = monthCalendar.getHeight();
                }
                setLayoutParams(getLayoutParams());
            }
        } else if (curScrollY == 0) {
            getCurScrollHeightScrollInfo();
        } else {  //这种情况 下，只有周祖图下会响应该动作 curScrolly默认开始为0 开始时判断一下
            if (viewMode == ViewMode.WEEK) {
                monthCalendar.setVisibility(View.VISIBLE);
                weekCalendar.setVisibility(View.GONE);
                LogHelper.d(tag, "getLayoutParams.height: " + getLayoutParams().height + " monthCalendar.getY: " + monthCalendar.getY());
                if (curScrollY <= curScrollHeight) {
                    getLayoutParams().height = (int) (monthCalendar.getRowHeight() + curScrollY);

                    setLayoutParams(getLayoutParams());
                } else {
                    int dy = (int) (curScrollY - curScrollHeight);   // - monthCalendar.getRowHeight());//滑动当前的日期周，不进行改动
                    if (dy > 0) {
                        int scroly = (int) minScrollY;//(int) monthCalendar.getY();
                        LogHelper.d(tag, "dy: " + dy + " scroly: " + scroly);

                        if (dy + scroly > 0) {
                            monthCalendar.setY(0);
                        } else {
                            monthCalendar.setY(dy + scroly);
                        }

                    }
                    getLayoutParams().height = (int) (monthCalendar.getRowHeight() + curScrollY);
                    if (getLayoutParams().height <= monthCalendar.getRowHeight()) {
                        getLayoutParams().height = monthCalendar.getRowHeight();
                    } else if (getLayoutParams().height >= monthCalendar.getHeight()) {
                        getLayoutParams().height = monthCalendar.getHeight();
                    }
                    setLayoutParams(getLayoutParams());
                }
            } else {
                LogHelper.e(tag, "当前不是周视图  已经到达月视图");  //这种情况 有可能是月视图向上划，然后再向上划动产生的，这种情况 需要重视    会导致layout高度突然变化
                //这种情况 月视图已经达到最大值，不能再增加

            }

        }
    }

    private void expandScrollY(float deltay) {
        this.monthCalendar.setY((int) (monthCalendar.getY() + deltay));
    }

    private void expandHeight(float deltay) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = layoutParams.height + (int) deltay;
        if (layoutParams.height > monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7)) {
            layoutParams.height = monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7);
        }
        setLayoutParams(layoutParams);
    }

    public void animateVisiable() {
        monthCalendar.setLayerType(2, null);
        monthCalendar.animate().alpha(1).x(1f).y(-monthCalendar.getRowHeight()).setDuration(250).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationCancelOrEnd(Animator animator) {
                monthCalendar.setLayerType(0, null);
            }
        }).start();
    }

    /**
     * 从月神图切换到周神图，首先月视图的高度变化 ，然后再变化其Y轴
     */
    public void animateToWeekView() {
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        final float[] arrayOfFloat = new float[2];
        arrayOfFloat[0] = this.getLayoutParams().height;  //getLayoutParams().height;//该值为-2？？
        if (arrayOfFloat[0] <= 0) {
            arrayOfFloat[0] = monthCalendar.getHeight();
        }
        //计算得到当前位置
        CaldroidGridAdapter curadaptor = monthDatePagerAdapters.get(monthPageListener.getCurrent(monthPageListener.getCurrentPage()));
        ArrayList<DateTime> datelist = curadaptor.getDatetimeList();
        DateTime cur = curadaptor.getDateTime();
        int position = 0;
        for (int i = 0; i < datelist.size(); i++) {
            DateTime dt = datelist.get(i);
            if (dt.equals(cur)) {
                position = i + 1;
                break;
            }
        }
        final int row = (int) Math.ceil(position % 7 == 0 ? position / 7 : (Math.floor(position / 7) + 1));
        int totalrow = datelist.size() / 7;
        LogHelper.i(tag, "animate : row: " + row + " totalrow: " + totalrow + " position: " + position + " datelist.size: " + datelist.size());

        arrayOfFloat[1] = arrayOfFloat[0] - monthCalendar.getRowHeight() * (totalrow - row);
        final int curHeight = monthCalendar.getRowHeight() * row;
        LogHelper.d(tag, "animate animateToWeekView " + arrayOfFloat[0] + "  " + arrayOfFloat[1]);
        localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
        localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // TODO Auto-generated method stub
                getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                LogHelper.d(tag, "animate getLayoutParams().height: " + getLayoutParams().height);
                setLayoutParams(getLayoutParams());
            }
        });
        localValueAnimator.setDuration(250);
        localValueAnimator.setInterpolator(new LinearInterpolator());
        localValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewMode = ViewMode.WEEK;
                onViewModeChangedEvent(ViewMode.WEEK);
                animateYChange(false, 0, -(row - 1) * monthCalendar.getRowHeight(), curHeight);
            }
        });
        localValueAnimator.start();
    }


    /**
     * 周月视图变化 时，月视图有一部分以y轴变化 的方式改变
     */
    private void animateYChange(final boolean isAnimateToMonthView, final int starty, final int endy, final int curMontHHeight) {
        LogHelper.d(tag, "animate animateYchange isAnimateToMonthView" + isAnimateToMonthView + " startY: " + starty + " endY: " + endy + " curMontHHeight: " + curMontHHeight);
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        float[] arrayOfFloat = new float[2];
        arrayOfFloat[0] = starty;
        arrayOfFloat[1] = endy;
        yChangeValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
        yChangeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // TODO Auto-generated method stub
                monthCalendar.setY(((Float) animation.getAnimatedValue()).intValue());
                if (isAnimateToMonthView) {
                    getLayoutParams().height = (int) (curMontHHeight + ((Float) animation.getAnimatedValue()).intValue());
                    LogHelper.d(tag, "AnimateToMonthView: " + getLayoutParams().height);
                    setLayoutParams(getLayoutParams());
                } else {
                    if (starty < 0) {
                        if ((curMontHHeight - starty) > monthCalendar.getHeight()) {
                            int startHeight = monthCalendar.getHeight() + starty;

                            getLayoutParams().height = (int) (startHeight + (-starty + ((Float) animation.getAnimatedValue()).intValue()));
                        } else {
                            getLayoutParams().height = (int) (curMontHHeight + (-starty + ((Float) animation.getAnimatedValue()).intValue()));
                        }

                    } else {
                        getLayoutParams().height = (int) (curMontHHeight + ((Float) animation.getAnimatedValue()).intValue());
                    }

                    LogHelper.d(tag, "animate getLayoutParams().height: " + getLayoutParams().height);
                    setLayoutParams(getLayoutParams());
                }
            }
        });
        yChangeValueAnimator.setDuration(250);
        yChangeValueAnimator.setInterpolator(new LinearInterpolator());
        yChangeValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (endy == 0) {
                    weekCalendar.setVisibility(View.GONE);
                    monthCalendar.setVisibility(View.VISIBLE);
                }
                if (starty == 0) {
                    monthCalendar.setVisibility(View.GONE);
                    weekCalendar.setVisibility(View.VISIBLE);
                }
                checkEndStatus();

            }
        });
        yChangeValueAnimator.start();
    }

    /**
     * 动画切换到月视图
     */
    public void animateToMonthView(int startLayoutHeight, int endLayoutHeight) {
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        if (yChangeValueAnimator != null) {
            if (yChangeValueAnimator.isRunning()) {
                yChangeValueAnimator.removeAllListeners();
                yChangeValueAnimator.end();
            }
            yChangeValueAnimator = null;
        }
        final float[] arrayOfFloat = new float[2];
        arrayOfFloat[0] = startLayoutHeight;// getLayoutParams().height;
        arrayOfFloat[1] = endLayoutHeight; //monthCalendar.getRowHeight() * (int) (monthCalendar.getDates().size() / 7)+monthCalendar.getY();
        if (monthCalendar.getY() < 0 && endLayoutHeight == monthCalendar.getHeight()) {
            arrayOfFloat[1] = monthCalendar.getHeight() + monthCalendar.getY();
        }
        LogHelper.i(tag, "animate  animateToMonthViwe: " + arrayOfFloat[0] + "  --->  " + arrayOfFloat[1]);
        localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
        localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // TODO Auto-generated method stub
                getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                LogHelper.d(tag, "animate  getLayoutParams().height: " + getLayoutParams().height);
                setLayoutParams(getLayoutParams());
            }
        });
        localValueAnimator.setDuration(250);
        localValueAnimator.setInterpolator(new LinearInterpolator());
        localValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (monthCalendar.getY() < 0) {//
                    int deltay = getLayoutParams().height - monthCalendar.getHeight();
                    animateYChange(false, deltay, 0, getLayoutParams().height);
//                    animateYChange(false, (int) monthCalendar.getY(), 0, getLayoutParams().height);
                } else {
                    weekCalendar.setVisibility(View.GONE);
                }
                viewMode = ViewMode.MONTH;
                onViewModeChangedEvent(ViewMode.MONTH);
            }
        });
        weekCalendar.setVisibility(View.GONE);
        localValueAnimator.start();
    }

    /**
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    public void refreshMonthView() {
        // If month and year is not yet initialized, refreshMonthView doesn't do
        // anything
        if (month == -1 || year == -1) {
            return;
        }
        // Refresh the date grid views
        for (CaldroidGridAdapter adapter : monthDatePagerAdapters) {
            // Reset caldroid data
            adapter.setCaldroidData(getCaldroidData());
            // Reset extra data
            adapter.setExtraData(extraData);
            // Update today variable
            adapter.updateToday();
            // Refresh view
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshWeeksView() {
        // If month and year is not yet initialized, refreshMonthView doesn't do
        // anything

        // Refresh the date grid views
        for (CaldroidGridAdapter adapter : weekDatePagerAdapters) {
            // Reset caldroid data
            adapter.setCaldroidData(getCaldroidData());
            // Reset extra data
            adapter.setExtraData(extraData);
            // Update today variable
            adapter.updateToday();
            // Refresh view
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 当选定日期后，日期对应 的课表发生变化
     *
     * @param curSelectedDateTime
     */
    public void refreshTeacherCourse(DateTime curSelectedDateTime) {
        if (coursePageChangedListener != null) {
            LogHelper.d(tag, "refreshTeacherCourse: " + curSelectedDateTime.toString(CaldroidCustomConstant.simpleFormator));
            coursePageChangedListener.setCurDateTime(curSelectedDateTime);
            if (infiniteCoursePageAdapter != null) {
                infiniteCoursePageAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 当前的选定日期改变后，周视图需要动态更新
     *
     * @param dateTime
     */
    public void refreshWeekViewToSelectedDateTime(DateTime dateTime) {
//        monthDatePagerAdapters.get()
        weekPageListener.changedToCur(dateTime);
    }

    /**
     * 周视图中，当前的选定日期改变后，月视图需要动态更新
     * 更新当前选中的日期，或者进行日期的切换
     *
     * @param dateTime
     */
    public void refreshMonthViewToSelectedDateTime(DateTime dateTime) {

        monthPageListener.changedSelectDateToCur(dateTime);//??????????????
    }

    /**
     * This method can be used to provide different gridview.
     *
     * @return
     */
    protected int getGridViewRes() {
        return R.layout.date_grid_fragment;
    }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public CaldroidGridAdapter getNewDatesGridAdapter(int day, int month, int year) {
        return new CaldroidGridAdapter(fragment.getActivity(), ViewMode.MONTH, day, month, year,
                getCaldroidData(), extraData);
    }

    public CaldroidGridAdapter getNewWeekDatesGridAdapter(int day, int month, int year) {
        return new CaldroidGridAdapter(fragment.getActivity(), ViewMode.WEEK, day, month, year,
                getCaldroidData(), extraData);
    }


    /**
     * caldroidData return data belong to Caldroid
     *
     * @return
     */
    public Map<String, Object> getCaldroidData() {
        caldroidData.clear();
//      caldroidData.put(CaldroidCustomConstant.CusDISABLE_DATES, disableDates);
        caldroidData.put(CaldroidCustomConstant.SELECTED_DATES, selectedDates);
        caldroidData.put(CaldroidCustomConstant._MIN_DATE_TIME, minDateTime);
        caldroidData.put(CaldroidCustomConstant._MAX_DATE_TIME, maxDateTime);
//      caldroidData.put(CaldroidCustomConstant.START_DAY_OF_WEEK, startDayOfWeek);
        caldroidData
                .put(CaldroidCustomConstant._BACKGROUND_FOR_DATETIME_MAP, backgroundForDateTimeMap);
        caldroidData.put(CaldroidCustomConstant._TEXT_COLOR_FOR_DATETIME_MAP, textColorForDateTimeMap);
        caldroidData.put(CaldroidCustomConstant._TEXT_DOT_FOR_DATETIME_MAP, textDotForDateTimeMap);
        caldroidData.put(CaldroidCustomConstant.THEME_RESOURCE, themeResource);
        return caldroidData;
    }

    public void expandCollapseWeekView(float paramFloat) {

    }

    public void setViewModeChangeListener(OnViewModeChangeListener paramOnViewModeChangeListener) {
        onViewModeChnangeListener = paramOnViewModeChangeListener;
    }

    public void setFragment(TimeTableFragment fragment) {
        this.fragment = fragment;
    }


    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    public AdapterView.OnItemClickListener getMonthDateItemClickListener() {
        if (dateMonthItemClickListener == null) {
            dateMonthItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    DateTime dateTime = dateInMonthsList.get(position);
                    LogHelper.d(tag, "position: " + position + " Row: " + (int) (position / 7 + 1));
                    CaldroidGridAdapter curadaptor = monthDatePagerAdapters.get(monthPageListener.getCurrent(monthPageListener.getCurrentPage()));
                    LogHelper.d(tag, "curadaptor set adapterDateTime: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                    curadaptor.setAdapterDateTime(dateTime);
                    setMonthSelectedDateInfo(position, (int) (position / 7 + 1));
                    if (caldroidListener != null) {
//                        if (!enableClickOnDisabledDates) {
//                            if ((minDateTime != null && dateTime
//                                    .lt(minDateTime))
//                                    || (maxDateTime != null && dateTime
//                                    .gt(maxDateTime))
//                                    || (disableDates != null && disableDates
//                                    .indexOf(dateTime) != -1)) {
//                                return;
//                            }
//                        }
                        caldroidListener.onSelectMonthDate(dateTime, view, false);

                        LogHelper.i(tag, "selected dateTime : " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                        refreshWeekViewToSelectedDateTime(dateTime);
                    }
                }
            };
        }

        return dateMonthItemClickListener;
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     * 周视图下没有不可点击的位置，如果一个周内有两个月的日期，点击，需要切换月视图
     *
     * @return
     */
    public AdapterView.OnItemClickListener getWeekDateItemClickListener() {
        if (dateWeekItemClickListener == null) {
            dateWeekItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    DateTime dateTime = dateInWeekList.get(position);
                    LogHelper.d(tag, "position: " + position + " Row: " + (int) (position / 7 + 1) +
                            "selected dateTime : " + dateTime.toString(CaldroidCustomConstant.simpleFormator));

                    if (caldroidListener != null) {

                        caldroidListener.onSelectWeekDate(dateTime, view, false);

                        refreshMonthViewToSelectedDateTime(dateTime);
                    }
                }
            };
        }
        return dateWeekItemClickListener;
    }

    /**
     * 记录选择中的日期的位置信息，视图切换中使用
     *
     * @param position
     * @param row
     */
    private void setMonthSelectedDateInfo(int position, int row) {
        LogHelper.i(tag, "setMonthSelectedDateInfo: Height: " + getHeight() + " CalendarHeight: " + monthCalendar.getHeight() + " RowHeight: "
                + monthCalendar.getRowHeight() + " size: " + monthCalendar.getDates().size());
//        rowHeight = monthCalendar.getRowHeight();

    }

    /**
     * 发布当前viewMode已经改变，需要多方配合
     *
     * @param week
     */
    private void onViewModeChangedEvent(ViewMode week) {
        LogHelper.i(tag, "onViewModeChangeEvent: " + week);
        eventBus.post(new ViewModeChangedEvent(week));
    }

    public void setSelectedDate(Date selectedDate) {
        if (selectedDate == null) {
            return;
        }
        DateTime dateTime = CalendarHelper.convertDateToDateTime(selectedDate);
        LogHelper.d(tag, "setSelectedDate" + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        selectedDates.add(dateTime);
    }

    /**
     * 主界面 主动改变当前神图状态
     *
     * @param viewMode
     */
    public void convertWeek_Month(ViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == ViewMode.MONTH) {
            LogHelper.d(tag, "animate  montCalendar.getY: " + monthCalendar.getY() + " getLayoutHeight: " + getLayoutParams().height);
            LogHelper.d(tag, "animate  montCalendar.getY: " + monthCalendar.getY() + " getLayoutHeight: " + getLayoutParams().height);
            monthCalendar.setVisibility(View.VISIBLE);
            LogHelper.d(tag, "animate  montCalendar.getY: " + monthCalendar.getY() + " getLayoutHeight: " + getLayoutParams().height);
            LogHelper.d(tag, "animate animateToMonthView: weekCalendar.getHeight: " + weekCalendar.getHeight() + " monthCalendar.getY: "
                    + monthCalendar.getY() + " monthCalendar.getHeight: " + monthCalendar.getHeight());
//            animateToMonthView(weekCalendar.getHeight() == 0 ? monthCalendar.getRowHeight() : weekCalendar.getHeight(),
//                    monthCalendar.getHeight() == 0 ? weekCalendar.getHeight() * 6 : monthCalendar.getHeight());
            weekToMonthAnimation();
        } else {
//            animateToWeekView();
            monthToWeekAnimation();
        }
    }

    /**
     * 通过外部 的按钮进行周月切换
     * 周 ---> 月
     */
    private void weekToMonthAnimation() {
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        //   首先判断是否为空，检测有效性   当前layout height 不能为-2  monthCalenda.getHeight getRowheight 不能为0
        final float[] arrayOfFloat = new float[2];
        final int rowHeight;
        if (getLayoutParams().height > 0) {
            arrayOfFloat[0] = getLayoutParams().height;
        } else {
            arrayOfFloat[0] = weekCalendar.getHeight();
        }

        if (monthCalendar.getRowHeight() > 0 && monthCalendar.getHeight() > monthCalendar.getRowHeight()) {
            arrayOfFloat[1] = monthCalendar.getHeight();
            rowHeight = monthCalendar.getRowHeight();
        } else {
            if (weekCalendar.getRowHeight() > 0 && weekCalendar.getHeight() > 0) {
                arrayOfFloat[1] = weekCalendar.getRowHeight() * 6;
                rowHeight = weekCalendar.getRowHeight();
            } else {
                arrayOfFloat[1] = weekCalendar.getHeight() * 6;
                rowHeight = weekCalendar.getHeight();
            }
        }
        getCurScrollHeightScrollInfo();
        LogHelper.d(tag, "animate weekToMonthAnimation " + arrayOfFloat[0] + " --> " + arrayOfFloat[1]
                + " minScrollY " + minScrollY + "  curScrollHeight " + curScrollHeight + " rowHeight: " + rowHeight
                + " monthCalendar.getY(): " + monthCalendar.getY());
        localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
        localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                //首先去除y的位移  ------>更改为y与下方的做位移同时移动
                float dy = (getLayoutParams().height - arrayOfFloat[0]) / (arrayOfFloat[1] - arrayOfFloat[0]) * minScrollY;
                monthCalendar.setY(-dy + minScrollY);
//                if(getLayoutParams().height -arrayOfFloat[0]<= -minScrollY){
//                    monthCalendar.setY(getLayoutParams().height - arrayOfFloat[0] +minScrollY);
//                }else{
//                    monthCalendar.setY(0);
//                }
//                if (getLayoutParams().height >=( arrayOfFloat[1] + minScrollY )) {
//                    int y = (int) (getLayoutParams().height - ( arrayOfFloat[1] ));
//                    monthCalendar.setY(y-minScrollY);
//                }
                LogHelper.d(tag, "getLayoutParams().height : " + getLayoutParams().height + " monthCalendar.getY(): " + monthCalendar.getY());
                setLayoutParams(getLayoutParams());
            }
        });
//        localValueAnimator.setDuration(2500);
        localValueAnimator.setInterpolator(new LinearInterpolator());
        localValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewMode = ViewMode.MONTH;
                onViewModeChangedEvent(ViewMode.MONTH);
            }
        });
        monthCalendar.setVisibility(View.VISIBLE);
        weekCalendar.setVisibility(View.GONE);
        localValueAnimator.start();
    }

    /**
     * 通过外部 的按钮进行周月切换
     * 月 ---> 周
     */
    private void monthToWeekAnimation() {
        if (localValueAnimator != null) {
            if (localValueAnimator.isRunning()) {
                localValueAnimator.removeAllListeners();
                localValueAnimator.end();
            }
            localValueAnimator = null;
        }
        //   首先判断是否为空，检测有效性   当前layout height 不能为-2  monthCalenda.getHeight getRowheight 不能为0
        final float[] arrayOfFloat = new float[2];
        final int rowHeight;
        if (monthCalendar.getRowHeight() > 0 && monthCalendar.getHeight() > monthCalendar.getRowHeight()) {
            arrayOfFloat[0] = monthCalendar.getHeight();
            arrayOfFloat[1] = monthCalendar.getRowHeight();
            rowHeight = monthCalendar.getRowHeight();
        } else {
            if (weekCalendar.getRowHeight() > 0 && weekCalendar.getHeight() > 0) {
                arrayOfFloat[0] = weekCalendar.getRowHeight() * 6;
                arrayOfFloat[1] = weekCalendar.getRowHeight();
                rowHeight = weekCalendar.getRowHeight();
            } else {
                arrayOfFloat[0] = weekCalendar.getHeight() * 6;
                arrayOfFloat[1] = weekCalendar.getHeight();
                rowHeight = weekCalendar.getHeight();
            }
        }
        LogHelper.d(tag, "animate monthToWeekAnimation " + arrayOfFloat[0] + "  " + arrayOfFloat[1]
                + " minScrollY " + minScrollY + "  curScrollHeight " + curScrollHeight + " rowHeight: " + rowHeight);
        localValueAnimator = ValueAnimator.ofFloat(arrayOfFloat);
        localValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                float dy = (getLayoutParams().height - arrayOfFloat[0]) / (arrayOfFloat[1] - arrayOfFloat[0]) * minScrollY;
                monthCalendar.setY(dy);
                //首先去除y的位移    ------>更改为y与下方的做位移同时移动
//                if(arrayOfFloat[0]- getLayoutParams().height <= -minScrollY){
//                    monthCalendar.setY(getLayoutParams().height - arrayOfFloat[0]);
//                }else{
//                    monthCalendar.setY(minScrollY);
//                }
//                if (getLayoutParams().height < ( -minScrollY + rowHeight)) {
//                    int dy = (int) (getLayoutParams().height + minScrollY  - rowHeight);
//                    monthCalendar.setY( -dy);
//                }
                LogHelper.d(tag, "etLayoutParams().height : " + getLayoutParams().height + " monthCalendar.getY(): " + monthCalendar.getY());
                setLayoutParams(getLayoutParams());
            }
        });
//        localValueAnimator.setDuration(2500);
        localValueAnimator.setInterpolator(new LinearInterpolator());
        localValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewMode = ViewMode.WEEK;
                onViewModeChangedEvent(ViewMode.WEEK);
                weekCalendar.setVisibility(View.VISIBLE);
                monthCalendar.setVisibility(View.GONE);
            }
        });
        localValueAnimator.start();
    }

    /**
     * 主界面可以选择周视图的指定日期，用来改变当前的周视图，同时改变月视图
     *
     * @param dateTime
     */
    public void convert_to_Week(DateTime dateTime) {
        clearSelectedDates();
        dateTime = dateTime.withTime(0, 0, 0, 0);
        setSelectedDate(CalendarHelper.convertDateTimeToDate(dateTime));
        setCurSelectedDateTime(dateTime);

        weekPageListener.changedToCur(dateTime);
        monthPageListener.changedSelectDateToCur(dateTime);
        coursePageChangedListener.setCurDateTime(dateTime);
    }

    /**
     * 当前选定时间
     *
     * @param curSelectedDateTime
     */
    public void setCurSelectedDateTime(DateTime curSelectedDateTime) {
        this.curSelectedDateTime = curSelectedDateTime;
    }

    public DateTime getCurSelectedDateTime() {
        return curSelectedDateTime;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public ArrayList<DateTime> getDateInMonthsList() {
        return dateInMonthsList;
    }

    public void setTextDotForDateTimeMap(Map<DateTime, Integer> textDotForDateTimeMap) {
        this.textDotForDateTimeMap = textDotForDateTimeMap;
    }

    public void setDateInMonthsList(ArrayList<DateTime> dateInMonthsList) {
        this.dateInMonthsList = dateInMonthsList;
    }

    public ArrayList<DateTime> getDateInWeekList() {
        return dateInWeekList;
    }

    public void setDateInWeekList(ArrayList<DateTime> dateInWeekList) {
        this.dateInWeekList = dateInWeekList;
    }

    /**
     * Down事件后获取相应数据信息
     */
    public void getCurScrollHeightScrollInfo() {
        //计算得到当前位置
//        CaldroidGridAdapter curadaptor = monthDatePagerAdapters.get(monthPageListener.getCurrent(monthPageListener.getCurrentPage()));
//        ArrayList<DateTime> datelist = curadaptor.getDatetimeList();
//        DateTime cur = curadaptor.getDateTime();
        LogHelper.d(tag, "getCurScrollHeightScrollInfo: " + curSelectedDateTime.toString(CaldroidCustomConstant.simpleFormator));
        curSelectedDateTime = curSelectedDateTime.withTime(0, 0, 0, 0);
        int position = 0;
        for (int i = 0; i < dateInMonthsList.size(); i++) {
            DateTime dt = dateInMonthsList.get(i);
//            LogHelper.d(tag, "dt: " + dt.toString(CaldroidCustomConstant.simpleFormator)
//                    + curSelectedDateTime.toString(CaldroidCustomConstant.simpleFormator));
            if (dt.equals(curSelectedDateTime)) {
                position = i + 1;
                break;
            }
        }
        final int row = (int) Math.ceil(position % 7 == 0 ? position / 7 : (Math.floor(position / 7) + 1));
        int totalrow = 6;//datelist.size()/7;
        curScrollY = 0;
        int rowHeight = monthCalendar.getRowHeight() == 0 ? weekCalendar.getRowHeight() : monthCalendar.getRowHeight();
        if (rowHeight == 0) {
//            View firstChild = View.inflate(getContext(),R.layout.normal_date_cell,null);
//            int width = getMeasuredWidth();
//            // Use the previously measured width but simplify the calculations
//            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
//                    MeasureSpec.EXACTLY);
//            firstChild.measure(widthMeasureSpec, MeasureSpec
//                    .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//
//            rowHeight = firstChild.getMeasuredHeight();
//            rowHeight = DisplayUtils.dip2px(getContext(),39);
        }
        curScrollHeight = rowHeight * (totalrow - row);
        minScrollY = -rowHeight * (row - 1);
//        isInitedminScrollY  = true;
        LogHelper.i(tag, "animate : row: " + row + " totalrow: " + totalrow + " position: " + position + " datelist.size: " + dateInMonthsList.size());
        LogHelper.i(tag, "animate : curScrollY: " + curScrollY + " curScrollHeight: " + curScrollHeight + " minScrollY: " + minScrollY);

    }

    /**
     * 翻动课表ViewPager 课程列表改变 对应的动作
     *
     * @param dateTime
     */
    public void setSelectedDateTimeByCoursePagers(DateTime dateTime) {
//        monthPageListener.changedSelectDateToCur(dateTime);
//        weekPageListener.changedToCur(dateTime);
        if (viewMode == ViewMode.WEEK) {
            CaldroidGridAdapter curadaptor = weekDatePagerAdapters.get(weekPageListener.getCurrent(weekPageListener.getCurrentPage()));
            curadaptor.setAdapterDateTime(dateTime);
            if (caldroidListener != null) {
                caldroidListener.onSelectWeekDate(dateTime, null, true);
                refreshMonthViewToSelectedDateTime(dateTime);
                refreshWeekViewToSelectedDateTime(dateTime);
                if (pageChangeListener != null) {
                    pageChangeListener.onPageChanged(dateTime);
                }
            }
//            refreshWeeksView();
        } else {
            CaldroidGridAdapter curadaptor = monthDatePagerAdapters.get(monthPageListener.getCurrent(monthPageListener.getCurrentPage()));
            curadaptor.setAdapterDateTime(dateTime);
            if (caldroidListener != null) {
                caldroidListener.onSelectMonthDate(dateTime, null, true);
                LogHelper.i(tag, "selected dateTime : " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                refreshWeekViewToSelectedDateTime(dateTime);
                refreshMonthViewToSelectedDateTime(dateTime);
                if (pageChangeListener != null) {
                    pageChangeListener.onPageChanged(dateTime);
                }
            }
//            refreshMonthView();
        }
    }

    public void setCoursePageChangedListener(CoursesPageChangedLister coursePageChangedListener) {
        this.coursePageChangedListener = coursePageChangedListener;
    }

    /**
     * 初使加载，或者，直接转换视图
     *
     * @param viewMode
     */
    public void setViewMode(ViewMode viewMode) {
        LogHelper.d(tag, "getLayoutParams().height: " + getLayoutParams().height + "  " + getMeasuredHeight() + " rowHeight: "
                + monthCalendar.getRowHeight() + " monthCalendar.height: " + monthCalendar.getHeight());
        this.viewMode = viewMode;
        onViewModeChangedEvent(viewMode);
        if (viewMode == ViewMode.MONTH) {
            getLayoutParams().height = monthCalendar.getHeight();
            monthCalendar.setVisibility(View.VISIBLE);
            weekCalendar.setVisibility(View.GONE);
            setLayoutParams(getLayoutParams());
        } else {
            getCurScrollHeightScrollInfo();
            monthCalendar.setVisibility(View.GONE);
            weekCalendar.setVisibility(View.VISIBLE);
            invalidate();
            if (monthCalendar.getRowHeight() != 0 || weekCalendar.getRowHeight() != 0) {
                getLayoutParams().height = weekCalendar.getRowHeight() == 0 ? monthCalendar.getRowHeight() : weekCalendar.getRowHeight();
            } else {  //有时会出现当前高度为0的bug,在些强制设置高度
                getLayoutParams().height = DisplayUtils.dip2px(context, 39);
            }
            monthCalendar.setY(minScrollY);
            getCurScrollHeightScrollInfo();
            setLayoutParams(getLayoutParams());
        }
    }

    /**
     * 两部分动画，其中一部分，检查
     */
    private void checkScrollEndStatus() {

    }

    /**
     * 动画结束，检查当前的view显示状态是否正确
     * 若不正确，调整
     */
    public void checkEndStatus() {
        LogHelper.d(tag, "checkEndStatus");
        if (monthCalendar != null && weekCalendar != null) {
            if (viewMode == ViewMode.MONTH) {
                if (weekCalendar.getVisibility() != View.GONE) {
                    weekCalendar.setVisibility(View.GONE);
                }
                if (monthCalendar.getVisibility() != View.VISIBLE) {
                    monthCalendar.setVisibility(VISIBLE);
                }
                if (monthCalendar.getY() != 0) {
                    monthCalendar.setY(0);
                }
                if (getLayoutParams().height != monthCalendar.getHeight()) {
                    getLayoutParams().height = monthCalendar.getHeight();
                    setLayoutParams(getLayoutParams());
                }
            } else {
                if (weekCalendar.getVisibility() != View.VISIBLE) {
                    weekCalendar.setVisibility(View.VISIBLE);
                }
                if (curScrollHeight != 0 && minScrollY != 0) {
                    monthCalendar.setY(minScrollY);
                }
                if (monthCalendar.getVisibility() != View.GONE) {
                    monthCalendar.setVisibility(GONE);
                }

            }
        }
    }

    public static class PullCalEvent {
        public float   distance          = 0F;
        public boolean isEnded           = false;
        public boolean threasholdReached = false;

        public PullCalEvent(float paramFloat) {
            this.distance = paramFloat;
        }

        public PullCalEvent(boolean paramBoolean) {
            this.isEnded = true;
            this.threasholdReached = paramBoolean;
        }
    }

    public static class ViewModeChangedEvent {
        public ViewMode viewMode;

        public ViewModeChangedEvent(ViewMode viewMode) {
            this.viewMode = viewMode;
        }
    }

    public PageChangeListener getPageChangeListener() {
        return pageChangeListener;
    }

    public void setPageChangeListener(PageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }


    public interface PageChangeListener {
        void onPageChanged(DateTime dateTime);
    }

    public static abstract interface OnViewModeChangeListener {
        public abstract void onViewModeChanged(ViewMode viewMode);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (eventBus != null) {
            this.eventBus.register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogHelper.d(tag, "onDetachedFromWindow");
        if (eventBus != null) {
            eventBus.unregister(this);
        }
    }

    @Subscribe
    public void onEvent(TeacherCourseManager.TeacherCourseChanged teacherCourseChanged) {
        LogHelper.d(tag, "onEvnet teacherCourseChanged: ");
        TeacherCourseManager teacherCourseManager = TeacherCourseManager.getInstance(getContext().getApplicationContext());
        setTextDotForDateTimeMap(teacherCourseManager.getHaveCourseMap());
        refreshWeeksView();
        refreshMonthView();
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public InfiniteCoursePageAdapter getInfiniteCoursePageAdapter() {
        return infiniteCoursePageAdapter;
    }

    public void setInfiniteCoursePageAdapter(InfiniteCoursePageAdapter infiniteCoursePageAdapter) {
        this.infiniteCoursePageAdapter = infiniteCoursePageAdapter;
    }
}
