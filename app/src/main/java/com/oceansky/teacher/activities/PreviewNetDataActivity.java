package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.ModifyHomeworkEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.HomeworkPreviewEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.StringUtils;
import com.oceansky.teacher.utils.ToastUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class PreviewNetDataActivity extends BaseActivityWithLoadingState {
    private static final String TAG = PreviewNetDataActivity.class.getSimpleName();

    @Bind(R.id.select_btn1)
    Button mBtnAssignHomework;

    @Bind(R.id.select_btn2)
    Button mBtnSaveHomework;

    @Bind(R.id.webview_homework_preview)
    WebView mWvHomeworkPreview;

    @Bind(R.id.loading)
    ImageView mIvLoading;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.error_layout)
    RelativeLayout mErrorLayout;

    @Bind(R.id.select_tv_selected)
    TextView mTvSelected;

    @Bind(R.id.tv_setting)
    TextView mTvAdd;

    private AnimationDrawable         mLoadingAnimation;
    private CustomProgressDialog      mDialog;
    private ModifyHomeworkSubscriber  mModifyHomeworkSubscriber;
    private Subscription              mModifyHomeworkSubscription;
    private HomeworkPreviewSubscriber mHomeworkPreviewSubscriber;
    private Subscription              mHomeworkPreviewSubscription;
    private String                    mHtmlContentStr;
    private String                    mHomeworkTitle;
    private int                       mHomeworkId;
    private int                       mGradeId;
    private int                       mLessonId;
    private boolean                   mIsReuseHomework;
    private boolean                   mHaveLocalCache;

    private MyHashSet<Integer>            mTextbookIdSet = new MyHashSet<>();
    private MyHashSet<Integer>            mChapterIdSet  = new MyHashSet<>();
    private MyHashSet<Integer>            mSectionIdSet  = new MyHashSet<>();
    private MyHashSet<Integer>            mDetailIdSet   = new MyHashSet<>();
    private MyHashSet<String>             mQuestionIdSet = new MyHashSet<>();
    private LinkedHashMap<String, String> mHtmlMap       = new LinkedHashMap<>();//questionID:questionHtml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogHelper.d(TAG, "onResume");
        //如果数据进行了修改,再次回到此页面时,更新数据
        try {
            mHaveLocalCache = Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId);
            if (mHaveLocalCache) {
                //如果有本地缓存数据,说明对题目进行了操作
                LogHelper.d(TAG, "mHaveLocalCache: " + mHaveLocalCache);
                loadLocalCacheDataAndRefreshView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_homework_preview));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mBtnAssignHomework.setText(R.string.btn_assign_homework);
        mBtnSaveHomework.setVisibility(View.VISIBLE);
        mBtnSaveHomework.setText(R.string.btn_save_homework);
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
        mWvHomeworkPreview.getSettings().setJavaScriptEnabled(true);
        mWvHomeworkPreview.getSettings().setDefaultTextEncodingName("UTF-8");
        mWvHomeworkPreview.addJavascriptInterface(new JsInteration(), "controller");
        mDialog = CustomProgressDialog.createDialog(this);
        mTvAdd.setEnabled(false);//预览接口的数据回来后才能点击,否则后面的页面获取不到html数据
    }

    private void initData() {
        mTitleBar.setSettingButtonText(getString(R.string.btn_add_homework));
        mIsReuseHomework = getIntent().getBooleanExtra(Constants.IS_REUSE_HOMEWORK, false);
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        mGradeId = getIntent().getIntExtra(Constants.GRADE_ID, -1);
        mLessonId = getIntent().getIntExtra(Constants.LESSON_ID, -1);
        LogHelper.d(TAG, "IsReuseHomework: " + mIsReuseHomework);
        LogHelper.d(TAG, "HomeworkId: " + mHomeworkId);
        LogHelper.d(TAG, "HomeworkTitle: " + mHomeworkTitle);
        LogHelper.d(TAG, "GradeId: " + mGradeId);
        LogHelper.d(TAG, "LessonId: " + mLessonId);
        if (mIsReuseHomework) {
            LogHelper.d(TAG, "ReuseHomework");
            //已布置作业列表重新使用进入
            try {
                if (Reservoir.contains(Constants.HOMEWORK_HTML_MAP)) {
                    mHtmlMap = Reservoir.get(Constants.HOMEWORK_HTML_MAP, new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType());
                    loadHtmlFromMap(mHtmlMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            getDataSetFromPreviousActivity();
            mTvSelected.setText("已选择" + mQuestionIdSet.size() + "道题");
        } else {
            //待布置作业列表item点击进入
            try {
                mHaveLocalCache = Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId);
                if (mHaveLocalCache) {
                    //本地进行过修改,中途崩溃退出,此时点击进入显示修改缓存的内容
                    loadLocalCacheDataAndRefreshView();
                } else {
                    //正常情况都是加载网络数据
                    loadNetDataAndRefreshView();
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadNetDataAndRefreshView();
            }
        }
    }

    /**
     * 加载本地缓存的数据并刷新视图
     */
    private void loadLocalCacheDataAndRefreshView() throws Exception {
        mTextbookIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
        }.getType());
        mChapterIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
        }.getType());
        mSectionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
        }.getType());
        mDetailIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
        }.getType());
        mQuestionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<String>>() {
        }.getType());
        mHtmlMap = Reservoir.get(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId, new TypeToken<LinkedHashMap<String, String>>() {
        }.getType());
        if (mHtmlMap.size() > 0) {
            loadHtmlFromMap(mHtmlMap);
            refreshLoadingState(Constants.LOADING_STATE_SUCCESS, true);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_HOMEWORK_EMPTY, true);
        }
        mTvSelected.setText("已选择" + mQuestionIdSet.size() + "道题");
        LogHelper.d(TAG, "getDataSetFromLocalCache");
        LogHelper.d(TAG, "textbookIds: " + mTextbookIdSet.toString());
        LogHelper.d(TAG, "chapterIds: " + mChapterIdSet.toString());
        LogHelper.d(TAG, "sectionIds: " + mSectionIdSet.toString());
        LogHelper.d(TAG, "detailIds: " + mDetailIdSet.toString());
        LogHelper.d(TAG, "questionIds: " + mQuestionIdSet.toString());
    }

    /**
     * 加载服务器存储的数据并刷新视图
     */
    private void loadNetDataAndRefreshView() {
        getDataSetFromPreviousActivity();
        mTvSelected.setText("已选择" + mQuestionIdSet.size() + "道题");
        previewHomework(mHomeworkId);
    }

    private void loadHtmlFromMap(LinkedHashMap<String, String> htmlMap) {
        StringBuffer htmlStrBuf = new StringBuffer();
        for (Map.Entry<String, String> entry : htmlMap.entrySet()) {
            htmlStrBuf.append(entry.getValue());
        }
        mHtmlContentStr = htmlStrBuf.toString();
        webviewLoadHtml(mHtmlContentStr);
    }

    private void getDataSetFromPreviousActivity() {
        MyHashSet<Integer> textbookIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.TEXTBOOK_ID_SET);
        MyHashSet<Integer> chapterIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.CHAPTER_ID_SET);
        MyHashSet<Integer> sectionIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.SECTION_ID_SET);
        MyHashSet<Integer> detailIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.DETAIL_ID_SET);
        try {
            if (Reservoir.contains(Constants.QUESTION_ID_SET)) {
                MyHashSet<String> questionIdSet = Reservoir.get(Constants.QUESTION_ID_SET, new TypeToken<MyHashSet<String>>() {
                }.getType());
                if (questionIdSet != null) {
                    mQuestionIdSet.addAll(questionIdSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (textbookIdSet != null) {
            mTextbookIdSet.addAll(textbookIdSet);
        }
        if (chapterIdSet != null) {
            mChapterIdSet.addAll(chapterIdSet);
        }
        if (sectionIdSet != null) {
            mSectionIdSet.addAll(sectionIdSet);
        }
        if (detailIdSet != null) {
            mDetailIdSet.addAll(detailIdSet);
        }
        LogHelper.d(TAG, "getDataSetFromPreviousActivity");
        LogHelper.d(TAG, "textbookIds: " + mTextbookIdSet.toString());
        LogHelper.d(TAG, "chapterIds: " + mChapterIdSet.toString());
        LogHelper.d(TAG, "sectionIds: " + mSectionIdSet.toString());
        LogHelper.d(TAG, "detailIds: " + mDetailIdSet.toString());
        LogHelper.d(TAG, "questionIds: " + mQuestionIdSet.toString());
    }

    public void previewHomework(int homeworkId) {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mHomeworkPreviewSubscriber = new HomeworkPreviewSubscriber(this);
            mHomeworkPreviewSubscription = HttpManager.previewHomework(token, homeworkId).subscribe(mHomeworkPreviewSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    @Override
    protected void onErrorLayoutClick() {
        super.onErrorLayoutClick();
        mErrorLayout.setVisibility(View.GONE);
        previewHomework(mHomeworkId);
    }

    private class HomeworkPreviewSubscriber extends LoadingSubscriber<ArrayList<HomeworkPreviewEntity>> {

        public HomeworkPreviewSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, true);
        }

        @Override
        protected void showLoading() {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingAnimation.start();
        }

        @Override
        protected void dismissLoading() {
            mLoadingLayout.setVisibility(View.INVISIBLE);
            mLoadingAnimation.stop();
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
                    refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
                    break;
                case ApiException.ERROR_LOAD_FAIL:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
        }

        @Override
        public void onNext(ArrayList<HomeworkPreviewEntity> homeworkPreviewList) {
            super.onNext(homeworkPreviewList);
            LogHelper.d(TAG, "HomeworkPreviewList" + homeworkPreviewList);
            LogHelper.d(TAG, "HomeworkPreviewList size:" + homeworkPreviewList.size());
            StringBuffer htmlContentStrBuffer = new StringBuffer();
            for (HomeworkPreviewEntity homeworkPreviewEntity : homeworkPreviewList) {
                htmlContentStrBuffer.append(homeworkPreviewEntity.getQuestion_html());
                mHtmlMap.put(homeworkPreviewEntity.getQuestion_id(), homeworkPreviewEntity.getQuestion_html());
            }
            mHtmlContentStr = htmlContentStrBuffer.toString();
            webviewLoadHtml(mHtmlContentStr);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }
    }

    public class JsInteration {
        @JavascriptInterface
        public void confirmDelete(String id) {
            LogHelper.d(TAG, "confirmDelete: " + id);
            if (mQuestionIdSet.contains(id)) {
                mQuestionIdSet.remove(id);
                mHtmlMap.remove(id);
                runOnUiThread(() -> refreshBottomTabview());
                cacheOperator();
            }
        }
    }

    /**
     * 对作业的修改进行缓存
     */
    private void cacheOperator() {
        try {
            Reservoir.put(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId, mQuestionIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId, mHtmlMap);
            Reservoir.put(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId, mTextbookIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId, mChapterIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId, mSectionIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId, mDetailIdSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 作业保存成功后清除本地修改的缓存
     */
    private void clearHomeworkLocalCache() {
        try {
            if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                Reservoir.delete(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void webviewLoadHtml(String htmlContentStr) {
        String htmlString = StringUtils.replaceAssetFileString(this, "questions_preview.html", htmlContentStr);
        mWvHomeworkPreview.loadDataWithBaseURL(null, htmlString, "text/html", "UTF-8", null);
        mWvHomeworkPreview.setVisibility(View.VISIBLE);
        mTvAdd.setEnabled(true);
    }

    private void saveHomeworkHttp() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mModifyHomeworkSubscriber = new ModifyHomeworkSubscriber(this);
            mModifyHomeworkSubscription = HttpManager.modifyHomework(token, mHomeworkId, mTextbookIdSet.toString(),
                    mChapterIdSet.toString(), mSectionIdSet.toString(), mDetailIdSet.toString(), mQuestionIdSet.toString())
                    .subscribe(mModifyHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    @OnClick(R.id.select_btn1)
    public void assignHomework() {
        Intent intent = new Intent(PreviewNetDataActivity.this, AssignHomeworkActivity.class);
        intent.putExtra(Constants.HOMEWORK_TITLE, mHomeworkTitle);
        intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
        intent.putExtra(Constants.TEXTBOOK_ID_SET, mTextbookIdSet);
        intent.putExtra(Constants.CHAPTER_ID_SET, mChapterIdSet);
        intent.putExtra(Constants.SECTION_ID_SET, mSectionIdSet);
        intent.putExtra(Constants.DETAIL_ID_SET, mDetailIdSet);
        try {
            Reservoir.put(Constants.QUESTION_ID_SET, mQuestionIdSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }

    @OnClick(R.id.select_btn2)
    public void saveHomework() {
        saveHomeworkHttp();
    }

    private class ModifyHomeworkSubscriber extends LoadingSubscriber<SimpleResponse> {

        public ModifyHomeworkSubscriber(Context context) {
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
        public void onNext(SimpleResponse simpleResponse) {
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            clearHomeworkLocalCache();
            ToastUtil.showToastBottom(mContext, getString(R.string.toast_save_success), Toast.LENGTH_SHORT);
            RxBus.getInstance().post(new ModifyHomeworkEvent());
            RxBus.getInstance().post(new RefreshUnassignedHomeworkListEvent());
            finish();
        }
    }

    @OnClick(R.id.tv_setting)
    public void addHomework() {
        Intent intent = new Intent(this, KnowledgePointSelectActivity.class);
        intent.putExtra(Constants.IS_ADD_HOMEWORK, true);
        intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
        intent.putExtra(Constants.HOMEWORK_TITLE, mHomeworkTitle);
        intent.putExtra(Constants.GRADE_ID, mGradeId);
        intent.putExtra(Constants.LESSON_ID, mLessonId);
        try {
            Reservoir.put(Constants.HOMEWORK_HTML_MAP, mHtmlMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intent.putExtra(Constants.TEXTBOOK_ID_SET, mTextbookIdSet);
        intent.putExtra(Constants.CHAPTER_ID_SET, mChapterIdSet);
        intent.putExtra(Constants.SECTION_ID_SET, mSectionIdSet);
        intent.putExtra(Constants.DETAIL_ID_SET, mDetailIdSet);
        try {
            Reservoir.put(Constants.QUESTION_ID_SET, mQuestionIdSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_bottom_in, R.anim.pop_win_content_fade_out);
    }

    @Subscribe
    public void assignHomework(AssignHomeworkEvent assignHomeworkEvent) {
        finish();
    }

    @Subscribe
    public void saveHomework(ModifyHomeworkEvent modifyHomeworkEvent) {
        finish();
    }

    private void refreshBottomTabview() {
        int size = mQuestionIdSet.size();
        mTvSelected.setText("已选择" + size + "道题");
        if (size < 1) {
            refreshLoadingState(Constants.LOADING_STATE_HOMEWORK_EMPTY, true);
        }
    }

    private void showDialog() {
        CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
        ibuilder.setMessage(R.string.dialog_unsave_homework_back);
        ibuilder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            dialog.dismiss();
            clearHomeworkLocalCache();
            finish();
        });
        ibuilder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        ibuilder.create().show();
    }

    private void back() {
        try {
            if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                showDialog();
            } else {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onBackStack() {
        back();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mModifyHomeworkSubscriber != null) {
            mModifyHomeworkSubscriber.onCancle();
        }
        if (mModifyHomeworkSubscription != null && !mModifyHomeworkSubscription.isUnsubscribed()) {
            mModifyHomeworkSubscription.unsubscribe();
        }
        if (mHomeworkPreviewSubscriber != null) {
            mHomeworkPreviewSubscriber.onCancle();
        }
        if (mHomeworkPreviewSubscription != null && !mHomeworkPreviewSubscription.isUnsubscribed()) {
            mHomeworkPreviewSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
