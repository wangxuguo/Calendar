package com.oceansky.example.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.example.R;
import com.oceansky.example.adapter.CourseAdapter;
import com.oceansky.example.constant.Constants;
import com.oceansky.example.customviews.BGAJHYXRefreshViewHolder;
import com.oceansky.example.customviews.CustomListView;
import com.oceansky.example.entity.CourseBeanForAdapter;
import com.oceansky.example.event.LoginSuccessEvent;
import com.oceansky.example.event.RxBus;
import com.oceansky.example.network.http.ApiException;
import com.oceansky.example.network.http.HttpManager;
import com.oceansky.example.network.response.CourseEntity;
import com.oceansky.example.network.subscribers.LoadingSubscriber;
import com.oceansky.example.utils.LogHelper;
import com.oceansky.example.utils.NetworkUtils;
import com.oceansky.example.utils.SecurePreferences;

import java.util.ArrayList;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import rx.Observable;
import rx.Subscription;

public class MineCourseFragment extends BaseLazyFragment implements BGARefreshLayout.BGARefreshLayoutDelegate, AdapterView.OnItemClickListener {
    private static final String TAG    = MineCourseFragment.class.getSimpleName();
    public static final  int    SIZE   = 0;//每次加载数量，取值范围1-50(为0时加载全部)
    public static final  int    OFFSET = 0;//偏移课程id(为0时加载全部)

    private int     mCourseStatus      = 0;
    private boolean mIsFirstRefreshing = true;//是否首次刷新

    private FrameLayout       mLoadingLayout;
    private AnimationDrawable mLoadingAnimation;
    private CustomListView    mListView;
    private ImageView         mLoadingImg;
    private View              mEmptyPage;
    private BGARefreshLayout  mRefreshLayout;
    private Context           mContext;

