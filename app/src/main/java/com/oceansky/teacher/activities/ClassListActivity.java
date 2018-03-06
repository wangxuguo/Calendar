package com.oceansky.teacher.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.adapter.ClassListAdapter;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.RefreshListView;
import com.oceansky.teacher.event.LoginSuccessEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.ClassListEntity;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import rx.Subscription;


public class ClassListActivity extends BaseActivityWithLoadingState implements RefreshListView.OnRefreshListener,
        AdapterView.OnItemClickListener {
    private static final String TAG = ClassListActivity.class.getSimpleName();

    private ArrayList<ClassListEntity> mClassListDatas;
    private ClassListAdapter           mClassListAdapter;
    private Context                    mContext;
    private RefreshListView            mListView;
    private FrameLayout                mLoadingLayout;
    private ImageView                  mIvLoading;
    private AnimationDrawable          mLoadingAnimation;
    private View                       mEmptyPage;
    private IntentFilter               mIntentFilter;
    private boolean                    mIsLogined;
    private boolean                    mIsFirstLoading;
    private boolean                    mIsFirstRefresh;//是否首次刷新
    private int                        mLoadState;
    private ArrayList<ClassListEntity> mClassListCache;
    private ClassListSubscriber        mClassListSubscriber;
    private Subscription               mClassListSubscription;
    private boolean                    mIsEmpty;

    @Override
    protected void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            getClassList();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);
        mContext = this;
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    private void initData() {
        mIsFirstRefresh = true;
        mIsLogined = !TextUtils.isEmpty(SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN));
        if (!mIsLogined) {
            refreshLoadingState(Constants.LOADING_STATE_UNLOGIN, true);
            return;
        }
        try {
            if (Reservoir.contains(Constants.CLASS_LIST)) {
                mClassListCache = Reservoir.get(Constants.CLASS_LIST, new TypeToken<ArrayList<ClassListEntity>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "classListCache " + mClassListCache);
        mIsFirstLoading = mClassListCache == null;
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            getClassList();
        } else {
            if (mClassListCache != null) {
                refreshListView(mClassListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_my_class));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mLoadingLayout = (FrameLayout) findViewById(R.id.loading_layout);
        mIvLoading = (ImageView) findViewById(R.id.loading);
        mListView = (RefreshListView) findViewById(R.id.class_lv);
        mClassListDatas = new ArrayList<>();
        mClassListAdapter = new ClassListAdapter(mContext, mClassListDatas);
        initEmptyPage();
        mListView.addHeaderView(mEmptyPage);
        mIsEmpty = true;
        mListView.setAdapter(mClassListAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_BROADCAST);
        mIntentFilter.addAction(Constants.LOGOUT_SUCCESS_BROADCAST);
        mContext.registerReceiver(loginBroadcastReceiver, mIntentFilter);
    }

    private void initEmptyPage() {
        //添加空页面
        mEmptyPage = View.inflate(mContext, R.layout.layout_empty_page, null);
        TextView tvEmpty = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        tvEmpty.setText(R.string.empty_msg_class);
        ImageView ivIcon = (ImageView) mEmptyPage.findViewById(R.id.empty_iv_icon);
        ivIcon.setImageResource(R.mipmap.icon_class_no_class);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int titleBarHeight = (int) getResources().getDimension(R.dimen.title_bar_height);
        int listViewPaddingBottom = (int) getResources().getDimension(R.dimen.padding_bottom_listview);
        int dividerHeight = (int) getResources().getDimension(R.dimen.dividerheight_listview);
        //获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        LogHelper.d(TAG, "statusBarHeight:" + statusBarHeight);
        int contentViewHeight = screenHeight - titleBarHeight - statusBarHeight - listViewPaddingBottom - dividerHeight;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentViewHeight);
        mEmptyPage.setLayoutParams(params);
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    private void refreshData() {
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            getClassList();
        } else {
            // 网络不可用，结束刷新
            mListView.setOnRefreshComplete();
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    private void getClassList() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
        mClassListSubscriber = new ClassListSubscriber(mContext);
        mClassListSubscription = HttpManager.getClassList(token).subscribe(mClassListSubscriber);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 1 || mClassListDatas == null || mClassListDatas.size() == 0) {
            return;
        }
        ClassListEntity item = mClassListDatas.get(position - 1);
        if (item != null) {
            MobclickAgent.onEvent(mContext, "jhyx_tap_class_name");
            int classId = item.getId();
            Intent intent = new Intent(mContext, ClassActivity.class);
            intent.putExtra(Constants.CLASS_ID, classId);
            intent.putExtra(Constants.CLASS_TITL, item.getTitle());
            startActivity(intent);
        }
    }

    class ClassListSubscriber extends LoadingSubscriber<ArrayList<ClassListEntity>> {
        private ArrayList<ClassListEntity> mClassList;

        public ClassListSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (mIsFirstRefresh && mClassListCache != null) {
                refreshListView(mClassListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, mIsFirstLoading);
        }

        @Override
        protected void showLoading() {
            LogHelper.d(TAG, "showLoading mIsFirstRefresh: " + mIsFirstRefresh);
            if (mIsFirstRefresh) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingAnimation.start();
            }
        }

        @Override
        protected void dismissLoading() {
            LogHelper.d(TAG, "dismissLoading mIsFirstRefresh: " + mIsFirstRefresh);
            if (mIsFirstRefresh) {
                mLoadingLayout.setVisibility(View.INVISIBLE);
                mLoadingAnimation.stop();
            } else {
                mListView.setOnRefreshComplete();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case "4013":
                    showTokenInvalidDialog();
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    break;
                default:
                    if (mIsFirstRefresh && mClassListCache != null) {
                        refreshListView(mClassListCache);
                    }
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, mIsFirstLoading);
            }
        }

        @Override
        public void onNext(ArrayList<ClassListEntity> classList) {
            if (classList == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            } else {
                mClassList = classList;
                mLoadState = Constants.LOADING_STATE_SUCCESS;
                try {
                    Reservoir.put(Constants.CLASS_LIST, classList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            refreshListView(mClassList);
            refreshLoadingState(mLoadState, mIsFirstLoading);
            mIsFirstLoading = false;
        }
    }

    private void refreshListView(ArrayList<ClassListEntity> classList) {
        LogHelper.d(TAG, "classList: " + classList);
        if (classList == null) {
            return;
        }
        mIsFirstRefresh = false;
        mClassListDatas.clear();
        mClassListDatas.addAll(classList);
        if (classList.size() > 0) {
            removeHeaderView();
        } else {
            //没有课程
            addHeaderView();
        }
        mClassListAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
    }

    private void removeHeaderView() {
        LogHelper.d(TAG, "removeHeaderView");
        if (!mIsEmpty) {
            return;
        }
        mListView.removeHeaderView(mEmptyPage);
        mIsEmpty = false;
    }

    private void addHeaderView() {
        LogHelper.d(TAG, "HeaderViewsCount: " + mListView.getHeaderViewsCount());
        if (mIsEmpty) {
            return;
        }
        LogHelper.d(TAG, "addHeaderView");
        if (android.os.Build.VERSION.SDK_INT > 18) {
            mListView.addHeaderView(mEmptyPage);
        } else {
            //SDK18以下setAdapter后调用addHeaderView方法会崩溃
            mListView.setAdapter(null);
            mListView.addHeaderView(mEmptyPage);
            mListView.setAdapter(mClassListAdapter);
        }
        mIsEmpty = true;
    }

    private BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "onReceive");
            if (TextUtils.equals(intent.getAction(), Constants.LOGIN_SUCCESS_BROADCAST)) {
                LogHelper.d(TAG, "LOGIN_SUCCESS");
                mErrorLayout.setVisibility(View.GONE);
                refreshData();
            } else if (TextUtils.equals(intent.getAction(), Constants.LOGOUT_SUCCESS_BROADCAST)) {
                refreshLoadingState(Constants.LOADING_STATE_UNLOGIN, true);
            }
        }
    };

    @Subscribe
    public void loginSuccess(LoginSuccessEvent loginSuccessEvent) {
        if (!TextUtils.equals(this.getClass().getSimpleName(), loginSuccessEvent.getClassName())) {
            return;
        }
        mIsFirstRefresh = true;
        mErrorLayout.setVisibility(View.INVISIBLE);
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            getClassList();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClassListSubscriber != null) {
            mClassListSubscriber.onCancle();
        }
        if (mClassListSubscription != null && !mClassListSubscription.isUnsubscribed()) {
            mClassListSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mContext.unregisterReceiver(loginBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RxBus.getInstance().unregister(this);
    }
}
