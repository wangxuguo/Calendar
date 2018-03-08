package com.oceansky.example.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.example.R;
import com.oceansky.example.constant.Constants;
import com.oceansky.example.customviews.BGAHomeworkSelectRefreshViewHolder;
import com.oceansky.example.event.ChangeHomeworkTypeEvent;
import com.oceansky.example.event.ChangeSetEvent;
import com.oceansky.example.event.ChangeknowledgePointEvent;
import com.oceansky.example.event.DeleteHomeworkEvent;
import com.oceansky.example.event.HomeworkOutOfLimitEvent;
import com.oceansky.example.event.RxBus;
import com.oceansky.example.event.SelectHomeworkEvent;
import com.oceansky.example.network.http.ApiException;
import com.oceansky.example.network.http.HttpManager;
import com.oceansky.example.network.response.HomeworkEntity;
import com.oceansky.example.network.subscribers.LoadingSubscriber;
import com.oceansky.example.utils.LogHelper;
import com.oceansky.example.utils.MyHashSet;
import com.oceansky.example.utils.NetworkUtils;
import com.oceansky.example.utils.SecurePreferences;
import com.oceansky.example.utils.SharePreferenceUtils;
import com.oceansky.example.utils.StringUtils;
import com.oceansky.example.utils.ToastUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import rx.Subscription;

/**
 * User: dengfa
 * Date: 16/8/18
 * Tel:  18500234565
 */
