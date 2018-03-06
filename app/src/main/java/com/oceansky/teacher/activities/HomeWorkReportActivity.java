package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作业报告页面（Html） 展示学生作业情况，页面内可点击
 * 由我的作业--->已布置作业进入
 * User: 王旭国
 * Date: 16/8/29 16:27
 * Email:wangxuguo@jhyx.com.cn
 */
public class HomeWorkReportActivity extends BaseActivity {
    private static final String TAG = HomeWorkReportActivity.class.getSimpleName();
    private int homework_id;
    private int course_id;
    @Bind(R.id.webview_homework_report)
    WebView        webview;
    @Bind(R.id.error_layout)
    RelativeLayout error_layout;
    @Bind(R.id.loading_layout)
    FrameLayout    loading_layout;
    @Bind(R.id.loading)
    ImageView      loadingImg;
    @Bind(R.id.error_img)
    ImageView      mErrorImg;
    @Bind(R.id.error_desc)
    TextView       mErrorDesc;
    private AnimationDrawable mLoadingAnimation;
    private String            mUrl;
    private boolean           isPageLoaded;
    private TimeCount         mTime;
    private Context           mContext;
    private String            mTitle;
    private boolean           mIsCannotVisit;
    private boolean           mGoErrorQuestion;
    private boolean           mIsLoading;
    private boolean           isOnPause;

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            if (!isFinishing()) {
                // mDialog.dismiss();
                isPageLoaded = true;
                loading_layout.setVisibility(View.GONE);
                mLoadingAnimation.stop();
            }
            LogHelper.d(TAG, "-----------------onFinish----------------------");
            mIsLoading = false;
            setLoadFailView();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_report);
        ButterKnife.bind(this);
        mContext = this;
        homework_id = getIntent().getIntExtra(Constants.HOMEWORK_ID, 0);
        course_id = getIntent().getIntExtra(Constants.COURSE_ID, 0);
        mUrl = getIntent().getStringExtra(Constants.WEBVIEW_URL);
        boolean isFromReport = getIntent().getBooleanExtra("isFromReport", false);  // 判断是否是从作业报告里面点击的
        if (mUrl == null) {
            mUrl = FeatureConfig.API_HOST_NAME + "h5/homework/" + homework_id + "/course/" + course_id + "/homework_report";
        }
        mTitle = getIntent().getStringExtra(Constants.WEBVIEW_TITLE);
        if (!isFromReport && mTitle == null) {  // TODO // 在正常情况 下,推送到达 的作业报告,目前没有包含作业名称
            mTitle = getString(R.string.title_homework_report);
        }
        LogHelper.d(TAG, "homework_id: " + homework_id + "  course_id: " + course_id + "  mUrl: " + mUrl);
        mTime = new TimeCount(10000, 1000);
        initView();
    }

    private void initView() {
        mTitleBar.setTitle(mTitle);
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        //加载动画
        loadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) loadingImg.getDrawable();
        //        boolean isFromNotification = getIntent().getBooleanExtra(Constants.IS_FROM_NOTIFICATION, false);
        initData();
    }

    private void initData() {
        WebSettings webSettings = webview.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "Nautile");
        webSettings.setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new JsInteration(), "controller");
        webSettings.setSupportZoom(false);
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webview.setWebChromeClient(new MyWebChromeClient());
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogHelper.d(TAG, "-----------------onPageStarted----------------------");
            }

            @Override
            public void onReceivedHttpError(final WebView view, final WebResourceRequest request, WebResourceResponse errorResponse) {
                LogHelper.d(TAG, "----------------onReceivedHttpError------------------");
                final int statusCode;
                // SDK < 21 does not provide statusCode
                if (Build.VERSION.SDK_INT < 21) {
                    statusCode = 1000;
                } else {
                    statusCode = errorResponse.getStatusCode();
                }

                LogHelper.d(TAG, "[onReceivedHttpError]" + statusCode);
                if (statusCode == 4004 || statusCode == 404) {
                    setCannotVisitView();
                    mIsCannotVisit = true;
                    LogHelper.d(TAG, "------------set 404 page-----------");
                } else {
                    setLoadFailView();
                    mIsCannotVisit = false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogHelper.d(TAG, "-----------------onPageFinished----------------------");
                mTime.cancel();
                if (!mIsLoading) {
                    mIsLoading = false;
                    LogHelper.d(TAG, "onPageFinished  mIsLoading  false return");
                    return;
                }
                mIsLoading = false;
                mLoadingAnimation.stop();
                loading_layout.setVisibility(View.GONE);

                LogHelper.d(TAG, "view.getTitle(): " + view.getTitle() + " mIsCannotVisit: " + mIsCannotVisit);
                //处理某些特殊情况,所有的回调方法检测不到错误的情况下
                if ((view.getTitle() != null && (view.getTitle().contains("api/h5/homework") && !view.getTitle().contains("#") && !mGoErrorQuestion) || view.getTitle().contains("Error")) && !mIsCannotVisit) {
                    LogHelper.d(TAG, view.getTitle() + "---------------onPageCommitVisible Error-----------");
                    mTime.cancel();
                    //                    setLoadFailView();
                    setCannotVisitView();
                    mIsCannotVisit = true;
                    return;
                } else if (view.getTitle() == null || TextUtils.isEmpty(view.getTitle()) || mIsCannotVisit) {
                    setCannotVisitView();
                    mIsCannotVisit = true;
                    return;
                }
                setNormalView();
                mGoErrorQuestion = false;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                LogHelper.d(TAG, "-----------------onPageCommitVisible----------------------");
                if (!isFinishing()) {
                    loading_layout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                mTime.cancel();
                LogHelper.d(TAG, "view.getTitle: " + view.getTitle());
                //处理某些特殊情况,所有的回调方法检测不到错误的情况下
                if ((view.getTitle().contains("ocean-sky") || view.getTitle().contains("Error")) && !mIsCannotVisit) {
                    LogHelper.d(TAG, view.getTitle() + "---------------onPageCommitVisible Error-----------");
                    setLoadFailView();
                    mIsCannotVisit = false;
                } else {
                    //                    setCannotVisitView();
                    //                    mIsCannotVisit = true;
                }
                if (view.getTitle().equals("网页无法打开")) {  // 连接代理,代理已经断开的情况
                    LogHelper.d(TAG, "-------onPageCommitVisible---------网页无法打开");
                    setCannotVisitView();
                    mIsCannotVisit = true;

                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                LogHelper.d(TAG, "-----------------onReceivedError----------------------");
                mTime.cancel();
                isPageLoaded = false;
                if (!isFinishing()) {
                    loading_layout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int status = error.getErrorCode();
                    if (status == 4004) {
                        setCannotVisitView();
                    } else {
                        setLoadFailView();
                    }
                } else {
                    setLoadFailView();
                }

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                mTime.cancel();
                isPageLoaded = false;
                LogHelper.d(TAG, "------------------onReceivedSslError--------error.getPrimaryError()-------------" + error.getPrimaryError());
                if (SslError.SSL_UNTRUSTED == error.getPrimaryError()) {
                    handler.proceed();
                } else {
                    super.onReceivedSslError(view, handler, error);
                }
                if (!isFinishing()) {
                    loading_layout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                int status = error.getPrimaryError();
                if (status == 4004) {
                    setCannotVisitView();
                } else {
                    setLoadFailView();
                }
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogHelper.d(TAG, "-----------------shouldOverrideUrlLoading----------------------");
                return false;
            }
        });
        loadUrl();
    }

    private void loadUrl() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            if (!TextUtils.isEmpty(mUrl)) {
                Map<String, String> extraHeaders = new HashMap<>();
                extraHeaders.put("Authorization", "Bearer " + SharePreferenceUtils.getStringSecPref(this, Constants.KEY_ACCESS_TOKEN, ""));
                extraHeaders.put("client-version", Constants.CLIENT_VERSION);
                LogHelper.d(TAG, "extraHeaders: " + extraHeaders.toString());
                LogHelper.d(TAG, "mUrl: " + mUrl);

                mTime.start();
                if (!isFinishing()) {
                    //mDialog.show();
                    isPageLoaded = true;
                    loading_layout.setVisibility(View.VISIBLE);
                    mLoadingAnimation.start();
                }
                mIsLoading = true;
                webview.loadUrl(mUrl, extraHeaders);
                setLoading_layout();
            } else {
                setCannotVisitView();
            }
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
            setNetworkErrorView();
        }


    }

    private void setNetworkErrorView() {
        error_layout.setVisibility(View.VISIBLE);
        loading_layout.setVisibility(View.GONE);
        mErrorImg.setImageResource(R.mipmap.icon_common_wifi);
        mErrorDesc.setText(R.string.error_msg_no_net);
    }

    private void setLoadFailView() {
        error_layout.setVisibility(View.VISIBLE);
        loading_layout.setVisibility(View.GONE);
        mErrorImg.setImageResource(R.mipmap.icon_error_load_failure);
        mErrorDesc.setText(R.string.error_msg_load_failure);
        error_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
    }

    protected void setLoading_layout() {
        error_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
    }

    protected void setCannotVisitView() {
        error_layout.setVisibility(View.VISIBLE);
        webview.setVisibility(View.GONE);
        mErrorImg.setImageResource(R.mipmap.icon_cannot_visit);
        mErrorDesc.setText(R.string.error_page_cannot_visit);
        error_layout.setOnClickListener(null);
    }

    protected void setNormalView() {
        error_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.GONE);
    }

    public class JsInteration {
        // 错题的点击
        @JavascriptInterface
        public void goErrorQuestion() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoErrorQuestion = true;
                    LogHelper.d(TAG, "goErrorQuestion: ");
                    MobclickAgent.onEvent(HomeWorkReportActivity.this, Constants.HOMEWORK_WRONG_QUESTION_TOUCHED);
                }
            });
        }

        @JavascriptInterface
        public void redirectQuestReport() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.d(TAG, "redirectQuestReport: ");
                    Intent intent = new Intent(mContext, HomeWorkReportActivity.class);
                    intent.putExtra(Constants.HOMEWORK_ID, homework_id);
                    intent.putExtra(Constants.COURSE_ID, course_id);
                    intent.putExtra("isFromReport", true);
                    intent.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.homework_wrongquestions_detail));
                    String url = FeatureConfig.API_HOST_NAME + "h5/homework/" + homework_id + "/course/" + course_id + "/question_report";
                    intent.putExtra(Constants.WEBVIEW_URL, url);
                    startActivity(intent);
                }
            });
        }

        @JavascriptInterface
        public void redirectKidReport(final String kid_id, final String kid_name) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MobclickAgent.onEvent(HomeWorkReportActivity.this, Constants.HOMEWORK_REPORT_STUDENT_NAME_TOUCHED);
                    LogHelper.d(TAG, "redirectKidReport: " + kid_id);
                    Intent intent = new Intent(mContext, HomeWorkReportActivity.class);
                    //                    intent.putExtra(Constants.WEBVIEW_TITLE,getString(R.string.homework_wrongquestions_detail));
                    intent.putExtra("isFromReport", true);
                    intent.putExtra(Constants.HOMEWORK_ID, homework_id);
                    intent.putExtra(Constants.COURSE_ID, course_id);  ///api/h5/homework/:homework_id/course/:course_id/kid/:kid_id/homework_report
                    String url = FeatureConfig.API_HOST_NAME + "h5/homework/" + homework_id + "/course/" + course_id + "/kid/" + kid_id + "/homework_report";
                    intent.putExtra(Constants.WEBVIEW_URL, url);
                    // TODO
                    intent.putExtra(Constants.WEBVIEW_TITLE, kid_name + getString(R.string.homework_detail));
                    startActivity(intent);
                }
            });
        }

    }


    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            LogHelper.d(TAG, "onReceivedTitle title: " + title);
            if (title.equals("找不到网页")) {
                setCannotVisitView();
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            LogHelper.d(TAG, "onProgressChanged newProgress: " + newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            LogHelper.d(TAG, message);
            LogHelper.d(TAG, "onJsAlert message: " + message + " url " + url);
            result.confirm();
            return true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (webview != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webview.onPause(); // 暂停网页中正在播放的视频
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当Activity执行onResume()时让WebView执行resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (webview != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webview.onResume();
        }
    }
}
