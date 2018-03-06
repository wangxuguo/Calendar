package com.oceansky.teacher.manager;

import android.content.Context;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.oceansky.teacher.constant.CaldroidCustomConstant;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.entity.TearcherCourseListItemBean;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.TeacherCourseEntity;
import com.oceansky.teacher.network.subscribers.BaseSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.SecurePreferences;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rx.Subscription;

/**
 * 默认读取数据读取一年的数据，每次按一年的数据进行缓存 从1月1日到次年1月1日
 * User: 王旭国
 * Date: 16/6/16 13:00
 * Email:wangxuguo@jhyx.com.cn
 */
public class TeacherCourseManager {
    private static final String WEEK_DATETIME_FOMATOR = "MM.dd";
    private static final String TAG                   = TeacherCourseManager.class.getSimpleName();
    private static TeacherCourseManager teacherCourseManager;
    private static final String DateTime_FORMATOR      = "yyyyMM";
    private static final String DateTimeWHOLE_FORMATOR = "yyyyMMdd hh:mm:ss";
    private final Context mContext;
    private boolean loginState = true;
    private Object  mapLock    = new Object();
    private TeacherCourseBeanSubscriber mTeacherCourseBeanSubscriber;
    private Subscription                mTeacherCourseSubscription;

    private TeacherCourseManager(Context context) {
        this.mContext = context;
    }


    public static TeacherCourseManager getInstance(Context context) {
        if (teacherCourseManager == null) {
            synchronized (TeacherCourseManager.class) {
                if (teacherCourseManager == null) {
                    teacherCourseManager = new TeacherCourseManager(context);
                    try {
                        Reservoir.init(context, 2 * 1024 * 1024 * 8);
                    } catch (Exception e) {
                        LogHelper.e(TAG, "Reservoir.init error");
                        e.printStackTrace();
                    }
                }
            }
        }
        return teacherCourseManager;
    }

    private Map<DateTime, ArrayList<TearcherCourseListItemBean>> hashMap       = new HashMap<>();
    private Map<String, ArrayList<TeacherCourseEntity>>          yearMap       = new HashMap<>();
    private Map<DateTime, Integer>                               haveCourseMap = new HashMap<>();
    private Map<String, Boolean>                                 isGetNetData  = new HashMap<>();