public class HomeworkSelectWebviewFragment extends BaseLazyFragment
        implements BGARefreshLayout.BGARefreshLayoutDelegate {
    private static final String TAG = HomeworkSelectWebviewFragment.class.getSimpleName();

    public static final int HOMEWORK_SIZE     = 20;
    public static final int HOMEWORK_MAX_SIZE = 100;

    private FragmentActivity mContext;
    private WebView          mWebView;
    private NestedScrollView mNestedScrollView;
    private BGARefreshLayout mRefreshLayout;
    private RelativeLayout   mOffLineLayout;
    private RelativeLayout   mEmptyLayout;
    private ImageView        mStateImg;
    private TextView         mStateDesc;
    private TextView         mStateDescSub;

    private int                           mLoadingState;
    private int                           mHomeworkDifficulty;
    private int                           mHomeworkType;
    private int                           mHomeworkId;
    private int                           mLessonId;
    private MyHashSet<String>             mQuestionIdSet;
    private String                        mKnowledgeDetailId;
    private String                        mKnowledgePoint;
    private String                        mOffset;//作业List的最后一个的id
    private LinkedHashMap<String, String> mQustionHtmlMap;
    private boolean                       misLoading;
    private boolean                       mIsChangeSet;
    private boolean                       mIsLoaddataSuccess;//是否成功加载过此知识点下的数据
    private boolean                       isPageLoaded;

    private SelectHomeworkSubscriber mSelectHomeworkSubscriber;
    private Subscription             mSelectHomeworkSubscription;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_homework_select_webview);
        super.onCreateViewLazy(savedInstanceState);
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    @Override
    protected void onResumeLazy() {
        super.onResumeLazy();
        LogHelper.d(TAG, "HomeworkSelectWebviewFragment onResumeLazy");
        try {
            if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                mQuestionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId,
                        new TypeToken<MyHashSet<String>>() {
                        }.getType());
                LogHelper.d(TAG, "QuestionIdSet: " + mQuestionIdSet.toString());
                mWebView.loadUrl("javascript:selected('" + mQuestionIdSet.toString() + "')");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        mKnowledgePoint = SharePreferenceUtils.getStringPref(mContext, Constants.KNOWLEDGE_POINT, "");
        mKnowledgeDetailId = SharePreferenceUtils.getStringPref(mContext, Constants.KNOWLEDGE_DETAIL_ID, "");
        LogHelper.d(TAG, "KnowledgePoint: " + mKnowledgePoint);
        LogHelper.d(TAG, "KnowledgeDetailId: " + mKnowledgeDetailId);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mHomeworkDifficulty = arguments.getInt(Constants.HOMEWORK_DIFFICULTY);
            mLessonId = arguments.getInt(Constants.LESSON_ID);
            mHomeworkId = arguments.getInt(Constants.HOMEWORK_ID);
            mQuestionIdSet = (MyHashSet<String>) arguments.getSerializable(Constants.QUESTION_ID_SET);
            mWebView.loadUrl("javascript:selected('" + mQuestionIdSet.toString() + "')");
            LogHelper.d(TAG, "HomeworkDifficulty: " + mHomeworkDifficulty);
            LogHelper.d(TAG, "LessonId: " + mLessonId);
            LogHelper.d(TAG, "HomeworkId: " + mHomeworkId);
            LogHelper.d(TAG, "QuestionIdSet: " + mQuestionIdSet);
        }
        mHomeworkType = SharePreferenceUtils.getIntPref(mContext, Constants.HOMEWORK_TYPE, Constants.HOMEWORK_TYPE_CHOICE);
        LogHelper.d(TAG, "HomeworkType: " + mHomeworkType);
        if (mHomeworkType != Constants.HOMEWORK_TYPE_CHOICE) {
            mOffLineLayout.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.INVISIBLE);
        } else {
            selectHomework(mLessonId, mKnowledgeDetailId, mKnowledgePoint, mHomeworkType, mHomeworkDifficulty,
                    mOffset, HOMEWORK_SIZE);
        }
    }

    private void initView() {
        mContext = getActivity();
        mOffLineLayout = (RelativeLayout) findViewById(R.id.not_online_layout);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.error_layout);
        ((ImageView) mOffLineLayout.findViewById(R.id.error_img)).setImageResource(R.mipmap.icon_homework_type_none);
        ((TextView) mOffLineLayout.findViewById(R.id.error_desc)).setText(R.string.empty_msg_homework_select);
        initStatePageLayoutParams();
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.homework_select_webview_refresh);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.homework_sv);
        mStateImg = (ImageView) mEmptyLayout.findViewById(R.id.error_img);
        mStateDesc = (TextView) mEmptyLayout.findViewById(R.id.error_desc);
        mStateDescSub = (TextView) mEmptyLayout.findViewById(R.id.error_desc_sub);
        initRefreshLayout();
        mWebView = (WebView) findViewById(R.id.webview_homework_select);
        WebSettings webViewSettings = mWebView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDefaultTextEncodingName("UTF-8");
        mWebView.addJavascriptInterface(new JsInteration(), "controller");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isPageLoaded = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogHelper.d(TAG, "onPageFinished");
                super.onPageFinished(view, url);
                if (!isPageLoaded) {
                    isPageLoaded = true;
                    initSelectedQuestion();
                    mWebView.loadUrl("javascript:getHeight()");
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                LogHelper.d(TAG, "-----------------onPageCommitVisible----------------------");
                if (!isPageLoaded) {
                    isPageLoaded = true;
                    initSelectedQuestion();
                    mWebView.loadUrl("javascript:getHeight()");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                LogHelper.d(TAG, "-----------------onReceivedError----------------------");
                refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                isPageLoaded = true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                LogHelper.d(TAG, "-----------------onReceivedSslError----------------------");
                refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                isPageLoaded = true;
            }
        });
    }

    private void initStatePageLayoutParams() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int titleBarHeight = (int) getResources().getDimension(R.dimen.title_bar_height);
        int typeTabHeight = (int) getResources().getDimension(R.dimen.height_homework_type_tab);
        int difficultyTabHeight = (int) getResources().getDimension(R.dimen.height_homework_difficulty_tab);
        int bottomTabHeight = (int) getResources().getDimension(R.dimen.height_homework_select_bottom_tab);
        //获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        LogHelper.d(TAG, "statusBarHeight:" + statusBarHeight);
        int contentViewHeight = screenHeight - titleBarHeight - typeTabHeight - difficultyTabHeight - statusBarHeight - bottomTabHeight;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentViewHeight);
        mEmptyLayout.setLayoutParams(params);
        mOffLineLayout.setLayoutParams(params);
    }

    /**
     * 初始化刷新头
     */
    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(false);
        BGAHomeworkSelectRefreshViewHolder bgaNormalRefreshViewHolder = new BGAHomeworkSelectRefreshViewHolder(mContext, true);
        bgaNormalRefreshViewHolder.setPullDownImageResource(R.mipmap.loading_logo_00025);
        bgaNormalRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshViewBackgroundColorRes(R.color.activity_bg_gray);
        bgaNormalRefreshViewHolder.setSpringDistanceScale(0);
        mRefreshLayout.setRefreshViewHolder(bgaNormalRefreshViewHolder);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        misLoading = true;
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    public class JsInteration {
        @JavascriptInterface
        public void addQuestion(String id) {
            LogHelper.d(TAG, "addQuestion: " + id);
            LogHelper.d(TAG, "QuestionIdSet Size: " + mQuestionIdSet.size());
            if (mQuestionIdSet.size() < HOMEWORK_MAX_SIZE) {
                mQuestionIdSet.add(id);
                RxBus.getInstance().post(new SelectHomeworkEvent(id, mQustionHtmlMap.get(id)));
            } else {
                RxBus.getInstance().post(new HomeworkOutOfLimitEvent(id));
            }
        }

        @JavascriptInterface
        public void deleteQuestion(String id) {
            LogHelper.d(TAG, "deleteQuestion: " + id);
            mQuestionIdSet.remove(id);
            RxBus.getInstance().post(new DeleteHomeworkEvent(id));
        }

        @JavascriptInterface
        public void resize(final float height) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded() && getActivity() != null) {
                        mWebView.setLayoutParams(new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels,
                                (int) (height * getResources().getDisplayMetrics().density)));
                    }
                }
            });
        }
    }

    private void selectHomework(int lessonId, String detailIds, String knowledgePoint, int type,
                                int difficuty, String offset, int size) {
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            mSelectHomeworkSubscriber = new SelectHomeworkSubscriber(mContext);
            mSelectHomeworkSubscription =
                    HttpManager.selectHomework(token, lessonId, detailIds, knowledgePoint, type, difficuty, offset, size)
                            .subscribe(mSelectHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, !mIsChangeSet);
        }
    }

    class SelectHomeworkSubscriber extends LoadingSubscriber<ArrayList<HomeworkEntity>> {
        public SelectHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_FAIL, !mIsChangeSet);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, !mIsChangeSet);
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, !mIsChangeSet);
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            mIsLoaddataSuccess = true;
        }

        @Override
        public void onNext(ArrayList<HomeworkEntity> homeworkList) {
            LogHelper.d(TAG, "homeworkList: " + homeworkList);
            if (homeworkList.size() > 0) {
                HomeworkEntity lastHomeworkEntity = homeworkList.get(homeworkList.size() - 1);
                mOffset = lastHomeworkEntity.getQuestion_id();
                webviewLoadHtml(homeworkList);
                refreshLoadingState(Constants.LOADING_STATE_SUCCESS, true);
                mLoadingState = Constants.LOADING_STATE_SUCCESS;
                if (mWebView.getVisibility() != View.VISIBLE) {
                    mWebView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mIsLoaddataSuccess && mLoadingState != Constants.LOADING_STATE_HOMEWORK_EMPTY) {
                    mLoadingState = Constants.LOADING_STATE_HOMEWORK_NO_MORE;
                    mOffset = "";
                } else {
                    mLoadingState = Constants.LOADING_STATE_HOMEWORK_EMPTY;
                }
                mWebView.setLayoutParams(new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels,
                        (int) getResources().getDimension(R.dimen.height_homework_select_refresh_layout)));
                mWebView.setVisibility(View.INVISIBLE);
            }
            refreshLoadingState(mLoadingState, true);
        }

        @Override
        protected void showLoading() {
            if (mLoadingState != Constants.LOADING_STATE_HOMEWORK_NO_MORE
                    && mLoadingState != Constants.LOADING_STATE_HOMEWORK_EMPTY) {
                mErrorLayout.setVisibility(View.INVISIBLE);
            }
            mNestedScrollView.smoothScrollTo(0, 0);
            mRefreshLayout.beginRefreshing();
        }

        @Override
        protected void dismissLoading() {
            mRefreshLayout.endRefreshing();
            misLoading = false;
        }
    }

    private void webviewLoadHtml(ArrayList<HomeworkEntity> homeworkList) {
        LogHelper.d(TAG, "homeworkList size: " + homeworkList.size());
        StringBuffer htmlContentStrBuffer = new StringBuffer();
        if (mQustionHtmlMap == null) {
            mQustionHtmlMap = new LinkedHashMap<>();
        } else {
            mQustionHtmlMap.clear();
        }
        for (HomeworkEntity homeworkData : homeworkList) {
            mQustionHtmlMap.put(homeworkData.getQuestion_id(), homeworkData.getQuestion_html());
            htmlContentStrBuffer.append(homeworkData.getQuestion_html());
        }
        String htmlString = StringUtils.replaceAssetFileString(mContext, "choose_questions.html",
                htmlContentStrBuffer.toString());
        LogHelper.d(TAG, "webviewLoadHtml");
        mWebView.loadDataWithBaseURL(null, htmlString, "text/html", "UTF-8", null);
    }

    //已经选过的题目回县减号
    private void initSelectedQuestion() {
        try {
            if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                MyHashSet<String> slectedQuestionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId,
                        new TypeToken<MyHashSet<String>>() {
                        }.getType());
                LogHelper.d(TAG, "slectedQuestionIdSet: " + slectedQuestionIdSet.toString());
                mWebView.loadUrl("javascript:selected('" + slectedQuestionIdSet.toString() + "')");
            } else {
                if (mQuestionIdSet != null && mQuestionIdSet.size() > 0) {
                    mWebView.loadUrl("javascript:selected('" + mQuestionIdSet.toString() + "')");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        selectHomework(mLessonId, mKnowledgeDetailId, mKnowledgePoint, mHomeworkType, mHomeworkDifficulty,
                mOffset, HOMEWORK_SIZE);
    }

    @Override
    protected void refreshLoadingState(int loadingState, boolean isFirstLoading) {
        super.refreshLoadingState(loadingState, isFirstLoading);
        switch (loadingState) {
            case Constants.LOADING_STATE_HOMEWORK_EMPTY:
                mStateImg.setImageResource(R.mipmap.icon_homework_select_empty);
                mStateDesc.setText(R.string.error_msg_load_homework_empty);
                mEmptyLayout.setVisibility(View.VISIBLE);
                mStateDescSub.setVisibility(View.INVISIBLE);
                break;
            case Constants.LOADING_STATE_HOMEWORK_NO_MORE:
                mStateImg.setImageResource(R.mipmap.icon_homework_select_no_more);
                mStateDesc.setText(R.string.msg_load_homework_no_more);
                mStateDescSub.setText(R.string.msg_load_homework_no_more_sub);
                mStateDescSub.setVisibility(View.VISIBLE);
                mEmptyLayout.setVisibility(View.VISIBLE);
                break;
            default:
                if (isFirstLoading) {
                    mStateDescSub.setVisibility(View.INVISIBLE);
                }
        }
    }

    @Subscribe
    public void changeSet(ChangeSetEvent changeSetEvent) {
        LogHelper.d(TAG, "changeSet");
        LogHelper.d(TAG, "isLoading: " + misLoading);
        LogHelper.d(TAG, "HomeworkDifficulty: " + mHomeworkDifficulty);

        if (mHomeworkType == Constants.HOMEWORK_TYPE_COMPLETION
                || mHomeworkType == Constants.HOMEWORK_TYPE_CHECKING) {
            return;
        }
        if (misLoading) {
            //还未加载完，换一批题按钮点击无效
            return;
        }
        if (mIsLoaddataSuccess && mHomeworkDifficulty == changeSetEvent.getHomeworkDifficulty()) {
            mIsChangeSet = true;
            selectHomework(mLessonId, mKnowledgeDetailId, mKnowledgePoint, mHomeworkType, mHomeworkDifficulty,
                    mOffset, HOMEWORK_SIZE);
        }
    }

    @Subscribe
    public void changeKnowledgePoint(ChangeknowledgePointEvent changeknowledgePointEvent) {
        LogHelper.d(TAG, "Subscribe changeKnowledgePoint " + TAG);
        unsubscribe();
        mIsChangeSet = false;
        mKnowledgePoint = changeknowledgePointEvent.getKnowledge_detail();
        mKnowledgeDetailId = changeknowledgePointEvent.getKnowledge_detail_ids() + "";
        mOffset = "";
        mIsLoaddataSuccess = false;//更换知识点后,相当于又是首次加载数据
        selectHomework(mLessonId, mKnowledgeDetailId, mKnowledgePoint, mHomeworkType, mHomeworkDifficulty,
                mOffset, HOMEWORK_SIZE);
    }

    @Subscribe
    public void changeHomeworkType(ChangeHomeworkTypeEvent changeHomeworkTypeEvent) {
        mHomeworkType = changeHomeworkTypeEvent.getHomeworkType();
        switch (mHomeworkType) {
            case Constants.HOMEWORK_TYPE_CHOICE:
                mOffLineLayout.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                if (!mIsLoaddataSuccess) {
                    //还未加载过数据
                    selectHomework(mLessonId, mKnowledgeDetailId, mKnowledgePoint, mHomeworkType,
                            mHomeworkDifficulty, mOffset, HOMEWORK_SIZE);
                }
                break;
            case Constants.HOMEWORK_TYPE_COMPLETION:
                mOffLineLayout.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
                break;
            case Constants.HOMEWORK_TYPE_CHECKING:
                mOffLineLayout.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Subscribe
    public void homeworkOutOfLimit(HomeworkOutOfLimitEvent homeworkOutOfLimitEvent) {
        String question_id = homeworkOutOfLimitEvent.getQuestion_id();
        LogHelper.d(TAG, "homeworkOutOfLimit:" + question_id);
        mWebView.loadUrl("javascript:plus_minus('" + question_id + "')");
        ToastUtil.showToastBottom(mContext, R.string.toast_homework_too_much, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        unsubscribe();
        RxBus.getInstance().unregister(this);
    }

    private void unsubscribe() {
        if (mSelectHomeworkSubscriber != null) {
            mSelectHomeworkSubscriber.onCancle();
        }
        if (mSelectHomeworkSubscription != null && !mSelectHomeworkSubscription.isUnsubscribed()) {
            mSelectHomeworkSubscription.unsubscribe();
        }
    }
}
