package com.oceansky.teacher.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.activities.CreatHomeworkActivity;
import com.oceansky.teacher.activities.HomeWorkReportActivity;
import com.oceansky.teacher.activities.KnowledgePointSelectActivity;
import com.oceansky.teacher.activities.PreviewNetDataActivity;
import com.oceansky.teacher.activities.TokenInvalidDialogActivity;
import com.oceansky.teacher.adapter.HomeworkAdapter;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.customviews.MsgCenterBGARefreshViewHolder;
import com.oceansky.teacher.entity.CourseBeanForAdapter;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.CreatHomeworkEvent;
import com.oceansky.teacher.event.LoginSuccessEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.ReuseHomeworkEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.HomeworkListEntity;
import com.oceansky.teacher.network.response.HomeworkReuseEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import rx.Subscription;

public class HomeworkFragment extends BaseLazyFragment implements BGARefreshLayout.BGARefreshLayoutDelegate,
        SlideAndDragListView.OnMenuItemClickListener, SlideAndDragListView.OnListItemClickListener {
    private static final String TAG  = HomeworkFragment.class.getSimpleName();
    public static final  int    SIZE = 20;

    private FrameLayout          mLoadingLayout;
    private AnimationDrawable    mLoadingAnimation;
    private SlideAndDragListView mListView;
    private Context              mContext;
    private HomeworkAdapter      mHomeworkAdapter;
    private ImageView            mLoadingImg;
    private View                 mEmptyPage;
    private BGARefreshLayout     mRefreshLayout;
    private CustomProgressDialog mDialog;
    private Menu                 mMenu;
    private View                 mFooterView;

    private ArrayList<HomeworkListEntity.HomeworkData> mHomeworkList;
    private ArrayList<HomeworkListEntity.HomeworkData> mHomeworkDatas;
    private ArrayList<HomeworkListEntity.HomeworkData> mHomeworkListCache;
    private HomeworkReuseEntity                        homeworkReuseDatas;

    private int mHomeworkStatus;
    private int mDeletePosition;
    private int mOffset;

    private GetHomeworkListSubscriber mGetHomeworkListSubscriber;
    private DeleteHomeworkSubscriber  mDeleteHomeworkSubscriber;
    private ReuseHomeworkSubscriber   mReuseHomeworkSubscriber;
    private Subscription              mGetHomeworkSubscription;
    private Subscription              mDeleteHomeworkSubscription;
    private Subscription              mReuseHomeworkSubscription;

    private boolean mCanLoadingMore;
    private boolean mIsLoadingMore;
    private boolean mIsFirstLoading;//是否首次加载数据
    private boolean mIsFirstRefreshing = true;//是否首次刷新
    private boolean mIsEmpty;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_homework);
        super.onCreateViewLazy(savedInstanceState);
        mContext = getActivity();
        Bundle arguments = getArguments();
        if (arguments != null) {
            mHomeworkStatus = arguments.getInt(Constants.HOMEWORK_STATE);
            LogHelper.d(TAG, "HomeworkStatus: " + mHomeworkStatus);
        }
        initView();
        initData();
        RxBus.getInstance().register(this);
    }

    @Override
    void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            refreshDatas();
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    private void initView() {
        mLoadingLayout = (FrameLayout) findViewById(R.id.loading_layout);
        mLoadingImg = (ImageView) findViewById(R.id.loading);
        mListView = (SlideAndDragListView) findViewById(R.id.homework_lv);
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.homework_layout_refresh);
        initEmptyPage();
        initRefreshLayout();
        initReservoir();
        //加载动画
        mLoadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImg.getDrawable();
        initMenu();
        mListView.setMenu(mMenu);
        mHomeworkDatas = new ArrayList<>();
        mHomeworkAdapter = new HomeworkAdapter(mContext, mHomeworkDatas);
        mListView.setOnSlideListener(new SlideAndDragListView.OnSlideListener() {
            @Override
            public void onSlideOpen(View view, View parentView, int position, int direction) {
                MobclickAgent.onEvent(mContext, Constants.LEFT_SWIP_HOMEWORK_ITEM);
            }

            @Override
            public void onSlideClose(View view, View parentView, int position, int direction) {

            }
        });
        mListView.setAdapter(mHomeworkAdapter);
        mListView.setOnMenuItemClickListener(this);
        mListView.setOnListItemClickListener(this);
        mDialog = CustomProgressDialog.createDialog(mContext);
    }

    public void initMenu() {
        mMenu = new Menu(true, false, 0);
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.width_homework_list_delete))
                .setBackground(new ColorDrawable(0xfffc4b4b))
                .setText(getString(R.string.btn_delete))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setTextColor(Color.WHITE)
                .setTextSize(16)
                .build());
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_DONE) {
            mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.width_homework_list_reuse))
                    .setBackground(new ColorDrawable(0xffcccccc))
                    .setText(getString(R.string.btn_reuse))
                    .setDirection(MenuItem.DIRECTION_RIGHT)
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .build());
        }
    }

    /**
     * 初始化刷新头
     */
    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(true);
        MsgCenterBGARefreshViewHolder msgRefreshViewHolder = new MsgCenterBGARefreshViewHolder(getActivity(), true);
        msgRefreshViewHolder.setPullDownImageResource(R.drawable.loading_animation);
        msgRefreshViewHolder.setRefreshingAnimResId(R.drawable.loading_animation);
        msgRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        msgRefreshViewHolder.setLoadingMoreText(getString(R.string.refresh_load_more));
        msgRefreshViewHolder.setLoadMoreBackgroundDrawableRes(R.drawable.loading_animation);
        msgRefreshViewHolder.setSpringDistanceScale(0);
        mRefreshLayout.setRefreshViewHolder(msgRefreshViewHolder);
        mFooterView = View.inflate(mContext, R.layout.layout_list_footer_nomore_data, null);
    }

    private void initEmptyPage() {
        mEmptyPage = LayoutInflater.from(mContext).inflate(R.layout.layout_empty_page, mRefreshLayout, false);
        ImageView emptyIcon = (ImageView) mEmptyPage.findViewById(R.id.empty_iv_icon);
        emptyIcon.setImageResource(R.mipmap.icon_homework_list_empty);
        TextView emptyMsg = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_PENDING) {
            emptyMsg.setText(Html.fromHtml(getActivity().getString(R.string.empty_msg_homework)));
            emptyMsg.setOnClickListener(v -> startActivity(new Intent(mContext, CreatHomeworkActivity.class)));
        } else {
            emptyMsg.setText(R.string.empty_msg_homework_done);
        }
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
        try {
            if (Reservoir.contains(Constants.HOMEWORK_LIST + mHomeworkStatus)) {
                mHomeworkListCache = Reservoir.get(Constants.COURSE_LIST + mHomeworkStatus,
                        new TypeToken<ArrayList<CourseBeanForAdapter>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "CourseListCache: " + mHomeworkListCache);
        mIsFirstLoading = mHomeworkListCache == null;
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            refreshDatas();
        } else {
            if (mHomeworkListCache != null) {
                refreshList(mHomeworkListCache);
            }
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            mIsLoadingMore = false;
            refreshDatas();
        } else {
            mRefreshLayout.endRefreshing();
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, mIsFirstLoading);
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        if (mCanLoadingMore) {
            if (NetworkUtils.isNetworkAvaialble(mContext)) {
                mIsLoadingMore = true;
                LoadingMoreDatas();
            } else {
                mRefreshLayout.endLoadingMore();
                refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
            }
            return true;
        } else {
            return false;
        }
    }

    public void refreshDatas() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
        String teacherId = SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_TEACHER_ID);
        mGetHomeworkListSubscriber = new GetHomeworkListSubscriber(mContext);
        mGetHomeworkSubscription = HttpManager.getHomeworkList(token, teacherId, mHomeworkStatus, 0, SIZE)
                .subscribe(mGetHomeworkListSubscriber);
    }

    public void LoadingMoreDatas() {
        final String token =
                "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
        String teacherId = SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_TEACHER_ID);
        mGetHomeworkListSubscriber = new GetHomeworkListSubscriber(mContext);
        mGetHomeworkSubscription = HttpManager.getHomeworkList(token, teacherId, mHomeworkStatus, mOffset, SIZE)
                .subscribe(mGetHomeworkListSubscriber);
    }

    private class GetHomeworkListSubscriber extends LoadingSubscriber<HomeworkListEntity> {

        public GetHomeworkListSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (mIsLoadingMore) {
                mRefreshLayout.endLoadingMore();
                refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
            } else {
                if (mIsFirstLoading) {
                    refreshList(mHomeworkListCache);
                }
                refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, mIsFirstLoading);
            }
        }

        @Override
        protected void showLoading() {
            if (!mIsLoadingMore) {
                if (mIsFirstRefreshing) {
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    mLoadingAnimation.start();
                }
            }
        }

        @Override
        protected void dismissLoading() {
            if (!mIsLoadingMore) {
                if (mIsFirstRefreshing) {
                    mLoadingLayout.setVisibility(View.INVISIBLE);
                    mLoadingAnimation.stop();
                } else {
                    mRefreshLayout.setVisibility(View.VISIBLE);
                    mRefreshLayout.endRefreshing();
                }
            }
        }

        @Override
        protected void handleError(Throwable e) {
            LogHelper.d(TAG, "handleError: " + e.getMessage());
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    Intent intent = new Intent(mContext, TokenInvalidDialogActivity.class);
                    intent.putExtra(Constants.CLASS_NAME, HomeworkFragment.class.getSimpleName());
                    startActivity(intent);
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
            if (mHomeworkListCache != null) {
                refreshList(mHomeworkListCache);
            }
            refreshLoadingState(state, mIsFirstLoading);
        }

        @Override
        public void onNext(HomeworkListEntity homeworkListEntity) {
            super.onNext(homeworkListEntity);
            mHomeworkList = homeworkListEntity.getHomeworkList();
            LogHelper.d(TAG, "HomeworkList: " + mHomeworkList);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            if (mIsLoadingMore) {
                mRefreshLayout.endLoadingMore();
                if (mHomeworkList.size() < SIZE) {
                    //没有更多了
                    mRefreshLayout.setIsShowLoadingMoreView(false);
                    mCanLoadingMore = false;
                    addFooterView();
                } else {
                    mOffset += SIZE;
                    mCanLoadingMore = true;
                }
                mHomeworkDatas.addAll(mHomeworkList);
                mHomeworkAdapter.notifyDataSetChanged();
            } else {
                if (mHomeworkList.size() < SIZE) {
                    mRefreshLayout.setIsShowLoadingMoreView(false);
                    mOffset = mHomeworkList.size();
                    mCanLoadingMore = false;
                } else {
                    mRefreshLayout.setIsShowLoadingMoreView(true);
                    mOffset = SIZE;
                    mCanLoadingMore = true;
                    removeFooterView();
                }
                try {
                    Reservoir.put(Constants.COURSE_LIST + mHomeworkStatus, mHomeworkList);
                    refreshList(mHomeworkList);
                    refreshLoadingState(Constants.LOADING_STATE_SUCCESS, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mIsFirstLoading = false;
            }
        }
    }

    private void removeFooterView() {
        if (android.os.Build.VERSION.SDK_INT > 18 && mListView.getFooterViewsCount() > 0) {
            mListView.removeFooterView(mFooterView);
        }
    }

    private void addFooterView() {
        if (android.os.Build.VERSION.SDK_INT > 18 && mListView.getFooterViewsCount() < 1) {
            mListView.addFooterView(mFooterView);
        }
    }

    private class DeleteHomeworkSubscriber extends LoadingSubscriber<SimpleResponse> {

        public DeleteHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            ToastUtil.showToastBottom(getActivity(), R.string.toast_error_time_out, Toast.LENGTH_SHORT);
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            super.onNext(simpleResponse);
            if (simpleResponse == null || simpleResponse.getCode() != 200) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
        }

        @Override
        protected void showLoading() {
            mDialog.show();
        }

        @Override
        protected void dismissLoading() {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            ToastUtil.showToastBottom(getActivity(), R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            LogHelper.d(TAG, "DeletePosition: " + mDeletePosition);
            LogHelper.d(TAG, "HeaderViewsCount: " + mListView.getHeaderViewsCount());
            mHomeworkDatas.remove(mDeletePosition - mListView.getHeaderViewsCount());
            mHomeworkAdapter.notifyDataSetChanged();
            if (mHomeworkDatas.size() == 0) {
                addEmptyPage();
            }
            if (mHomeworkDatas.size() < SIZE && mListView.getFooterViewsCount() > 0) {
                removeFooterView();
            }
        }
    }

    private class ReuseHomeworkSubscriber extends LoadingSubscriber<HomeworkReuseEntity> {

        public ReuseHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
        }

        @Override
        protected void showLoading() {
            mDialog.show();
        }

        @Override
        protected void dismissLoading() {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, false);
            }
        }

        @Override
        public void onNext(HomeworkReuseEntity homeworkReuseEntity) {
            LogHelper.d(TAG, "HomeworkReuseEntity: " + homeworkReuseEntity);
            homeworkReuseDatas = homeworkReuseEntity;
        }

        @Override
        public void onCompleted() {
            LogHelper.d(TAG, "onCompleted");
            super.onCompleted();
            RxBus.getInstance().post(new ReuseHomeworkEvent());
            HomeworkReuseEntity.HomeworkData homeworkData = homeworkReuseDatas.getBase_info();
            Intent intent = new Intent(mContext, PreviewNetDataActivity.class);
            intent.putExtra(Constants.IS_REUSE_HOMEWORK, true);
            if (homeworkData != null) {
                int homewrokId = homeworkData.getId();
                String homeworkTilte = homeworkData.getTitle();
                MyHashSet<Integer> textbookIds = homeworkData.getTextbook_ids();
                MyHashSet<Integer> chapterIds = homeworkData.getKnowledge_chapter_ids();
                MyHashSet<Integer> sectionIds = homeworkData.getKnowledge_section_ids();
                MyHashSet<Integer> detailIds = homeworkData.getKnowledge_detail_ids();
                MyHashSet<String> questionIds = homeworkData.getQuestion_ids();
                int gradeId = homeworkData.getGrade_id();
                int lessonId = homeworkData.getLesson_id();
                intent.putExtra(Constants.HOMEWORK_ID, homewrokId);
                intent.putExtra(Constants.HOMEWORK_TITLE, homeworkTilte);
                intent.putExtra(Constants.GRADE_ID, gradeId);
                intent.putExtra(Constants.LESSON_ID, lessonId);
                intent.putExtra(Constants.TEXTBOOK_ID_SET, textbookIds);
                intent.putExtra(Constants.CHAPTER_ID_SET, chapterIds);
                intent.putExtra(Constants.SECTION_ID_SET, sectionIds);
                intent.putExtra(Constants.DETAIL_ID_SET, detailIds);
                //intent中无法传递LinkedHashMap,需要保证选择题目的顺序,所以改为本地存储
                //intent.putExtra(Constants.QUESTION_ID_SET, questionIds);
                try {
                    Reservoir.put(Constants.QUESTION_ID_SET, questionIds);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ArrayList<HomeworkReuseEntity.QuestionData> questions = homeworkReuseDatas.getQuestions();
            if (questions != null) {
                LinkedHashMap<String, String> htmlMap = new LinkedHashMap<>();
                for (HomeworkReuseEntity.QuestionData questionData : questions) {
                    htmlMap.put(questionData.getQuestionId(), questionData.getQuestionHtml());
                }
                //intent中无法传递LinkedHashMap,需要保证选择题目的顺序,所以改为本地存储
                //intent.putExtra(Constants.HOMEWORK_HTML_MAP, mHtmlMap);
                try {
                    Reservoir.put(Constants.HOMEWORK_HTML_MAP, htmlMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            startActivity(intent);
        }
    }

    private void refreshList(ArrayList<HomeworkListEntity.HomeworkData> homeworkList) {
        if (homeworkList == null) {
            LogHelper.d(TAG, "homeworkList = null");
            return;
        }
        mHomeworkDatas.clear();
        mHomeworkDatas.addAll(homeworkList);
        if (mHomeworkDatas.size() < 1) {
            //没有对应状态的课程
            addEmptyPage();
        } else {
            removeEmptyPage();
        }
        mHomeworkAdapter.notifyDataSetChanged();
        mRefreshLayout.setVisibility(View.VISIBLE);
        mIsFirstRefreshing = false;
    }

    private void removeEmptyPage() {
        if (mIsEmpty) {
            mRefreshLayout.removeView(mEmptyPage);
            mListView.setVisibility(View.VISIBLE);
            mIsEmpty = false;
        }
    }

    private void addEmptyPage() {
        if (!mIsEmpty) {
            mRefreshLayout.addView(mEmptyPage);
            mListView.setVisibility(View.GONE);
            mIsEmpty = true;
        }
    }

    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
        LogHelper.d(TAG, "onMenuItemClick   " + itemPosition + "   " + buttonPosition + "   " + direction);
        switch (buttonPosition) {
            case 0:
                DeleteHomework(itemPosition);
                MobclickAgent.onEvent(mContext, Constants.LEFTSWIP_DELETE_HOMEWORK_TOUCHED);
                return Menu.ITEM_SCROLL_BACK;
            case 1:
                //重新使用
                int homeworkId = mHomeworkDatas.get(itemPosition).getId();
                MobclickAgent.onEvent(mContext, Constants.LEFTSWIP_REUSE_HOMEWORK_TOUCHED);
                reuseHomework(homeworkId);
                return Menu.ITEM_SCROLL_BACK;
        }
        return Menu.ITEM_NOTHING;
    }

    private void gotoPreview(HomeworkListEntity.HomeworkData homeworkData) {
        int homewrokId = homeworkData.getId();
        String homeworkTilte = homeworkData.getTitle();
        MyHashSet<Integer> textbookIds = homeworkData.getTextbook_ids();
        MyHashSet<Integer> chapterIds = homeworkData.getKnowledge_chapter_ids();
        MyHashSet<Integer> sectionIds = homeworkData.getKnowledge_section_ids();
        MyHashSet<Integer> detailIds = homeworkData.getKnowledge_detail_ids();
        MyHashSet<String> questionIds = homeworkData.getQuestion_ids();
        int gradeId = homeworkData.getGrade_id();
        int lessonId = homeworkData.getLesson_id();
        Intent intent = new Intent(mContext, PreviewNetDataActivity.class);
        intent.putExtra(Constants.HOMEWORK_ID, homewrokId);
        intent.putExtra(Constants.HOMEWORK_TITLE, homeworkTilte);
        intent.putExtra(Constants.GRADE_ID, gradeId);
        intent.putExtra(Constants.LESSON_ID, lessonId);
        intent.putExtra(Constants.TEXTBOOK_ID_SET, textbookIds);
        intent.putExtra(Constants.CHAPTER_ID_SET, chapterIds);
        intent.putExtra(Constants.SECTION_ID_SET, sectionIds);
        intent.putExtra(Constants.DETAIL_ID_SET, detailIds);
        try {
            Reservoir.put(Constants.QUESTION_ID_SET, questionIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }

    public void DeleteHomework(int position) {
        LogHelper.d(TAG, "onItemDelete");
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            mDeletePosition = position;
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            mDeleteHomeworkSubscriber = new DeleteHomeworkSubscriber(mContext);
            HomeworkListEntity.HomeworkData deleteHomeworkData = mHomeworkDatas.get(position);
            mDeleteHomeworkSubscription = HttpManager.deleteHomework(token, deleteHomeworkData.getId())
                    .subscribe(mDeleteHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    public void reuseHomework(int homeworkId) {
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            mReuseHomeworkSubscriber = new ReuseHomeworkSubscriber(mContext);
            mReuseHomeworkSubscription = HttpManager.reuseHomework(token, homeworkId).subscribe(mReuseHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    @Override
    public void onListItemClick(View v, int position) {
        LogHelper.d(TAG, "onItemClick: " + position);
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_PENDING) {
            HomeworkListEntity.HomeworkData homeworkData = mHomeworkDatas.get(position);
            if (homeworkData != null) {
                MyHashSet<String> questionIds = homeworkData.getQuestion_ids();
                if (questionIds.size() == 0) {
                    //还未选择题目，跳转到知识点选择
                    int homewrokId = homeworkData.getId();
                    String homeworkTilte = homeworkData.getTitle();
                    int gradeId = homeworkData.getGrade_id();
                    int lessonId = homeworkData.getLesson_id();
                    Intent intent = new Intent(mContext, KnowledgePointSelectActivity.class);
                    intent.putExtra(Constants.HOMEWORK_ID, homewrokId);
                    intent.putExtra(Constants.HOMEWORK_TITLE, homeworkTilte);
                    intent.putExtra(Constants.GRADE_ID, gradeId);
                    intent.putExtra(Constants.LESSON_ID, lessonId);
                    startActivity(intent);
                } else {
                    gotoPreview(homeworkData);
                }
            }
        } else {
            HomeworkListEntity.HomeworkData homeworkData = mHomeworkDatas.get(position);
            LogHelper.d(TAG, "homeworkData: " + homeworkData);
            Intent intent = new Intent(mContext, HomeWorkReportActivity.class);
            intent.putExtra(Constants.HOMEWORK_ID, homeworkData.getId());
            intent.putExtra(Constants.COURSE_ID, homeworkData.getCourse_id());
            intent.putExtra(Constants.WEBVIEW_TITLE, homeworkData.getTitle() + getString(R.string.homework_report));
            String url = FeatureConfig.API_HOST_NAME + "h5/homework/" + homeworkData.getId() + "/course/" + homeworkData.getCourse_id() + "/homework_report";
            intent.putExtra(Constants.WEBVIEW_URL, url);
            startActivity(intent);
        }
    }

    @Subscribe
    public void creatHomework(CreatHomeworkEvent creatHomeworkEvent) {
        LogHelper.d(TAG, "creatHomework");
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_PENDING) {
            mRefreshLayout.beginRefreshing();
        }
    }

    @Subscribe
    public void refreshUnassignedHomeworkList(RefreshUnassignedHomeworkListEvent refreshUnassignedHomeworkListEvent) {
        LogHelper.d(TAG, "refreshUnassignedHomeworkList");
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_PENDING) {
            mRefreshLayout.beginRefreshing();
        }
    }

    @Subscribe
    public void assignHomework(AssignHomeworkEvent assignHomeworkEvent) {
        mRefreshLayout.beginRefreshing();
    }

    @Subscribe
    public void reuseHomework(ReuseHomeworkEvent reuseHomeworkEvent) {
        if (mHomeworkStatus == Constants.HOMEWORK_STATE_PENDING) {
            mRefreshLayout.beginRefreshing();
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
        if (mGetHomeworkListSubscriber != null) {
            mGetHomeworkListSubscriber.onCancle();
        }
        if (mGetHomeworkSubscription != null && !mGetHomeworkSubscription.isUnsubscribed()) {
            mGetHomeworkSubscription.unsubscribe();
        }
        if (mDeleteHomeworkSubscriber != null) {
            mDeleteHomeworkSubscriber.onCancle();
        }
        if (mDeleteHomeworkSubscription != null && !mDeleteHomeworkSubscription.isUnsubscribed()) {
            mDeleteHomeworkSubscription.unsubscribe();
        }
        if (mReuseHomeworkSubscriber != null) {
            mReuseHomeworkSubscriber.onCancle();
        }
        if (mReuseHomeworkSubscription != null && !mReuseHomeworkSubscription.isUnsubscribed()) {
            mReuseHomeworkSubscription.unsubscribe();
        }
        RxBus.getInstance().unregister(this);
    }
}