    /**
     * 初使化 从缓存中获取数据，获得是否有数据（有课）的标示，通过时间区间进行读取
     */
    public void init(DateTime dateTime) {
        String uid = SecurePreferences.getInstance(mContext, true).getString(Constants.KEY_USER_ID);
        try {
            if (Reservoir.contains(Constants.TEACHERCOURSE_DATA + "_" + uid + "_" + dateTime.getYear())) {
                ArrayList<TeacherCourseEntity> mListCache =
                        Reservoir.get(Constants.TEACHERCOURSE_DATA + "_" + uid + "_" + DateTime.now().getYear(),
                                new TypeToken<ArrayList<TeacherCourseEntity>>() {
                                }.getType());
                if (mListCache != null) {
                    LogHelper.d(TAG, "init mListCache Not null " + mListCache.toString());
                    parseTeacherCourseBean(mListCache);
                } else {
                    LogHelper.d(TAG, "init mListCache null ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getHaveTimeHashTable();
    }


    private void getHaveTimeHashTable() {
        for (DateTime dateTime : hashMap.keySet()) {
            ArrayList<TearcherCourseListItemBean> list = hashMap.get(dateTime);
            haveCourseMap.put(dateTime, list.size());
        }

    }

    /**
     * 按照 时间 区间获取是否是课（数据）的数据Map
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public Map<DateTime, Integer> getHaveCourseMap(DateTime startTime, DateTime endTime) {
        //////????????
        return haveCourseMap;
    }

    public Map<DateTime, Integer> getHaveCourseMap() {
        return haveCourseMap;
    }

    /**
     * 获取指定日期的课程列表
     *
     * @param dateTime
     * @return 返回空（未登陆状态）与 size为0的空list
     */
    public ArrayList<TearcherCourseListItemBean> getTeacherCourse(DateTime dateTime) {
        synchronized (mapLock) {
            LogHelper.d(TAG, "getTeacherCourse: " + formatDateTime(dateTime).toString(CaldroidCustomConstant.simpleFormator));
            if (dateTime.withTime(0, 0, 0, 0).equals(DateTime.now().withTime(0, 0, 0, 0))) {
                LogHelper.d(TAG, "Today");
                if (hashMap.containsKey(dateTime.withTime(12, 0, 0, 0))) {
                    LogHelper.d(TAG, "hashMap.get(dateTime):  withTime(12,0,0,0)" + hashMap.get(formatDateTime(dateTime)));
                    if (loginState) {
                        ArrayList<TearcherCourseListItemBean> list = hashMap.get(formatDateTime(dateTime));
                        Collections.sort(list, new TeacherCourseBeanComparator());
                        return list;
                    } else {
                        return null;
                    }
                }
            }
            if (hashMap.containsKey(formatDateTime(dateTime))) {
                LogHelper.d(TAG, "hashMap.get(dateTime): " + hashMap.get(formatDateTime(dateTime)));
                if (loginState) {
                    ArrayList<TearcherCourseListItemBean> list = hashMap.get(formatDateTime(dateTime));
                    Collections.sort(list, new TeacherCourseBeanComparator());
                    LogHelper.d(TAG, "list: " + list.size());
                    return list;
                } else {
                    return null;
                }

            } else {
                if (loginState) {
                    return new ArrayList<>();
                } else {
                    return null;
                }

            }
        }
    }

    public ArrayList<TeacherCourseEntity> getTeacherCource(DateTime dateTime) {
        LogHelper.d(TAG, "TEST GSON PARSE TExt: ");
        //        Gson gson = new Gson();
        //        TeacherClassBean te =  gson.fromJson(str,TeacherClassBean.class);
        //        LogHelper.d(TAG,te.getData().size()+"");
        //        TeacherClassBean.DataBean da = te.getData().get(0);
        //        String timeinfo =  da.getTime_info();
        //        JSONArray ja = new JSONArray();
        //        try {
        //            String time1 = (String) ja.get(0);
        //            LogHelper.d(TAG,"time1: "+time1);
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //            LogHelper.e(TAG,"JSONException ");
        //        }
        ArrayList<TeacherCourseEntity> list = getRandomSizeList(dateTime);
        return list;
    }

    private ArrayList<TeacherCourseEntity> getRandomSizeList(DateTime dateTime) {
        Random random = new Random();
        int courseId = random.nextInt(20) * 10 + random.nextInt(20);
        String[] grades = {"高一", "高二", "高三", "初一", "初二", "初三", "小学"};
        String[] courses = {"语文", "数学", "英语", "物理", "化学", "生物", "地理", "政治"};
        ArrayList<TeacherCourseEntity> list = new ArrayList<>();
        int count = new Random().nextInt(2);
        for (int i = 0; i < count; i++) {
            TeacherCourseEntity teacherCourse = new TeacherCourseEntity();
            teacherCourse.setGrade_name(grades[random.nextInt(7)]);
            teacherCourse.setId(courseId + i);
            teacherCourse.setLesson_name(courses[random.nextInt(8)]);
            teacherCourse.setTitle(teacherCourse.getGrade_name() + "  " + teacherCourse.getLesson_name());
            //            teacherCourse.setTime_info(getRandomTimeInfo(dateTime));
            list.add(teacherCourse);
        }
        return list;
    }

    public String getRandomTimeInfo(DateTime dateTime) {
        //        return new String(generateCaptcha(new Random().nextInt(40)+1));
        DateTime d = dateTime.hourOfDay().withMinimumValue().plusHours(8 + new Random().nextInt(16));
        return getWeekStr(d);
    }

    public char[] generateCaptcha(int length) {
        char[] captcha = new char[length];
        for (int i = 0; i < length; i++) {
            captcha[i] = (char) (new Random().nextInt(0x559D - 0x53E3) + 0x53E3);
        }
        return captcha;
    }

    public String getWeekStr(DateTime dateTime) {
        return dateTime.dayOfWeek().withMinimumValue().toString(WEEK_DATETIME_FOMATOR) + "-"
                + dateTime.dayOfWeek().withMaximumValue().toString(WEEK_DATETIME_FOMATOR);
    }


    /**
     * 初使化数据，从本地读取或者从网络加载
     * 网络刷新数据
     *
     * @param now
     */
    public void initToday_before_after_6_month(DateTime now) {
        if (loginState) {
            DateTime startDateTime = now.dayOfYear().withMinimumValue();
            DateTime endDateTime = now.dayOfYear().withMaximumValue();
            String start = startDateTime.toString(DateTime_FORMATOR);
            String end = endDateTime.toString(DateTime_FORMATOR);
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            LogHelper.d(TAG, "getTeacherCoursesPeriodMonth: " + token + "  " + start + " " + " " + end);
            mTeacherCourseBeanSubscriber = new TeacherCourseBeanSubscriber(mContext, now);
            mTeacherCourseSubscription = HttpManager.getTeacherCoursesPeriodMonth(token, start, end).subscribe(mTeacherCourseBeanSubscriber);
            LogHelper.d(TAG, "send TeacherCourse");
        } else {
            LogHelper.d(TAG, "loginState false  ");
        }
    }

    /**
     * 预先加载某一年的数据
     *
     * @param year
     */
    public void load(int year) {
        try {
            String uid = SecurePreferences.getInstance(mContext, true).getString(Constants.KEY_USER_ID);
            ArrayList<TeacherCourseEntity> mListCache = null;
            if (Reservoir.contains(Constants.TEACHERCOURSE_DATA + "_" + uid + "_" + year)) {
                mListCache = Reservoir.get(Constants.TEACHERCOURSE_DATA + "_" + uid + "_" + year,
                        new TypeToken<ArrayList<TeacherCourseEntity>>() {
                        }.getType());
            }
            if (mListCache != null) {
                if (isGetNetData.containsKey(year + "")) {
                    LogHelper.d(TAG, "year: " + year + " 已经读取过数据 不再从网络获取");
                } else { //  每次有一次数据所有加载的一个过程
                    isGetNetData.put(year + "", true);
                    initToday_before_after_6_month(new DateTime(year, 2, 1, 0, 0, 0));
                }
                LogHelper.d(TAG, "load: " + year + " mListCache Not null " + mListCache.toString());
                if (yearMap.get(String.valueOf(year)).equals(mListCache)) {//当前加载的数据相同 则不必重复加载???脏数据的问题
                    LogHelper.d(TAG, "yearMap has mListCache 不用再解析加载");

                } else {
                    LogHelper.d(TAG, "yearMap  mListCache 不一致  解析并加载");
                    parseTeacherCourseBean(mListCache);
                }

            } else {
                LogHelper.d(TAG, "load: " + year + " mListCache null ");
                //?????????????????????????????????????????????数据发生变化，脏数据问题
                initToday_before_after_6_month(new DateTime(year, 2, 1, 0, 0, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        getHaveTimeHashTable();
    }

    /**
     * 登录状态，true为已经登录，false为未登录或者登录失效
     * ？？？？？？必须得考虑用户账号切换发生的变化,此类状态下是否需要清空所有缓存数据？？？
     *
     * @param loginState
     */
    public void setLoginState(boolean loginState) {
        this.loginState = loginState;
        hashMap.clear();
        yearMap.clear();
        haveCourseMap.clear();
    }

    private class TeacherCourseBeanSubscriber extends BaseSubscriber<ArrayList<TeacherCourseEntity>> {


        private DateTime dateTime;

        public TeacherCourseBeanSubscriber(Context context, DateTime now) {
            super(context);
            this.dateTime = now;
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        public void onNext(ArrayList<TeacherCourseEntity> teacherCourseList) {
            try {
                String uid = SecurePreferences.getInstance(mContext, true).getString(Constants.KEY_USER_ID);
                Reservoir.put(Constants.TEACHERCOURSE_DATA + "_" + uid + "_" + dateTime.getYear(), teacherCourseList);
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.e(TAG, "Reservoir error: " + Constants.TEACHERCOURSE_DATA + "_" + dateTime.getYear() + " " + e.toString());
            }
            if (yearMap.containsKey(dateTime.getYear() + "")) {
                if (yearMap.get(dateTime.getYear() + "").equals(teacherCourseList)) {
                    LogHelper.d(TAG, "yearMap 已经存在相同的网络数据,不必缓存");
                } else {
                    synchronized (mapLock) {
                        yearMap.put(dateTime.getYear() + "", teacherCourseList);
                        deleteTeacherMap(dateTime.getYear());
                        parseTeacherCourseBean(teacherCourseList);
                        LogHelper.d(TAG, "缓存 到 yearMap  通知数据已经变更");
                        getHaveTimeHashTable();
                        //                        notifyTeacherCourBeanChanged();
                    }
                }
            } else {
                yearMap.put(dateTime.getYear() + "", teacherCourseList);
                parseTeacherCourseBean(teacherCourseList);
                getHaveTimeHashTable();
            }
            notifyTeacherCourBeanChanged();
        }

        @Override
        protected void handleError(Throwable e) {
            LogHelper.d(TAG, "Error: " + e.getMessage());
        }
    }

    /**
     * 通知数据已经发生变化，需要动态刷新页面
     */
    private void notifyTeacherCourBeanChanged() {
        EventBus.getDefault().post(new TeacherCourseChanged(true));
    }

    /**
     * 按照 年删除旧数据，新增新的数据
     *
     * @param year
     */
    private void deleteTeacherMap(int year) {
        synchronized (mapLock) {
            DateTime dt = new DateTime(year, 2, 1, 0, 0, 0);
            DateTime min = dt.dayOfYear().withMinimumValue();
            DateTime max = dt.dayOfYear().withMaximumValue();
            for (DateTime dateTime : hashMap.keySet()) {
                if (dateTime.getMillis() < max.getMillis() || dateTime.getMillis() > min.getMillis()) {
                    LogHelper.d(TAG, "deleteTeacherMap: " + year + " DateTime: " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
                    hashMap.remove(hashMap.get(dateTime));
                }
            }
        }
    }

    private void parseTeacherCourseBean(ArrayList<TeacherCourseEntity> teacherCourseList) {
        LogHelper.d(TAG, "teacherCourseList : " + teacherCourseList);
        for (int i = 0; i < teacherCourseList.size(); i++) {
            TeacherCourseEntity data = teacherCourseList.get(i);
            TearcherCourseListItemBean item = new TearcherCourseListItemBean();
            item.setTitle(data.getTitle());
            item.setClass_room(data.getClass_room());
            item.setGrade_id(data.getGrade_id());
            item.setGrade_name(data.getGrade_name());
            item.setLesson_id(data.getLesson_id());
            item.setLesson_name(data.getLesson_name());
            item.setDetail_url(data.getDetail_url());
            item.setId(data.getId());
            //            LogHelper.d(TAG, "setId");
            TeacherCourseEntity.ClassTimesBean class_time = data.getClass_times().get(0);
            item.setStarttime(class_time.getStart_time());
            item.setEndtime(class_time.getEnd_time());
            Map<String, List<Integer>> time_info = data.getTime_info();
            //            LogHelper.d(TAG, "time_info");
            for (String key : time_info.keySet()) {
                //                LogHelper.d(TAG, "key: " + key);
                DateTime dateTime = DateTime.parse(key, DateTimeFormat.forPattern(DateTime_FORMATOR));
                //                LogHelper.d(TAG, "key : dateTime" + dateTime.toString(DateTimeWHOLE_FORMATOR));
                ArrayList<Integer> list = (ArrayList<Integer>) time_info.get(key);
                for (int j = 0; j < list.size(); j++) {
                    int day = list.get(j);
                    //                    LogHelper.d(TAG, "day: " + day);
                    DateTime date = dateTime.withDayOfMonth(day).withTime(0, 0, 0, 0);
                    LogHelper.d(TAG, "key : date:  " + date.toString(DateTimeWHOLE_FORMATOR));
                    if (hashMap.containsKey(date)) {
                        ArrayList<TearcherCourseListItemBean> itemlist = hashMap.get(date);
                        if (!itemlist.contains(item)) {
                            itemlist.add(item);
                            LogHelper.d(TAG, "remove & put date: " + date.toString(CaldroidCustomConstant.simpleFormator));
                            hashMap.remove(date);
                            hashMap.put(date, itemlist);
                        } else {
                            LogHelper.d(TAG, "hashMap contains item: " + item.toString());
                        }
                    } else {
                        ArrayList<TearcherCourseListItemBean> itemlist = new ArrayList<>();
                        itemlist.add(item);
                        LogHelper.d(TAG, "put hashMap: " + date.toString(DateTimeWHOLE_FORMATOR) + " " + item.toString());
                        hashMap.put(date, itemlist);
                    }

                }
            }
            LogHelper.d(TAG, "parseFinished");
        }
    }

    public static class TeacherCourseChanged {
        public boolean ischanged = true;

        public TeacherCourseChanged(boolean ischanged) {
            this.ischanged = ischanged;
        }
    }

    public static DateTime formatDateTime(DateTime dateTime) {

        DateTime da = dateTime.withTime(0, 0, 0, 0);
        //        LogHelper.d(TAG,"formatDateTime : "+dateTime.toString(CaldroidCustomConstant.simpleFormator)
        //                + da.toString(CaldroidCustomConstant.simpleFormator));
        return da;
    }

    private class TeacherCourseBeanComparator implements java.util.Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            TearcherCourseListItemBean tc1 = (TearcherCourseListItemBean) lhs;
            TearcherCourseListItemBean tc2 = (TearcherCourseListItemBean) rhs;
            DateTime dateTime1 = DateTime.parse(tc1.getStarttime(), DateTimeFormat.forPattern("HH:mm"));
            DateTime dateTime2 = DateTime.parse(tc2.getStarttime(), DateTimeFormat.forPattern("HH:mm"));

            if (dateTime1.getMillis() > dateTime2.getMillis()) {
                return 1;
            } else if (dateTime1.getMillis() == dateTime2.getMillis()) {
                return 0;
            } else {
                return -1;
            }

        }
    }
}
