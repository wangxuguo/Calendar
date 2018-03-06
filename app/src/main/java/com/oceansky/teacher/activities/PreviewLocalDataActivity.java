package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
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
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.ModifyHomeworkEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.StringUtils;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class PreviewLocalDataActivity extends BaseActivityWithLoadingState {
    private static final String TAG = PreviewLocalDataActivity.class.getSimpleName();

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

    private int                           mHomeworkId;
    private String                        mTextbookIds;
    private String                        mChapterIds;
    private String                        mSectionIds;
    private String                        mDetailIds;
    private String                        mQuestionIds;
    private CustomProgressDialog          mDialog;
    private ModifyHomeworkSubscriber      mModifyHomeworkSubscriber;
    private Subscription                  mModifyHomeworkSubscription;
    private String                        mHomeworkTitle;
    private LinkedHashMap<String, String> mHtmlMap;
    private MyHashSet<Integer>            mTextbookIdSet;
    private MyHashSet<Integer>            mChapterIdSet;
    private MyHashSet<Integer>            mSectionIdSet;
    private MyHashSet<String>             mQuestionIdSet;
    private MyHashSet<Integer>            mDetailIdSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_homework_preview));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mBtnAssignHomework.setText(R.string.btn_assign_homework);
        mBtnSaveHomework.setVisibility(View.VISIBLE);
        mBtnSaveHomework.setText(R.string.btn_save_homework);
        mWvHomeworkPreview.getSettings().setJavaScriptEnabled(true);
        mWvHomeworkPreview.getSettings().setDefaultTextEncodingName("UTF-8");
        mWvHomeworkPreview.addJavascriptInterface(new JsInteration(), "controller");
        mDialog = CustomProgressDialog.createDialog(this);
    }


    private void initData() {
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        try {
            mHtmlMap = Reservoir.get(Constants.HOMEWORK_HTML_MAP, new TypeToken<LinkedHashMap<String, String>>() {
            }.getType());
            mQuestionIdSet = Reservoir.get(Constants.QUESTION_ID_SET, new TypeToken<MyHashSet<String>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextbookIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.TEXTBOOK_ID_SET);
        mChapterIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.CHAPTER_ID_SET);
        mSectionIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.SECTION_ID_SET);
        mDetailIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.DETAIL_ID_SET);
        refreshBottomTabview();
        LogHelper.d(TAG, "HtmlMap: " + mHtmlMap.toString());
        LogHelper.d(TAG, "TextbookIdSet: " + mTextbookIdSet.toString());
        LogHelper.d(TAG, "ChapterIdSet: " + mChapterIdSet.toString());
        LogHelper.d(TAG, "SectionIdSet: " + mSectionIdSet.toString());
        LogHelper.d(TAG, "DetailIdSet: " + mDetailIdSet.toString());
        LogHelper.d(TAG, "QusetionIdSet: " + mQuestionIdSet.toString());
        StringBuffer htmlContentStrBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : mHtmlMap.entrySet()) {
            htmlContentStrBuffer.append(entry.getValue());
        }
        webviewLoadHtml(htmlContentStrBuffer.toString());
    }

    private void refreshBottomTabview() {
        int size = mQuestionIdSet.size();
        mTvSelected.setText("已选择" + size + "道题");
        if (size < 1) {
            refreshLoadingState(Constants.LOADING_STATE_HOMEWORK_EMPTY, true);
        }
    }

    public class JsInteration {
        @JavascriptInterface
        public void confirmDelete(String id) {
            LogHelper.d(TAG, "confirmDelete: " + id);
            if (mQuestionIdSet.contains(id)) {
                mHtmlMap.remove(id);
                mQuestionIdSet.remove(id);
                cacheOperator();
                runOnUiThread(() -> refreshBottomTabview());
            }
        }
    }

    private void webviewLoadHtml(String htmlContentStr) {
        String htmlString = StringUtils.replaceAssetFileString(this, "questions_preview.html", htmlContentStr);
        mWvHomeworkPreview.loadDataWithBaseURL(null, htmlString, "text/html", "UTF-8", null);
        mWvHomeworkPreview.setVisibility(View.VISIBLE);
    }

    private void saveHomeworkHttp() {
        MobclickAgent.onEvent(this, Constants.PUBLISH_HOMEWORK_TOUCHED);
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mTextbookIds = mTextbookIdSet.toString();
            mChapterIds = mChapterIdSet.toString();
            mDetailIds = mDetailIdSet.toString();
            mSectionIds = mSectionIdSet.toString();
            mQuestionIds = mQuestionIdSet.toString();
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mModifyHomeworkSubscriber = new ModifyHomeworkSubscriber(this);
            mModifyHomeworkSubscription = HttpManager.modifyHomework(token, mHomeworkId, mTextbookIds,
                    mChapterIds, mSectionIds, mDetailIds, mQuestionIds).subscribe(mModifyHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    @OnClick(R.id.select_btn1)
    public void assignHomework() {
        MobclickAgent.onEvent(this, Constants.GENERATE_HOMEWORK_TOUCHED);
        //点击布置作业
        Intent intent = new Intent(PreviewLocalDataActivity.this, AssignHomeworkActivity.class);
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
        MobclickAgent.onEvent(this, Constants.SAVE_HOMEWORK_TOUCHED);
        saveHomeworkHttp();
    }

    @Subscribe
    public void assignHomework(AssignHomeworkEvent assignHomeworkEvent) {
        finish();
    }

    @Subscribe
    public void saveHomework(ModifyHomeworkEvent modifyHomeworkEvent) {
        finish();
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
            ToastUtil.showToastBottom(mContext,getString(R.string.toast_save_success), Toast.LENGTH_SHORT);
            clearHomeworkLocalCache();
            RxBus.getInstance().post(new ModifyHomeworkEvent());
            RxBus.getInstance().post(new RefreshUnassignedHomeworkListEvent());
            finish();
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