    private CourseAdapter                   mCourseAdapter;
    private DataLoadEndBroadcast            mDataLoadEndBroadcast;
    private boolean                         mIsRefreshing;
    private boolean                         mIsFirstLoading;//是否首次加载数据
    private ArrayList<CourseBeanForAdapter> mCourseDatas;
    private ArrayList<CourseBeanForAdapter> mCourseListCache;
    private ArrayList<CourseBeanForAdapter> mAllCourseList;
    private ArrayList<CourseBeanForAdapter> mInclassCourseList;
    private ArrayList<CourseBeanForAdapter> mWaitCourseList;
    private ArrayList<CourseBeanForAdapter> mFinishCourseList;
    private GetAllCoursesSubscriber         mGetAllCoursesSubscriber;
    private Subscription                    mGetAllCoursessubscription;
    private Subscription                    mGetCoursesByStateSubscription;
    private boolean                         mIsEmpty;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_mine_course);
        super.onCreateViewLazy(savedInstanceState);
        mContext = getActivity();
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    @Override
    void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            refreshDatas();
        } else {
            Intent intent = new Intent(Constants.ACTION_DATA_LOAD_FAILURE);
            intent.putExtra(Constants.LOADING_STATE, Constants.LOADING_STATE_NO_NET);
            mContext.sendBroadcast(intent);
        }
    }

    private void initView() {
        mLoadingLayout = (FrameLayout) findViewById(R.id.loading_layout);
        mLoadingImg = (ImageView) findViewById(R.id.loading);
        mListView = (CustomListView) findViewById(R.id.course_lv);
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.course_layout_refresh);
        initEmptyPage();
        initRefreshLayout();
        initReservoir();
        //加载动画
        mLoadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImg.getDrawable();
        mCourseDatas = new ArrayList<>();
        mCourseAdapter = new CourseAdapter(mContext, mCourseDatas);
        mListView.setAdapter(mCourseAdapter);
        mListView.setOnScrollYListener(scrollY -> {
            if (scrollY < 0 && mIsRefreshing) {
                mIsRefreshing = false;
                mRefreshLayout.endRefreshing();
            }
        });
        mListView.setOnItemClickListener(this);
        mDataLoadEndBroadcast = new DataLoadEndBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_DATA_LOAD_SUCCEED);
        intentFilter.addAction(Constants.ACTION_DATA_LOAD_FAILURE);
        mContext.registerReceiver(mDataLoadEndBroadcast, intentFilter);
    }

    /**
     * 初始化刷新头
     */
    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(false);
        BGAJHYXRefreshViewHolder bgaNormalRefreshViewHolder = new BGAJHYXRefreshViewHolder(mContext, true);
        bgaNormalRefreshViewHolder.setPullDownImageResource(R.mipmap.loading_logo_00025);
        bgaNormalRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshViewBackgroundColorRes(R.color.activity_bg_gray);
        bgaNormalRefreshViewHolder.setSpringDistanceScale(0);
        mRefreshLayout.setRefreshViewHolder(bgaNormalRefreshViewHolder);
    }

    private void initEmptyPage() {
        mEmptyPage = LayoutInflater.from(mContext).inflate(R.layout.layout_empty_page, mRefreshLayout, false);
        ImageView emptyIcon = (ImageView) mEmptyPage.findViewById(R.id.empty_iv_icon);
        emptyIcon.setImageResource(R.mipmap.icon_empty_pager_course);
        TextView emptyMsg = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        emptyMsg.setText(R.string.empty_msg_course);
        mRefreshLayout.addView(mEmptyPage);
        mIsEmpty = true;
    }

    private void initReservoir() {
        try {
            Reservoir.init(mContext, Constants.CACHE_MAX_SIZE);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void initData() {
        LogHelper.d(TAG, "initData");
        Bundle arguments = getArguments();
        if (arguments != null) {
            mCourseStatus = arguments.getInt(Constants.COURSE_STATUS);
            LogHelper.d(TAG, "CourseStatus: " + mCourseStatus);
        }
        try {
            if (Reservoir.contains(Constants.COURSE_LIST + mCourseStatus)) {
                mCourseListCache = Reservoir.get(Constants.COURSE_LIST + mCourseStatus,
                        new TypeToken<ArrayList<CourseBeanForAdapter>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "CourseListCache: " + mCourseListCache);
        mIsFirstLoading = mCourseListCache == null;
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            refreshDatas();
        } else {
            if (mCourseListCache != null) {
                refreshList(mCourseListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mIsRefreshing = true;
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            refreshDatas();
        } else {
            mRefreshLayout.endRefreshing();
            mIsRefreshing = false;
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    public void refreshDatas() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
        mGetAllCoursesSubscriber = new GetAllCoursesSubscriber(mContext);
        if (mIsFirstLoading || mCourseStatus == 0) {
            mGetAllCoursessubscription = HttpManager.getCourse(token, SIZE, OFFSET)
                    .flatMap(Observable::from)
                    .filter(courseEntity -> {
                        if (courseEntity == null) {
                            throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
                        }
                        ArrayList<CourseEntity.Times> times = courseEntity.getTimes();
                        return times != null && times.size() > 0;
                    }).subscribe(mGetAllCoursesSubscriber);
        } else {
            mGetCoursesByStateSubscription = HttpManager.getCourse(token, SIZE, OFFSET, mCourseStatus)
                    .flatMap(Observable::from)
                    .filter(courseEntity -> {
                        if (courseEntity == null) {
                            throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
                        }
                        ArrayList<CourseEntity.Times> times = courseEntity.getTimes();
                        return times != null && times.size() > 0;
                    }).subscribe(mGetAllCoursesSubscriber);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogHelper.d(TAG, "onItemClick: " + position);
        if (mCourseDatas.size() == 0) {
            return;
        }
        CourseBeanForAdapter courseBean = mCourseDatas.get(position);
        String detailUrl = courseBean.getDetailUrl();
//        Intent intent = new Intent(mContext, CourseDetailActivity.class);
//        intent.putExtra(Constants.WEBVIEW_URL, detailUrl);
//        intent.putExtra(Constants.WEBVIEW_TITLE, courseBean.getTitle());
//        startActivity(intent);
    }

    private class GetAllCoursesSubscriber extends LoadingSubscriber<CourseEntity> {

        public GetAllCoursesSubscriber(Context context) {
            super(context);
        }

        @Override
        public void onStart() {
            super.onStart();
            LogHelper.d(TAG, "onStart");
            if (mAllCourseList == null) {
                mAllCourseList = new ArrayList<>();
            } else {
                mAllCourseList.clear();
            }
            if (mInclassCourseList == null) {
                mInclassCourseList = new ArrayList<>();
            } else {
                mInclassCourseList.clear();
            }
            if (mWaitCourseList == null) {
                mWaitCourseList = new ArrayList<>();
            } else {
                mWaitCourseList.clear();
            }
            if (mFinishCourseList == null) {
                mFinishCourseList = new ArrayList<>();
            } else {
                mFinishCourseList.clear();
            }
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (mIsFirstLoading) {
                Intent intent = new Intent(Constants.ACTION_DATA_LOAD_FAILURE);
                intent.putExtra(Constants.LOADING_STATE, Constants.LOADING_STATE_FAIL);
                mContext.sendBroadcast(intent);
            } else {
                refreshList(mCourseListCache);
                refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
            }
        }

        @Override
        protected void showLoading() {
            if (mIsFirstRefreshing) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingAnimation.start();
            }
        }

        @Override
        protected void dismissLoading() {
            if (mIsFirstRefreshing) {
                mLoadingLayout.setVisibility(View.INVISIBLE);
                mLoadingAnimation.stop();
            } else {
                mRefreshLayout.setVisibility(View.VISIBLE);
                mRefreshLayout.endRefreshing();
                mIsRefreshing = false;
            }
        }

        @Override
        protected void handleError(Throwable e) {
            LogHelper.d(TAG, "handleError: " + e.getMessage());
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    break;
                case ApiException.ERROR_NO_NET:
                    handleErrorByState(Constants.LOADING_STATE_NO_NET);
                    break;
                case ApiException.ERROR_LOAD_FAIL:
                    handleErrorByState(Constants.LOADING_STATE_FAIL);
                    break;
                default:
                    handleErrorByState(Constants.LOADING_STATE_FAIL);
            }
        }

        private void handleErrorByState(int state) {
            if (mIsFirstLoading) {
                //发送广播通知其他页面进行状态刷新
                Intent intent = new Intent(Constants.ACTION_DATA_LOAD_FAILURE);
                intent.putExtra(Constants.LOADING_STATE, state);
                mContext.sendBroadcast(intent);
            } else {
                if (mCourseListCache != null) {
                    refreshList(mCourseListCache);
                }
                refreshLoadingState(state, false);
            }
        }

        @Override
        public void onNext(CourseEntity courseEntity) {
            ArrayList<CourseEntity.Times> times = courseEntity.getTimes();
            for (CourseEntity.Times time : times) {
                String start_time = time.getStart_time();
                String end_time = time.getEnd_time();
                CourseBeanForAdapter courseBeanForAdapter = new CourseBeanForAdapter();
                courseBeanForAdapter.setTitle(courseEntity.getTitle());
                courseBeanForAdapter.setStatus_des(courseEntity.getStatus_des());
                courseBeanForAdapter.setStart_date(courseEntity.getStart_date());
                courseBeanForAdapter.setEnd_date(courseEntity.getEnd_date());
                courseBeanForAdapter.setGrade_name(courseEntity.getGrade_name());
                courseBeanForAdapter.setClass_room(courseEntity.getClass_room());
                courseBeanForAdapter.setStart_time(start_time);
                courseBeanForAdapter.setEnd_time(end_time);
                courseBeanForAdapter.setDetailUrl(courseEntity.getDetail_url());
                int status = courseEntity.getStatus();
                courseBeanForAdapter.setStatus(status);

                mAllCourseList.add(courseBeanForAdapter);
                if (mIsFirstLoading) {
                    switch (status) {
                        case Constants.COURSE_STATE_ING:
                            mInclassCourseList.add(courseBeanForAdapter);
                            break;
                        case Constants.COURSE_STATE_WAIT:
                            mWaitCourseList.add(courseBeanForAdapter);
                            break;
                        case Constants.COURSE_STATE_END:
                            mFinishCourseList.add(courseBeanForAdapter);
                            break;
                    }
                }
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            LogHelper.d(TAG, "CourseList: " + mAllCourseList);
            try {
                if (mIsFirstLoading) {
                    Reservoir.put(Constants.COURSE_LIST + Constants.COURSE_STATE_ALL, mAllCourseList);
                    Reservoir.put(Constants.COURSE_LIST + Constants.COURSE_STATE_ING, mInclassCourseList);
                    Reservoir.put(Constants.COURSE_LIST + Constants.COURSE_STATE_WAIT, mWaitCourseList);
                    Reservoir.put(Constants.COURSE_LIST + Constants.COURSE_STATE_END, mFinishCourseList);
                    mIsFirstLoading = false;
                    mContext.sendBroadcast(new Intent(Constants.ACTION_DATA_LOAD_SUCCEED));
                } else {
                    Reservoir.put(Constants.COURSE_LIST + mCourseStatus, mAllCourseList);
                    refreshList(mAllCourseList);
                    refreshLoadingState(Constants.LOADING_STATE_SUCCESS, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshList(ArrayList<CourseBeanForAdapter> courseList) {
        if (courseList == null) {
            LogHelper.d(TAG, "courseList = null");
            return;
        }
        mCourseDatas.clear();
        mCourseDatas.addAll(courseList);
        if (mCourseDatas.size() < 1) {
            //没有对应状态的课程
            addEmptyPage();
        } else {
           removeEmptyPage();
        }
        mCourseAdapter.notifyDataSetChanged();
        mRefreshLayout.setVisibility(View.VISIBLE);
        mIsFirstRefreshing = false;
    }

    private void removeEmptyPage() {
        if (mIsEmpty) {
            mRefreshLayout.removeView(mEmptyPage);
            mIsEmpty = false;
        }
    }

    private void addEmptyPage() {
        if (!mIsEmpty) {
            mRefreshLayout.addView(mEmptyPage);
            mIsEmpty = true;
        }
    }

    private class DataLoadEndBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "DataLoadEndBroadcast onReceive: " + mCourseStatus);
            if (TextUtils.equals(intent.getAction(), Constants.ACTION_DATA_LOAD_SUCCEED)) {
                try {
                    if (Reservoir.contains(Constants.COURSE_LIST + mCourseStatus)) {
                        mCourseListCache = Reservoir.get(Constants.COURSE_LIST + mCourseStatus,
                                new TypeToken<ArrayList<CourseBeanForAdapter>>() {
                                }.getType());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Reservoir 偶尔会抛 cache is close 异常,导致mCourseListCache=null
                    initReservoir();
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    mLoadingAnimation.start();
                    refreshDatas();
                }
                if (mCourseListCache != null) {
                    refreshList(mCourseListCache);
                    mIsFirstLoading = false;
                    refreshLoadingState(Constants.LOADING_STATE_SUCCESS, false);
                }
            } else if (TextUtils.equals(intent.getAction(), Constants.ACTION_DATA_LOAD_FAILURE)) {
                int loadingState = intent.getIntExtra(Constants.LOADING_STATE, Constants.LOADING_STATE_FAIL);
                LogHelper.d(TAG, "loadingState: " + loadingState);
                refreshLoadingState(loadingState, mIsFirstLoading);
            }
        }
    }

    @Subscribe
    public void loginSuccess(LoginSuccessEvent loginSuccessEvent) {
        if (!TextUtils.equals(this.getClass().getSimpleName(), loginSuccessEvent.getClassName())) {
            return;
        }
        mIsFirstRefreshing = true;
        mErrorLayout.setVisibility(View.INVISIBLE);
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            refreshDatas();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        LogHelper.d(TAG, "MineCourseFragment onDestroy");
        if (mGetAllCoursesSubscriber != null) {
            mGetAllCoursesSubscriber.onCancle();
        }
        if (mGetAllCoursessubscription != null && !mGetAllCoursessubscription.isUnsubscribed()) {
            mGetAllCoursessubscription.unsubscribe();
            LogHelper.d(TAG, "GetCoursesSubscriber unsubscribe");
        }
        if (mGetCoursesByStateSubscription != null && !mGetCoursesByStateSubscription.isUnsubscribed()) {
            mGetCoursesByStateSubscription.unsubscribe();
            LogHelper.d(TAG, "GetCoursesByStateSubscriber unsubscribe");
        }
        try {
            mContext.unregisterReceiver(mDataLoadEndBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RxBus.getInstance().unregister(this);
    }
}
