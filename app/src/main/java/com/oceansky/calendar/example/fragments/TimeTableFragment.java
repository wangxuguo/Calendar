package com.oceansky.calendar.example.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.oceansky.calendar.example.constant.Constants;
import com.oceansky.calendar.example.customviews.CoursesViewpager;
import com.oceansky.calendar.example.utils.ToastUtil;
import com.oceansky.calendar.example.adapter.PickWeekAdapter;
import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.customviews.DateCalendar;
import com.oceansky.calendar.example.customviews.ViewMode;
import com.oceansky.calendar.example.customviews.adapter.CoursesPageChangedLister;
import com.oceansky.calendar.example.customviews.adapter.CoursesViewPagerAdapter;
import com.oceansky.calendar.example.customviews.adapter.InfiniteCoursePageAdapter;
import com.oceansky.calendar.example.customviews.adapter.WeekdayArrayAdapter;
import com.oceansky.calendar.example.manager.TeacherCourseManager;
import com.oceansky.calendar.example.network.http.HttpManager;
import com.oceansky.calendar.example.network.response.RedPointEntity;
import com.oceansky.calendar.example.network.subscribers.BaseSubscriber;
import com.oceansky.calendar.example.utils.DisplayUtils;
import com.oceansky.calendar.example.utils.LogHelper;
import com.oceansky.calendar.example.utils.NetworkUtils;
import com.oceansky.calendar.example.utils.SecurePreferences;
import com.oceansky.calendar.example.utils.SharePreferenceUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Subscription;

/**
 * 课表  包含下拉刷新，日历（周，月模式切换)
 * Created by 王旭国 on 16/6/2.
 */
public class TimeTableFragment extends BaseLazyFragment implements View.OnClickListener {
    private static final String MONTH_DATETIME_FOMATOR = "M月-yyyy年";
    private static final String WEEK_DATETIME_FOMATOR  = "MM.dd";
    private static final int    LOGINAGAIN             = 11;
    private              String tag                    = getClass().getSimpleName();
    private RelativeLayout   title_bar;
    private ImageButton      iv_msgcenter;
    private View             have_msg_unread;
    //    private LinearLayout title_panel;
    private LinearLayout     li_title_week;
    private TextView         tv_title_month;
    private TextView         tv_title_week;
    private ImageView        iv_title_arrow;
    //  private TextView tv_loginorregister;
    private ImageButton      btn_month_week;
    private GridView         weekday_gridview;
    private DateCalendar     pagers_container;
    //    private View layout_nologin_page;
    private CoursesViewpager vp_class;
    private View             loading_layout;
    private PopupWindow      popPickWeekView;
    private ViewMode viewMode = ViewMode.MONTH;
    private EventBus eventBus;
    TeacherCourseManager teacherCourseManager;
    public DateTime curSelectedDateTime;
    private DateCalendar.PageChangeListener pageChangeListener = new DateCalendar.PageChangeListener() {
        @Override
        public void onPageChanged(DateTime dateTime) {
            curSelectedDateTime = dateTime;
            DateTime now = DateTime.now();
            String str;
            if (dateTime.getMillis() < now.dayOfWeek().withMaximumValue().getMillis()
                    && dateTime.getMillis() > now.dayOfWeek().withMinimumValue().getMillis()) {
                str = "本周";
            } else {
                str = dateTime.dayOfWeek().withMinimumValue().toString(WEEK_DATETIME_FOMATOR) + "-"
                        + dateTime.dayOfWeek().withMaximumValue().toString(WEEK_DATETIME_FOMATOR);
            }
            if (viewMode == ViewMode.WEEK) {
                changePopWinListData();
            }
            tv_title_month.setText(dateTime.toString(MONTH_DATETIME_FOMATOR));
            tv_title_week.setText(str);
            checkIfLoadPreOrNextYear(dateTime);
        }
    };
    private InfiniteCoursePageAdapter infiniteCoursePageAdapter;
    private boolean isFirstLoaded = true;
    private int                mPri;
    private int                mPub;
    private RedPointSubscriber mRedPointSubscriber;
    private Subscription       mRedPointSubscription;

