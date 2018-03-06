package com.oceansky.teacher.activities;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.adapter.OrdersAdapter;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.RefreshListView;
import com.oceansky.teacher.event.LoginSuccessEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.OrdersEntity;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;

public class OrdersActivity extends BaseActivityWithLoadingState implements RefreshListView.OnRefreshListener {
    private static final String TAG          = OrdersActivity.class.getSimpleName();
    public static final  int    ORDER_OFFSET = 0;
    public static final  int    ORDER_LIMIT  = 100;

    @Bind(R.id.orders_lv)
    RefreshListView mListView;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mIvLoading;

    private RelativeLayout                     mEmptyPage;
    private AnimationDrawable                  mLoadingAnimation;
    private ArrayList<OrdersEntity.OrdersData> mOrdersDatas;
    private OrdersAdapter                      mOrdersAdapter;
    private int                                mLoadState;
    private boolean                            mIsFirstLoading;
    private ArrayList<OrdersEntity.OrdersData> mOrderListCache;
    private OrdersSubscriber                   mOrdersSubscriber;
    private boolean                            mIsFirstRefresh;
    private Subscription                       mOrdersSubscription;
    private boolean                            mIsEmpty;

    @Override
    protected void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        if (NetworkUtils.isNetworkAvaialble(this)) {
            getOrders();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.titlt_orders));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mEmptyPage = (RelativeLayout) View.inflate(this, R.layout.layout_empty_page, null);
        initEmptyPage();
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
        mOrdersDatas = new ArrayList<>();
        mOrdersAdapter = new OrdersAdapter(this, mOrdersDatas);
        mListView.addHeaderView(mEmptyPage);
        mIsEmpty = true;
        mListView.setAdapter(mOrdersAdapter);
        mListView.setOnRefreshListener(this);
    }

    private void initData() {
        mIsFirstRefresh = true;
        mOrderListCache = null;
        try {
            if (Reservoir.contains(Constants.ORDER_LIST)) {
                mOrderListCache = Reservoir.get(Constants.ORDER_LIST, new TypeToken<ArrayList<OrdersEntity.OrdersData>>() {
                }.getType());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "orderListCache " + mOrderListCache);
        mIsFirstLoading = mOrderListCache == null;
        if (NetworkUtils.isNetworkAvaialble(this)) {
            // 如果网络可用，则加载网络数据
            getOrders();
        } else {
            if (mOrderListCache != null) {
                refreshListView(mOrderListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    @Override
    public void onRefresh() {
        refreshDatas();
    }

    private void refreshDatas() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            getOrders();
        } else {
            mListView.setOnRefreshComplete();
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    private void getOrders() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
        String teacher_id = SecurePreferences.getInstance(this, false).getString(Constants.KEY_TEACHER_ID);
        mOrdersSubscriber = new OrdersSubscriber(this);
        mOrdersSubscription = HttpManager.getOrders(token, ORDER_OFFSET, ORDER_LIMIT, teacher_id).subscribe(mOrdersSubscriber);
    }

    private void initEmptyPage() {
        ImageView emptyIcon = (ImageView) mEmptyPage.findViewById(R.id.empty_iv_icon);
        emptyIcon.setImageResource(R.mipmap.icon_orders);
        TextView emptyMsg = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        emptyMsg.setText(R.string.empty_msg_orders);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int titleBarHeight = (int) getResources().getDimension(R.dimen.title_bar_height);
        int paddingbottom = (int) getResources().getDimension(R.dimen.padding_bottom_listview);
        int dividerHeight = (int) getResources().getDimension(R.dimen.dividerheight_listview);
        //获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        LogHelper.d(TAG, "statusBarHeight:" + statusBarHeight);
        int contentViewHeight = screenHeight - titleBarHeight - paddingbottom - statusBarHeight - dividerHeight;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentViewHeight);
        mEmptyPage.setLayoutParams(params);
    }

    class OrdersSubscriber extends LoadingSubscriber<OrdersEntity> {

        private ArrayList<OrdersEntity.OrdersData> mOrderList;

        public OrdersSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (mIsFirstRefresh && mOrderListCache != null) {
                refreshListView(mOrderListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, mIsFirstLoading);
        }

        @Override
        protected void showLoading() {
            if (mIsFirstRefresh) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingAnimation.start();
            }
        }

        @Override
        protected void dismissLoading() {
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
                    if (mIsFirstRefresh && mOrderListCache != null) {
                        refreshListView(mOrderListCache);
                    }
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, mIsFirstLoading);
            }
        }

        @Override
        public void onNext(OrdersEntity ordersEntity) {
            LogHelper.d(TAG, "ordersEntity: " + ordersEntity);
            if (ordersEntity == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
            ArrayList<OrdersEntity.OrdersData> orderList = ordersEntity.getOrderList();
            if (orderList != null) {
                mOrderList = orderList;
                mLoadState = Constants.LOADING_STATE_SUCCESS;
                mIsFirstLoading = false;
                try {
                    Reservoir.put(Constants.ORDER_LIST, orderList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            refreshListView(mOrderList);
            refreshLoadingState(mLoadState, mIsFirstLoading);
        }
    }

    private void refreshListView(ArrayList<OrdersEntity.OrdersData> orderList) {
        if (orderList == null) {
            return;
        }
        mIsFirstRefresh = false;
        mOrdersDatas.clear();
        mOrdersDatas.addAll(orderList);
        if (orderList.size() == 0) {
            //没有课程
            addHeaderView();
        } else {
            removeHeaderView();
        }
        mOrdersAdapter.notifyDataSetChanged();
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
            mListView.setAdapter(mOrdersAdapter);
        }
        mIsEmpty = true;
    }

    @Subscribe
    public void loginSuccess(LoginSuccessEvent loginSuccessEvent) {
        if (!TextUtils.equals(this.getClass().getSimpleName(), loginSuccessEvent.getClassName())) {
            return;
        }
        mIsFirstRefresh = true;
        mErrorLayout.setVisibility(View.INVISIBLE);
        refreshDatas();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOrdersSubscriber != null) {
            mOrdersSubscriber.onCancle();
        }
        if (mOrdersSubscription != null && !mOrdersSubscription.isUnsubscribed()) {
            mOrdersSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
