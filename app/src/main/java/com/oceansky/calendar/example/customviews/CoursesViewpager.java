package com.oceansky.calendar.example.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ListView;

import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.customviews.DateCalendar.PullCalEvent;
import com.oceansky.calendar.example.customviews.adapter.InfiniteCoursePageAdapter;
import com.oceansky.calendar.example.fragments.CoursesFragment;
import com.oceansky.calendar.example.utils.LogHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by 王旭国 on 16/6/14 10:39
 */
public class CoursesViewpager extends ViewPager {
    private EventBus eventBus;
    private static final String TAG = CoursesViewpager.class.getSimpleName();
    private ViewMode          viewMode;
    private CalendarViewPager monthCalendar;
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
    ListView listView;
    /**
     * 当前是否可以对手势处理到日历的变化 否则 当前是否在移动ListView
     */
    boolean isCalendar = true;

    //当前选中的日期可以划动的设置高度的距离
//    float curScrollHeight;
    //sety 最小值
//    float minScrollY = 0;
    public CoursesViewpager(Context paramContext) {
        super(paramContext);
        initComponent(paramContext);
    }

    public CoursesViewpager(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initComponent(paramContext);
    }

    protected void initComponent(Context paramContext) {
        eventBus = EventBus.getDefault();
    }

    private int[] getLocation() {//????
        int[] arrayOfInt = new int[2];
        getLocationInWindow(arrayOfInt);
//        getLocationOnScreen(arrayOfInt);
        return arrayOfInt;
    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        LogHelper.d(TAG, "onInterceptTouchEvent " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
//        return super.onInterceptTouchEvent(event);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        LogHelper.d(TAG, "onTouchEvent        " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
//        return super.onTouchEvent(event);
//    }

    /**
     * 移动策略  优化移动listview再对Calendar进行变化
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        locationInWindow = getLocation();
        int deltaX = (int) event.getRawX() - locationInWindow[0];//相对于控件内部 的顶间距
        int deltaY = (int) event.getRawY() - locationInWindow[1];
//        LogHelper.d(TAG,"locationInWindow[0]: "+locationInWindow[0]+" locationInWindow[1]: "+locationInWindow[1]+
//                "event.getRawX: "+event.getRawX()+" event.getRawY: "+event.getRawY()+"dX: "+deltaX+" dY: "+deltaY);
        switch (event.getAction()) {
            //  滑动事件冲突处理    优化滑动到周视图去处理，在Down事件中判断什么操作，是否可以进行日历切换
            case MotionEvent.ACTION_DOWN:
                LogHelper.d(TAG, "ACTION_DOWN: " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                fastFlingCount = 0;
                initParameters();
                velocityTracker.clear();
                downY = startScrolY = event.getRawY();
//                downY = startScrolY = deltaY;
                curScrollY = 0;
                // 根据当前视图状态，计算  判断对当前的MOVE事件是否进行处理
                if (viewMode == ViewMode.MONTH) {
                    isCalendar = true;
                } else {
                    if(isListViewReachTopEdge(listView)){
                        LogHelper.d(TAG,"listview reachedTopEdge");
                        isCalendar = true;
                    }else{
                        isCalendar = false;
                        LogHelper.d(TAG,"listview null or not reacheBottomEdge");
                    }
                }
//                if(velocityTracker.getYVelocity()>0){
//                    if(isListViewReachTopEdge(listView)){
//                        LogHelper.d(TAG,"listview reachedBottomEdge");
//                        isCalendar = true;
//                    }else{
//                        isCalendar = false;
//                        LogHelper.d(TAG,"listview null or not reacheBottomEdge");
//                    }
//                }else{
//                    if(isListViewReachBottomEdge(listView)){
//                        LogHelper.d(TAG,"listview reachBottomEdge");
//                        isCalendar = true;
//                    }else{
//                        isCalendar = false;
//                        LogHelper.d(TAG,"listview null or not reacheBottomEdge");
//                    }
//                }
                return super.dispatchTouchEvent(event);
//                break;
            case MotionEvent.ACTION_UP:
                LogHelper.d(TAG, "ACTION_UP: " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                listView = null;
                if (!isCalendar) {
                    return super.dispatchTouchEvent(event);
                }
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                LogHelper.d(TAG, "velocityTracker  XvelocityTracker : " + velocityTracker.getXVelocity() + " YVelocityTracker: " + velocityTracker.getYVelocity());
                LogHelper.d(TAG, "ACTION_UP curScrollY: " + curScrollY + "monthCalendar.Height" + monthCalendar.getHeight());
                if (monthCalendar.getHeight() > 0 && Math.abs(curScrollY) >= monthCalendar.getHeight() - monthCalendar.getRowHeight()) {
                    onPullCalEvent(new PullCalEvent(true));
                } else {
                    onPullCalEvent(new PullCalEvent(false));
                }
                return super.dispatchTouchEvent(event);
//                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                LogHelper.d(TAG, "ACTION_MOVE " + " X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY()
                        + "   XVelocity:" + velocityTracker.getXVelocity() + "  YVelocity: " + velocityTracker.getYVelocity());
//                    float dy = deltaY - startScrolY;
//                    startScrolY = deltaY;
                float dy = event.getRawY() - startScrolY;
                startScrolY = event.getRawY();

                //事件处理流程： 首先判断是否Y轴方向在处理事件，
                // 优先考虑List中的数据刷新移动问题
                if ((Math.abs(velocityTracker.getYVelocity()) < 50) || Math.abs(velocityTracker.getXVelocity()) >= Math.abs(velocityTracker.getYVelocity())) {
                    return super.dispatchTouchEvent(event);
                }
                if(viewMode == ViewMode.WEEK&&listView!=null&&!isListViewReachBottomEdge(listView)&&velocityTracker.getYVelocity() < 0){
                    return super.dispatchTouchEvent(event);//if(viewMode == ViewMode.WEEK&&dy>0){
                }
                if (!isCalendar) {
                    return super.dispatchTouchEvent(event);
                }
                if (velocityTracker.getYVelocity() > 0 && curScrollY >= 0 && viewMode == ViewMode.MONTH
                        || velocityTracker.getYVelocity() < 0 && curScrollY <= 0 && viewMode == ViewMode.WEEK) {
                    return false;
                } else {
                    //有效的滑动
                    LogHelper.i(TAG, "有效的滑动");
                }
                if (velocityTracker.getYVelocity() > -1000f && velocityTracker.getYVelocity() < 1000) {
                    LogHelper.d(TAG, "dy: " + dy);
                    curScrollY += dy;
                    onPullCalEvent(new DateCalendar.PullCalEvent(dy));
                    return true;
                } else {
////                    float dy = deltaY - startScrolY;
////                    startScrolY = deltaY;
//                    float dy = event.getRawY() - startScrolY;
//                    startScrolY = event.getRawY();
                    fastFlingCount++;
                    curScrollY += dy;
                    LogHelper.d(TAG, "YVelocity > 1000 dy: " + dy);
                    onPullCalEvent(new DateCalendar.PullCalEvent(dy));////???????如何处理快速滑动的手势事件？？？？？？
                    return true;
                }

            case MotionEvent.ACTION_CANCEL:
                LogHelper.d(TAG, "ACTION_CANCEL " + " X:  " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                return false;
//                break;
            default:
                LogHelper.d(TAG, "default " + "  X: " + event.getX() + " Y: " + event.getY() + " RaWX: " + event.getRawX() + " RawY: " + event.getRawY());
                return false;
//                break;
        }
    }

    private void onPullCalEvent(DateCalendar.PullCalEvent pullCalEvent) {
        eventBus.post(pullCalEvent);
    }

    @Subscribe
    public void onViewModeChnagedEvent(DateCalendar.ViewModeChangedEvent viewModeChnagedEvent) {
        if (viewMode == viewModeChnagedEvent.viewMode) {
            return;
        }
        this.viewMode = viewModeChnagedEvent.viewMode;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.eventBus.register(this);
    }


    protected void onDetachedFromWindow() {
        this.eventBus.unregister(this);
        super.onDetachedFromWindow();
    }

    /**
     * 当前参数获取
     */
    public void initParameters() {
        curScrollY = 0;
        if (getAdapter() instanceof InfiniteCoursePageAdapter) {
            LogHelper.d(TAG, "initParameters  getAdapter ");
            InfiniteCoursePageAdapter curserAdapter = (InfiniteCoursePageAdapter) getAdapter();
            if (curserAdapter != null) {
                CoursesFragment coursesFragment = (CoursesFragment) curserAdapter.getCurrentFragment();
//                CoursesFragment coursesFragment = (CoursesFragment) curserAdapter.getItem(getCurrentItem());
                listView = coursesFragment.getListView();
                if (listView == null && coursesFragment != null && coursesFragment.getView() != null) {
                    listView = (ListView) coursesFragment.getView().findViewById(R.id.listview_course);
                }
                if (listView != null) {
                    LogHelper.d(TAG, "listview: height: " + listView.getHeight() + " listview: scrollY; " + listView.getScrollY()
                            + "list.childCount: " + listView.getChildCount() + " getScrollY: " + getScrollY(listView) + "");
                }
            }
        } else {
            LogHelper.d(TAG, "initParameters  CourseNotLogHelperinViewPager");
        }

    }

    public int getScrollY(ListView mListView) {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int lastVisiblePosition = mListView.getLastVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    public boolean isListViewReachBottomEdge(final ListView listView) {
        boolean result = false;
        if (listView == null) {
            LogHelper.e(TAG,"isListViewReachBottomEdge listview null");
            return true;
        }
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            if (bottomChildView == null) {
                LogHelper.e(TAG,"isListViewReachTopEdge bottomChildView null");
                return true;
            }
            result = (listView.getHeight() >= bottomChildView.getBottom());
        }
        ;
        return result;
    }

    public boolean isListViewReachTopEdge(final ListView listView) {
        boolean result = false;
        if (listView == null) {
            LogHelper.e(TAG,"isListViewReachTopEdge listview null");
            return true;
        }
        if (listView.getFirstVisiblePosition() == 0) {
            final View topChildView = listView.getChildAt(0);
            if (topChildView == null) {
                LogHelper.e(TAG,"isListViewReachTopEdge topChildView null");
                return true;
            }
            result = topChildView.getTop() == 0;
        }
        return result;
    }

    public CalendarViewPager getMonthCalendar() {
        return monthCalendar;
    }

    public void setMonthCalendar(CalendarViewPager monthCalendar) {
        this.monthCalendar = monthCalendar;
    }
}