    /**
     * 根据当前时间 的移动判断是否需要预先加载课表数据
     *
     * @param dateTime
     */
    private void checkIfLoadPreOrNextYear(DateTime dateTime) {
        LogHelper.d(tag, "checkIfLoadPreOrNextYear " + dateTime.getYear() + " / " + dateTime.getMonthOfYear());
        if (dateTime.getMonthOfYear() <= 2) {
            LogHelper.d(tag, "checkIfLoadPreOrNextYear  load minus " + dateTime.minusYears(1).getYear());
            teacherCourseManager.load(dateTime.minusYears(1).getYear());
        } else if (dateTime.getMonthOfYear() >= 11) {
            LogHelper.d(tag, "checkIfLoadPreOrNextYear  load plus " + dateTime.plusYears(1).getYear());
            teacherCourseManager.load(dateTime.plusYears(1).getYear());
        }
    }

    private AdapterView.OnItemClickListener weekItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogHelper.d(tag, "onItemClick position: " + position + " DateTime : " + getWeekStr(listdateTimes.get(position)));

            pagers_container.convert_to_Week(listdateTimes.get(position));
            tv_title_week.setText(getWeekStr(listdateTimes.get(position)));
            popPickWeekView.dismiss();
        }
    };
    //    private ArrayList<String> listStrWeeks;
    private ArrayList<DateTime>      listdateTimes;
    private CoursesViewPagerAdapter  coursesViewPagerAdapter;
    //    private CoursesNotLoginAdapter   coursesNotLoginAdapter;
    private CoursesPageChangedLister coursesPageChangedLister;


    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_timetable);
        super.onCreateViewLazy(savedInstanceState);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        registerReceiver();
        teacherCourseManager = TeacherCourseManager.getInstance(getActivity());
        teacherCourseManager.init(curSelectedDateTime == null ? DateTime.now() : curSelectedDateTime);

        initView();
        //        initLeftImage();
        //        listStrWeeks = getPickWeeksList(DateTime.now().minusWeeks(1));
        teacherCourseManager.initToday_before_after_6_month(DateTime.now());
    }

    @Override
    void onErrorLayoutClick() {

    }

    private void initView() {
        title_bar = (RelativeLayout) findViewById(R.id.title_bar);
        weekday_gridview = (GridView) findViewById(R.id.weekday_gridview);
        pagers_container = (DateCalendar) findViewById(R.id.pagers_container);
        iv_msgcenter = (ImageButton) findViewById(R.id.iv_msgcenter);
        have_msg_unread = findViewById(R.id.have_msg_unread);
        iv_msgcenter.setOnClickListener(this);
        tv_title_month = (TextView) findViewById(R.id.tv_title_month);
        li_title_week = (LinearLayout) findViewById(R.id.li_title_week);
        tv_title_week = (TextView) findViewById(R.id.tv_title_week);
        li_title_week.setOnClickListener(this);
        btn_month_week = (ImageButton) findViewById(R.id.btn_month_week);
        btn_month_week.setOnClickListener(this);
        pagers_container.setFragment(this);
        vp_class = (CoursesViewpager) findViewById(R.id.vp_class);

        final String token = SecurePreferences.getInstance(getActivity(), false).getString(Constants.KEY_ACCESS_TOKEN);
        loading_layout = findViewById(R.id.loading_layout);
        if (!TextUtils.isEmpty(token)) {
            pagers_container.setTextDotForDateTimeMap(teacherCourseManager.getHaveCourseMap());
            teacherCourseManager.setLoginState(true);
        } else {
            teacherCourseManager.setLoginState(false);
        }
        pagers_container.init(ViewMode.MONTH);//默认周视图
        //        viewMode = ViewMode.WEEK;
        //        btn_month_week.setImageResource(R.mipmap.nav_icon_month_default);
        //        tv_title_month.setVisibility(View.INVISIBLE);
        //        li_title_week.setVisibility(View.VISIBLE);

        pagers_container.setPageChangeListener(pageChangeListener);
        GridView weekday_gridview = (GridView) findViewById(R.id.weekday_gridview);
        WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter(R.style.CaldroidDefault);
        weekday_gridview.setAdapter(weekdaysAdapter);
        coursesViewPagerAdapter = new CoursesViewPagerAdapter(getChildFragmentManager(), DateTime.now());
        //        CoursesNotLoginAdapter coursesNotLoginAdapter = new CoursesNotLoginAdapter(getActivity());
        //分登陆和登录两种状态
        infiniteCoursePageAdapter = new InfiniteCoursePageAdapter(coursesViewPagerAdapter);
        coursesPageChangedLister = new CoursesPageChangedLister(pagers_container, infiniteCoursePageAdapter, DateTime.now());
        coursesPageChangedLister.setFragments(coursesViewPagerAdapter.getFragments());
        //            vp_class.setAdapter(coursesNotLoginAdapter);
        //            vp_class.setMonthCalendar(pagers_container.monthCalendar);
        //            infiniteCoursePageAdapter.notifyDataSetChanged();
        //            CoursesFragment coursesFragment = (CoursesFragment) infiniteCoursePageAdapter.getCurrentFragment();
        //            LogHelper.d(tag,"coursesFragment: "+coursesFragment.getDateTime().toString(CaldroidCustomConstant.simpleFormator));
        //        pagers_container.setInfiniteCoursePageAdapter(infiniteCoursePageAdapter);
        vp_class.setAdapter(infiniteCoursePageAdapter);
        vp_class.setCurrentItem(1000);
        //          coursesPageChangedLister.setCurrentPage(1000);
        coursesPageChangedLister.refreshFragment(1000);
        vp_class.addOnPageChangeListener(coursesPageChangedLister);
        vp_class.setMonthCalendar(pagers_container.monthCalendar);
        //        coursesPageChangedLister.setCurDateTime(DateTime.now());
        pagers_container.setCoursePageChangedListener(coursesPageChangedLister);
        //        infiniteCoursePageAdapter.notifyDataSetChanged();
        tv_title_month.setText(DateTime.now().toString(MONTH_DATETIME_FOMATOR));
        tv_title_week.setText("本周");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogHelper.d(tag, "onViewCreated");

    }

    @Override
    protected void onFragmentStartLazy() {
        super.onFragmentStartLazy();
        LogHelper.d(tag, "onFragmentStartLazy");

    }

    @Override
    protected void onResumeLazy() {
        super.onResumeLazy();
        LogHelper.d(tag, "onResumeLazy");
        if (isFirstLoaded) {
            isFirstLoaded = false;
            pagers_container.setViewMode(ViewMode.WEEK);
        }
        initRedDot();
        if (isFromLogin) {
            isFromLogin = false;
            coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
            pagers_container.setTextDotForDateTimeMap(teacherCourseManager.getHaveCourseMap());
            pagers_container.refreshMonthView();
            pagers_container.refreshWeeksView();

            //            infiniteCoursePageAdapter.notifyDataSetChanged();
        }
        //        if(isFromLogin){
        //            isFromLogin = false;
        //            if (curSelectedDateTime == null) {
        //                curSelectedDateTime = DateTime.now();
        //            }
        //            vp_class.setAdapter(null);
        //            LogHelper.d(tag, "LOGIN: " + curSelectedDateTime.toString(CaldroidCustomConstant.simpleFormator));
        //            teacherCourseManager.init();
        //            teacherCourseManager.setLoginState(true);
        //            coursesViewPagerAdapter = new CoursesViewPagerAdapter(getChildFragmentManager(), curSelectedDateTime);
        //            infiniteCoursePageAdapter = new InfiniteCoursePageAdapter(coursesViewPagerAdapter);
        //            coursesPageChangedLister = new CoursesPageChangedLister(pagers_container, infiniteCoursePageAdapter, curSelectedDateTime);
        //            coursesPageChangedLister.setFragments(coursesViewPagerAdapter.getFragments());
        //            coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
        //            pagers_container.setCoursePageChangedListener(coursesPageChangedLister);
        //            vp_class.setMonthCalendar(pagers_container.monthCalendar);
        //            vp_class.setAdapter(infiniteCoursePageAdapter);
        //            vp_class.setCurrentItem(1000);
        //            vp_class.setOffscreenPageLimit(1);
        ////            coursesPageChangedLister.setCurrentPage(1000);
        //            vp_class.addOnPageChangeListener(coursesPageChangedLister);
        //
        ////            coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
        ////            handler.postDelayed(new Runnable() {
        ////                @Override
        ////                public void run() {
        ////                    handler.sendEmptyMessage(LOGINAGAIN);
        ////                }
        ////            }, 500);
        //
        //
        //
        //            teacherCourseManager.initToday_before_after_6_month(DateTime.now());
        //        }
    }

    private void initRedDot() {
        //发送GET请求，获取信息
        if (NetworkUtils.isNetworkAvaialble(getActivity())) {
            final String token = "Bearer "
                    + SecurePreferences.getInstance(getActivity(), false).getString(Constants.KEY_ACCESS_TOKEN);
            if (SecurePreferences.getInstance(getActivity(), false).getString(Constants.KEY_ACCESS_TOKEN) != null) {
                mRedPointSubscriber = new RedPointSubscriber(getActivity());
                mRedPointSubscription = HttpManager.getRedPoint(token).subscribe(mRedPointSubscriber);
            } else {
                boolean haveCommonMsg = SharePreferenceUtils.getBooleanPref(getActivity(), Constants.HAVE_COMMON_MSG, false);
                setleftimageRedTip(haveCommonMsg);
            }
        } else {
            ToastUtil.showToastBottom(getActivity(), R.string.toast_error_no_net, Toast.LENGTH_SHORT);

        }
    }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public WeekdayArrayAdapter getNewWeekdayAdapter(int themeResource) {
        return new WeekdayArrayAdapter(
                getActivity(), android.R.layout.simple_list_item_1,
                getDaysOfWeek(), themeResource);
    }

    public ArrayList<String> getPickWeeksList(DateTime dateTime) {
        ArrayList<String> list = new ArrayList<>();
        if (listdateTimes == null) {
            listdateTimes = new ArrayList<>();
        } else {
            listdateTimes.clear();
        }

        //        list.add(getWeekStr(dateTime));
        //        listdateTimes.add(dateTime);
        //        list.add("本周");
        listdateTimes.add(dateTime);
        list.add(getWeekStr(dateTime));

        for (int i = 0; i < 7; i++) {
            listdateTimes.add(dateTime.plusWeeks(1));
            list.add(getWeekStr(dateTime.plusWeeks(1)));
            dateTime = dateTime.plusWeeks(1);
        }
        return list;
    }

    public String getWeekStr(DateTime dateTime) {
        DateTime now = DateTime.now();
        String str;
        if (dateTime.getMillis() < now.dayOfWeek().withMaximumValue().getMillis()
                && dateTime.getMillis() > now.dayOfWeek().withMinimumValue().getMillis()) {
            str = "本周";
        } else {
            str = dateTime.dayOfWeek().withMinimumValue().toString(WEEK_DATETIME_FOMATOR) + "-"
                    + dateTime.dayOfWeek().withMaximumValue().toString(WEEK_DATETIME_FOMATOR);
        }
        return str;
    }

    /**
     * To display the week day tv_title
     *
     * @return "SUN, MON, TUE, WED, THU, FRI, SAT"
     */
    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("一");
        list.add("二");
        list.add("三");
        list.add("四");
        list.add("五");
        list.add("六");
        list.add("日");
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_msgcenter://msg
                MobclickAgent.onEvent(getContext(), "jhyx_tap_schedule_bell");
                if (TextUtils.isEmpty(SecurePreferences.getInstance(getActivity(), false).getString(Constants.KEY_ACCESS_TOKEN))) {
//                    Intent intent = new Intent(getActivity(), LoginActivity.class);
//                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
//                    startActivity(intent);
                } else {
//                    Intent msgIntent = new Intent(getActivity(), MsgExpandCenterActivity.class);
//                    if (msgIntent != null) {
//                        msgIntent.putExtra(Constants.PUSH_EVENT, mPushEvent);
//                        mPushEvent = null;
//                    }
//                    msgIntent.putExtra(Constants.COMMON_MSG_SUM, mPub);
//                    msgIntent.putExtra(Constants.PRI_MSG_SUM, mPri);
//                    startActivity(msgIntent);
                }
                break;
            case R.id.btn_month_week://周月转换
                MobclickAgent.onEvent(getContext(), "jhyx_tap_schedule_wm");
                if (viewMode == ViewMode.MONTH) {
                    viewMode = ViewMode.WEEK;
                    btn_month_week.setImageResource(R.mipmap.nav_icon_month_default);
                    tv_title_month.setVisibility(View.INVISIBLE);
                    li_title_week.setVisibility(View.VISIBLE);
                } else {
                    viewMode = ViewMode.MONTH;
                    btn_month_week.setImageResource(R.mipmap.nav_icon_week_default);
                    tv_title_month.setVisibility(View.VISIBLE);
                    li_title_week.setVisibility(View.INVISIBLE);
                }
                pagers_container.convertWeek_Month(viewMode);
                break;
            case R.id.li_title_week://
                MobclickAgent.onEvent(getContext(), "jhyx_pull_schedule_titles");//课表下拉选择事件
                //                DateTime dateTimeStart = new DateTime();
                //                DateTime dateTimeEnd = new DateTime();
                //                pagers_container.convert_to_Week(dateTimeStart,dateTimeEnd);
                //                DateTime dateTimeStart = new DateTime();
                //                DateTime dateTimeEnd = new DateTime();
                //                pagers_container.convert_to_Week(dateTimeStart,dateTimeEnd);
                if (popPickWeekView != null) {
                    if (popPickWeekView.isShowing()) {
                        popPickWeekView.dismiss();
                    }
                    popPickWeekView = null;
                }
                if (popPickWeekView == null) {
                    LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.pop_list_weeks, null);
                    ListView listView = (ListView) view.findViewById(R.id.listview);
                    listView.setItemsCanFocus(true);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setOnItemClickListener(weekItemClickListener);
                    ArrayList<String> listStrWeeks = getPickWeeksList(curSelectedDateTime);
                    PickWeekAdapter adapter = new PickWeekAdapter(getActivity(), listStrWeeks);
                    listView.setAdapter(adapter);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.setElevation(-DisplayUtils.dip2px(getContext(), -5f));
                    }
                    popPickWeekView = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    popPickWeekView.setWidth(DisplayUtils.dip2px(getActivity(), 210));//210dp
                    popPickWeekView.setBackgroundDrawable(new BitmapDrawable());
                    popPickWeekView.setOutsideTouchable(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        popPickWeekView.setElevation(-DisplayUtils.dip2px(getContext(), -5f));
                    }

                    int width = v.getWidth();
                    int offsetx = DisplayUtils.dip2px(getActivity(), 210) / 2 - width / 2;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        popPickWeekView.showAsDropDown(v, -offsetx, 0, Gravity.CENTER_HORIZONTAL);
                    } else {
                        popPickWeekView.showAsDropDown(v, -offsetx, 0);
                    }
                } else {
                    int width = v.getWidth();
                    int offsetx = DisplayUtils.dip2px(getActivity(), 210) / 2 - width / 2;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        popPickWeekView.showAsDropDown(v, -offsetx, 0, Gravity.CENTER_HORIZONTAL);
                    } else {
                        popPickWeekView.showAsDropDown(v, -offsetx, 0);
                    }
                }
                break;
            //            case R.id.tv_loginorregister:
            //                Log.i(tag,"start login");
            //                break;
            default:
                break;
        }
    }

    /**
     * 课表上方下拉导航逻辑 由写死改为当前选择的的后八周（含当前）
     * 切换到周视图时，改变 日期选择的队列
     *
     * @param viewModeChnagedEvent
     */
    @UiThread
    @Subscribe
    public void onViewModeChnagedEvent(DateCalendar.ViewModeChangedEvent viewModeChnagedEvent) {
        if (viewMode == viewModeChnagedEvent.viewMode) {
            return;
        }
        MobclickAgent.onEvent(getContext(), "jhyx_pull_schedule_wm");
        this.viewMode = viewModeChnagedEvent.viewMode;
        if (viewMode == ViewMode.WEEK) {
            btn_month_week.setImageResource(R.mipmap.nav_icon_month_default);
            tv_title_month.setVisibility(View.INVISIBLE);
            li_title_week.setVisibility(View.VISIBLE);
            changePopWinListData();
        } else {
            btn_month_week.setImageResource(R.mipmap.nav_icon_week_default);
            tv_title_month.setVisibility(View.VISIBLE);
            li_title_week.setVisibility(View.INVISIBLE);
        }
    }

    //课表上方下拉导航逻辑 由写死改为当前选择的的后八周（含当前）
    private void changePopWinListData() {
        if (curSelectedDateTime == null) {
            curSelectedDateTime = DateTime.now();
        }
        //        listStrWeeks = getPickWeeksList(curSelectedDateTime);
    }

    @Override
    public void onDestroy() {
        if (eventBus != null) {
            eventBus.unregister(this);
        }
        unregisterReceiver();
        super.onDestroy();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LOGIN_SUCCESS_BROADCAST);
        intentFilter.addAction(Constants.LOGOUT_SUCCESS_BROADCAST);
        intentFilter.addAction(Constants.ACTION_RECEIVE_PUSH);
        intentFilter.addAction(Constants.BROAD_MSGCENTER_HAVEREADALL);
        intentFilter.addAction(Constants.BROAD_PRI_MSG_READED);
        getActivity().registerReceiver(loginInOutReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        try {
            getActivity().unregisterReceiver(loginInOutReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler     = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGINAGAIN: //重新登陆进来之后，重新设置日期
                    if (coursesPageChangedLister != null) {
                        coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
                    } else {
                        LogHelper.e(tag, "coursePageChangeLisenter null");
                    }

                    break;
            }


        }
    };
    private boolean isFromLogin = false;
    private String mPushEvent;//如果 推送来个人信息,进入消息中心需要到个人消息页面
    BroadcastReceiver loginInOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(tag, "onReceive: " + intent.getAction());
            if (intent.getAction().equals(Constants.LOGIN_SUCCESS_BROADCAST)) {
                isFromLogin = true;
                if (curSelectedDateTime == null) {
                    curSelectedDateTime = DateTime.now();
                }
                //                initLeftImage();
                //                vp_class.setAdapter(null);
                //                LogHelper.d(tag, "LOGIN: " + curSelectedDateTime.toString(CaldroidCustomConstant.simpleFormator));
                teacherCourseManager.init(curSelectedDateTime == null ? DateTime.now() : curSelectedDateTime);
                teacherCourseManager.setLoginState(true);
                pagers_container.setTextDotForDateTimeMap(teacherCourseManager.getHaveCourseMap());
                //                if (coursesViewPagerAdapter == null) {
                //                    LogHelper.d(tag, "coursesViewPagerAdapter  null)");
                //                    coursesViewPagerAdapter = new CoursesViewPagerAdapter(getChildFragmentManager(), curSelectedDateTime);
                //
                //                }
                //                if (infiniteCoursePageAdapter == null) {
                //                    LogHelper.d(tag, "infiniteCoursePageAdapter  null)");
                //                    infiniteCoursePageAdapter = new InfiniteCoursePageAdapter(coursesViewPagerAdapter);
                //                }
                //                if (coursesPageChangedLister == null) {
                //                    LogHelper.d(tag, "coursesPageChangedLister  null)");
                //                    coursesPageChangedLister = new CoursesPageChangedLister(pagers_container, infiniteCoursePageAdapter, curSelectedDateTime);
                //                    coursesPageChangedLister.setFragments(coursesViewPagerAdapter.getFragments());
                //                }
                coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
                //                pagers_container.setCoursePageChangedListener(coursesPageChangedLister);
                //                pagers_container.setInfiniteCoursePageAdapter(infiniteCoursePageAdapter);
                //                vp_class.setMonthCalendar(pagers_container.monthCalendar);
                //                vp_class.setAdapter(infiniteCoursePageAdapter);
                //                vp_class.setCurrentItem(1000);
                //                vp_class.setOffscreenPageLimit(1);
                pagers_container.refreshMonthView();
                pagers_container.refreshWeeksView();
                //                vp_class.addOnPageChangeListener(coursesPageChangedLister);

                teacherCourseManager.initToday_before_after_6_month(DateTime.now());
                //                infiniteCoursePageAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.LOGOUT_SUCCESS_BROADCAST)) {
                setleftimageRedTip(false);
                teacherCourseManager.setLoginState(false);
                Map<DateTime, Integer> dotMap = new HashMap<>();
                pagers_container.setTextDotForDateTimeMap(dotMap);
                //                CoursesNotLoginAdapter coursesNotLoginAdapter = new CoursesNotLoginAdapter(getActivity());
                //                vp_class.setAdapter(coursesNotLoginAdapter);
                Map<DateTime, Integer> map = new HashMap<>();
                pagers_container.setTextDotForDateTimeMap(map);
                infiniteCoursePageAdapter.notifyDataSetChanged();
                coursesPageChangedLister.setCurDateTime(curSelectedDateTime);
                pagers_container.refreshMonthView();
                pagers_container.refreshWeeksView();
            } else if (intent.getAction().equals(Constants.ACTION_RECEIVE_PUSH)) {//接收到新的推送消息
                initRedDot();
                setleftimageRedTip(true);
                //                mPushEvent = intent.getStringExtra(Constants.PUSH_EVENT);
                //                if (mPushEvent != null) {
                //                    LogHelper.d(tag, "mPushEvent: " + mPushEvent);
                //                }

            } else if (intent.getAction().equals(Constants.BROAD_MSGCENTER_HAVEREADALL)) {
                setleftimageRedTip(false);
            } else if (intent.getAction().equals(Constants.BROAD_PRI_MSG_READED)) {
                mPri = 0;
            }
        }
    };

    /**
     * 设置左上角出现小红点
     */
    private void setleftimageRedTip(boolean isshow) {
        if (isshow) {
            have_msg_unread.setVisibility(View.VISIBLE);
        } else {
            have_msg_unread.setVisibility(View.GONE);
        }
    }

    private void initLeftImage() {
        boolean isCommonMsgRead = false;
        boolean isPersonMsgRead = false;
        try {
            if (Reservoir.contains(Constants.IS_COMMONMSG_READEDALL)) {
                isCommonMsgRead = Reservoir.get(Constants.IS_COMMONMSG_READEDALL, Boolean.class);
                isPersonMsgRead = Reservoir.get(Constants.IS_PERSONMSG_READEDALL, Boolean.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!(isCommonMsgRead || isPersonMsgRead)) {
            setleftimageRedTip(true);
        } else {
            setleftimageRedTip(false);
        }
    }

    class RedPointSubscriber extends BaseSubscriber<RedPointEntity> {
        public RedPointSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        public void onCompleted() {

        }

        @Override
        protected void handleError(Throwable e) {

        }

        @Override
        public void onNext(RedPointEntity redPointEntity) {
            LogHelper.d(tag, "redPointEntity: " + redPointEntity);
            if (redPointEntity != null) {
                RedPointEntity.DataBean.MsgBox msgBox = redPointEntity.getData().getMsgBox();
                if (msgBox == null) {
                    return;
                }
                int total = msgBox.getTotal();
                setleftimageRedTip(total > 0);
                mPri = msgBox.getPri();
                mPub = msgBox.getPub();
            }
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        if (mRedPointSubscriber != null) {
            mRedPointSubscriber.onCancle();
        }
        if (mRedPointSubscription != null && !mRedPointSubscription.isUnsubscribed()) {
            mRedPointSubscription.unsubscribe();
        }
    }
}
