package com.oceansky.teacher.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
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

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SharePreferenceUtils;

import java.util.HashMap;
import java.util.Map;


public class BaseWebView extends BaseActivityWithLoadingState {
    private final static String TAG = BaseWebView.class.getSimpleName();

    protected AnimationDrawable mLoadingAnimation;
    protected WebView           mWebView;
    protected FrameLayout       mLoadingLayout;
    protected ImageView         mLoadingImg;

    protected TimeCount mTime;
    protected String    mViewTitle;
    protected String    mTitle; // 标题
    protected String    mUrl; // url
    protected boolean   isPageLoaded;
    protected boolean   mIsCannotVisit;
    private   boolean   isOnPause;

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            LogHelper.d(TAG, "onFinish");
            if (!isFinishing()) {
                isPageLoaded = true;
                mLoadingLayout.setVisibility(View.GONE);
                mLoadingAnimation.stop();
            }
            refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示

        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mUrl = getIntent().getStringExtra(Constants.WEBVIEW_URL);
        mTitle = getIntent().getStringExtra(Constants.WEBVIEW_TITLE);
        mWebView = (WebView) findViewById(R.id.webview);
        mLoadingLayout = (FrameLayout) findViewById(R.id.loading_layout);
        mLoadingImg = (ImageView) findViewById(R.id.loading);
        //加载动画
        mLoadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImg.getDrawable();
        mTime = new TimeCount(Constants.TIME_OUT, Constants.TIMER_INTERVAL);
    }

    private void initData() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "Nautile");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogHelper.d(TAG, "-----------------onPageStarted----------------------");
                if (TextUtils.isEmpty(mTitle)) {
                    mViewTitle = view.getTitle();
                    mTitleBar.setTitle(mViewTitle);
                }
                isPageLoaded = false;
            }

            @Override
            public void onReceivedHttpError(final WebView view, final WebResourceRequest request, WebResourceResponse errorResponse) {
                final int statusCode;
                // SDK < 21 does not provide statusCode
                if (Build.VERSION.SDK_INT < 21) {
                    statusCode = 1000;
                } else {
                    statusCode = errorResponse.getStatusCode();
                }
                LogHelper.d(TAG, "[onReceivedHttpError]" + statusCode);
                if (statusCode == 404) {
                    refreshLoadingState(Constants.LOADING_STATE_CAN_NOT_VISIT, true);
                    mIsCannotVisit = true;
                    LogHelper.d(TAG, "------------set 404 page-----------");
                } else {
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    mIsCannotVisit = false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogHelper.d(TAG, "-----------------onPageFinished----------------------");
                if (TextUtils.isEmpty(mTitle)) {
                    mTitleBar.setTitle(view.getTitle());
                }
                if (!isFinishing() && !isPageLoaded) {
                    mTime.cancel();
                    isPageLoaded = false;
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                //处理某些特殊情况,所有的回调方法检测不到错误的情况下
                if ((view.getTitle().contains("ocean-sky") || view.getTitle().contains("Error")) && !mIsCannotVisit) {
                    LogHelper.d(TAG, view.getTitle() + "---------------onPageCommitVisible Error-----------");
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    mIsCannotVisit = false;
                }

                mWebView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                LogHelper.d(TAG, "-----------------onPageCommitVisible----------------------");
                if (!isFinishing()) {
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                mTime.cancel();
                //处理某些特殊情况,所有的回调方法检测不到错误的情况下
                if ((view.getTitle().contains("ocean-sky") || view.getTitle().contains("Error")) && !mIsCannotVisit) {
                    LogHelper.d(TAG, view.getTitle() + "---------------onPageCommitVisible Error-----------");
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    mIsCannotVisit = false;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                LogHelper.d(TAG, "-----------------onReceivedError----------------------");
                mTime.cancel();
                isPageLoaded = false;
                if (!isFinishing()) {
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int status = error.getErrorCode();
                    if (status == 404) {
                        refreshLoadingState(Constants.LOADING_STATE_CAN_NOT_VISIT, true);
                    } else {
                        refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    }
                } else {
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
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
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                int status = error.getPrimaryError();
                if (status == 404) {
                    refreshLoadingState(Constants.LOADING_STATE_CAN_NOT_VISIT, true);
                } else {
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.contains("tel"))
                    return false;
                loadUrl();
                return true;
            }
        });
        loadUrl();
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleBar.setTitle(mTitle);
        }
    }

    @Override
    protected void onErrorLayoutClick() {
        super.onErrorLayoutClick();
        mErrorLayout.setVisibility(View.INVISIBLE);
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
                    isPageLoaded = true;
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    mLoadingAnimation.start();
                }
                mWebView.loadUrl(mUrl, extraHeaders);
            } else {
                refreshLoadingState(Constants.LOADING_STATE_CAN_NOT_VISIT, true);
            }
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            LogHelper.d(TAG, message);
            result.confirm();
            return true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.onPause();
        }
    }
}
