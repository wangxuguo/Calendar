package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anupcowkur.reservoir.Reservoir;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oceansky.teacher.R;
import com.oceansky.teacher.adapter.ClassStudentAdapter;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.customviews.BGAJHYXRefreshViewHolder;
import com.oceansky.teacher.customviews.CustomGridView;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.ClassEntity;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;

public class ClassActivity extends BaseActivityWithLoadingState implements BGARefreshLayout.BGARefreshLayoutDelegate,
        AdapterView.OnItemClickListener {
    private static final String TAG                = ClassActivity.class.getSimpleName();
    private static final int    GRID_ITEM_WIDTH    = 166 / 2;
    public static final  int    HORIZONTAL_SPACING = 10 / 2;

    @Bind(R.id.class_student_gv)
    CustomGridView mStudentsGridView;

    @Bind(R.id.class_layout_refresh)
    BGARefreshLayout mRefreshLayout;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mIvLoading;

    private ArrayList<ClassEntity.Kid> mClassStudentsDatas;
    private ClassStudentAdapter        mClassStudentAdapter;
    private int                        mClassId;
    private View                       mAssistantView;
    private TextView                   mTitle;
    private TextView                   mTvPhoto;
    private CircleImageView            mIvPhoto;
    private RelativeLayout             mCall;
    private boolean                    mIsRefreshing;
    private AnimationDrawable          mLoadingAnimation;
    private RelativeLayout             mEmptyPage;
    private boolean                    mIsFirstLoading;
    private int                        mLoadState;
    private ClassEntity                mClassDataCache;
    private Intent                     mCallIntent;
    private ClassDetailSubscriber      mClassDetailSubscriber;
    private boolean                    mIsFirstRefresh;
    private Subscription               mClassDetailSubscription;

    @Override
    protected void onErrorLayoutClick() {
        mRefreshLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);
        if (NetworkUtils.isNetworkAvaialble(this)) {
            getClassDetail();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mClassId = getIntent().getIntExtra(Constants.CLASS_ID, -1);
        String classTitle = getIntent().getStringExtra(Constants.CLASS_TITL);
        mTitleBar.setTitle(classTitle);
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        initRefreshLayout();

        mAssistantView = View.inflate(this, R.layout.item_class_assistant, null);
        mEmptyPage = (RelativeLayout) mAssistantView.findViewById(R.id.layout_empty_page);
        initEmptyPage();
        mStudentsGridView.addHeaderView(mAssistantView);

        int itemWidth = caculateItemWidth();
        mClassStudentsDatas = new ArrayList<>();
        mClassStudentAdapter = new ClassStudentAdapter(this, mClassStudentsDatas, itemWidth);
        mStudentsGridView.setAdapter(mClassStudentAdapter);
        mStudentsGridView.setOnItemClickListener(this);
        mStudentsGridView.setOnScrollListener(scrollY -> {
            LogHelper.d(TAG, "mIsRefreshing: " + mIsRefreshing + " scrollY: " + scrollY);
            if (scrollY < 0 && mIsRefreshing) {
                mIsRefreshing = false;
                mRefreshLayout.endRefreshing();
            }
        });

        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
    }

    private void initEmptyPage() {
        ImageView emptyIcon = (ImageView) mEmptyPage.findViewById(R.id.empty_iv_icon);
        emptyIcon.setImageResource(R.mipmap.icon_class_no_student);
        TextView emptyMsg = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        emptyMsg.setText(R.string.empty_msg_student);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int titleBarHeight = (int) getResources().getDimension(R.dimen.title_bar_height);
        int gradviewPadding = (int) getResources().getDimension(R.dimen.padding_bottom_gridview);
        int itemHeight = (int) getResources().getDimension(R.dimen.class_assistant_item_height);
        int itempadding = (int) (getResources().getDimension(R.dimen.padding_bottom_item_assistant)
                + getResources().getDimension(R.dimen.padding_top_item_assistant));
        //获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int contentViewHeight = screenHeight - titleBarHeight - itemHeight - itempadding - gradviewPadding - statusBarHeight;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentViewHeight);
        mEmptyPage.setLayoutParams(params);
    }

    /**
     * 根据屏幕尺寸动态计算gridview的item的宽度
     *
     * @return gridview的item的宽度
     */
    private int caculateItemWidth() {
        int paddingLeft = mStudentsGridView.getPaddingLeft();
        int paddingRight = mStudentsGridView.getPaddingRight();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        int screenWidth = displayMetrics.widthPixels;
        int columNum = (int) ((screenWidth - paddingLeft - paddingRight) / (GRID_ITEM_WIDTH * density));
        LogHelper.d(TAG, "columNum: " + columNum);
        int space = (int) (screenWidth - paddingLeft - paddingRight
                - GRID_ITEM_WIDTH * density * columNum - HORIZONTAL_SPACING * density * (columNum - 1));
        if (space < 0) {
            columNum = columNum - 1;
            space = (int) (screenWidth - paddingLeft - paddingRight
                    - GRID_ITEM_WIDTH * density * columNum - HORIZONTAL_SPACING * density * (columNum - 1));
        }
        int itemWidth = (int) (GRID_ITEM_WIDTH * density + space / columNum);
        LogHelper.d(TAG, "itemWidth: " + itemWidth);
        mStudentsGridView.setColumnWidth(itemWidth);
        mStudentsGridView.setNumColumns(columNum);
        return itemWidth;
    }

    private void initHeaderView(View headerView, final ClassEntity.Assistant assistant) {
        mTitle = (TextView) headerView.findViewById(R.id.class_assistant_tv_name);
        mTvPhoto = (TextView) headerView.findViewById(R.id.class_assistant_tv_photo);
        mIvPhoto = (CircleImageView) headerView.findViewById(R.id.class_assistant_iv_photo);
        mCall = (RelativeLayout) headerView.findViewById(R.id.class_assistant_rl_call);

        if (assistant != null) {
            String name = assistant.getName().trim();
            String school_name = assistant.getSchool_name();
            mTitle.setText(String.format("%s助教%s", school_name, name));
            String avatar = assistant.getAvatar();
            if (TextUtils.isEmpty(avatar)) {
                if (name.length() > 0) {
                    mTvPhoto.setText(name.substring(name.length() - 1));
                }
                mTvPhoto.setVisibility(View.VISIBLE);
                mIvPhoto.setVisibility(View.INVISIBLE);
            } else {
                mTvPhoto.setVisibility(View.INVISIBLE);
                mIvPhoto.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(avatar, mIvPhoto);
            }

            mCall.setOnClickListener(v -> {
                MobclickAgent.onEvent(ClassActivity.this, "jhyx_tap_assistant");
                mCallIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + assistant.getPhone_number()));
                startActivity(mCallIntent);
            });
        }
    }

    private void initData() {
        mIsFirstRefresh = true;
        try {
            mClassDataCache = Reservoir.get(Constants.CLASS_DATA + mClassId, ClassEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "mClassDataCache: " + mClassDataCache);
        mIsFirstLoading = mClassDataCache == null;
        if (NetworkUtils.isNetworkAvaialble(this)) {
            getClassDetail();
        } else {
            if (mClassDataCache != null) {
                refreshGridView(mClassDataCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    /**
     * 初始化刷新头
     */
    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(false);
        BGAJHYXRefreshViewHolder bgaNormalRefreshViewHolder = new BGAJHYXRefreshViewHolder(this, true);
        bgaNormalRefreshViewHolder.setPullDownImageResource(R.mipmap.loading_logo_00025);
        bgaNormalRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshViewBackgroundColorRes(R.color.activity_bg_gray);
        bgaNormalRefreshViewHolder.setSpringDistanceScale(0);
        mRefreshLayout.setRefreshViewHolder(bgaNormalRefreshViewHolder);
    }

    private void getClassDetail() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
        mClassDetailSubscriber = new ClassDetailSubscriber(this);
        mClassDetailSubscription = HttpManager.getClassDetail(token, mClassId).subscribe(mClassDetailSubscriber);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mIsRefreshing = true;
        if (NetworkUtils.isNetworkAvaialble(this)) {
            getClassDetail();
        } else {
            mIsRefreshing = false;
            mRefreshLayout.endRefreshing();
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mClassStudentsDatas == null || mClassStudentsDatas.size() < 1) {
            return;
        }
        LogHelper.d(TAG, "onItemClick: " + position);
        MobclickAgent.onEvent(ClassActivity.this, "jhyx_tap_student");
        int kidID = mClassStudentsDatas.get(position).getId();
        String url = FeatureConfig.API_HOST_NAME + "teacher/courses/" + mClassId + "/kids/" + kidID;
        Intent intent = new Intent(this, NormalWebViewActivity.class);
        intent.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.title_student_infor));
        intent.putExtra(Constants.WEBVIEW_URL, url);
        startActivity(intent);
    }

    class ClassDetailSubscriber extends LoadingSubscriber<ClassEntity> {

        private ClassEntity mClassEntity;

        public ClassDetailSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (mIsFirstRefresh && mClassDataCache != null) {
                refreshGridView(mClassDataCache);
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
                mRefreshLayout.endRefreshing();
                mIsRefreshing = false;
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
                    if (mIsFirstRefresh && mClassDataCache != null) {
                        refreshGridView(mClassDataCache);
                    }
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, mIsFirstLoading);
            }
        }

        @Override
        public void onNext(ClassEntity classEntity) {
            LogHelper.d(TAG, "classEntity: " + classEntity);
            if (classEntity != null) {
                mLoadState = Constants.LOADING_STATE_SUCCESS;
                mIsFirstLoading = false;
                try {
                    Reservoir.put(Constants.CLASS_DATA + mClassId, classEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mLoadState = Constants.LOADING_STATE_FAIL;
            }
            mClassEntity = classEntity;
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            refreshGridView(mClassEntity);
            refreshLoadingState(mLoadState, mIsFirstLoading);
        }
    }

    private void refreshGridView(ClassEntity data) {
        if (data == null) {
            return;
        }
        mIsFirstRefresh = false;
        ClassEntity.Assistant assistant = data.getAssistant();
        if (assistant != null) {
            mAssistantView.setVisibility(View.VISIBLE);
            initHeaderView(mAssistantView, assistant);
        } else {
            mAssistantView.setVisibility(View.GONE);
        }
        ArrayList<ClassEntity.Kid> kids = data.getKids();
        if (kids != null && kids.size() > 0) {
            LogHelper.d(TAG, "kids: " + kids);
            mEmptyPage.setVisibility(View.GONE);
            mClassStudentsDatas.clear();
            mClassStudentsDatas.addAll(kids);
        } else {
            mClassStudentsDatas.clear();
            mEmptyPage.setVisibility(View.VISIBLE);
        }
        mClassStudentAdapter.notifyDataSetChanged();
        mRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClassDetailSubscriber != null) {
            mClassDetailSubscriber.onCancle();
        }
        if (mClassDetailSubscription != null && !mClassDetailSubscription.isUnsubscribed()) {
            mClassDetailSubscription.unsubscribe();
        }
    }
}
