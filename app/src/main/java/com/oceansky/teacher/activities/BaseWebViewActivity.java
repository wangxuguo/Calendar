package com.oceansky.teacher.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.utils.LetvParamsUtils;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SharePreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * User: 王旭国
 * Date: 16/6/16 17:38
 * Email:wangxuguo@jhyx.com.cn
 */
public class BaseWebViewActivity extends BaseActivityWithLoadingState {
    private final static String TAG = BaseWebViewActivity.class.getSimpleName();

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mIvLoading;

    private WebView           mWebView;
    private String            mUrl;
    private String            mTitle;
    private String            mBtnStr;
    private AnimationDrawable mLoadingAnimation;
    private BaseTimer         mTimer;
    private boolean           isOnPause;

    /* 定义一个倒计时的内部类 */
    class BaseTimer extends CountDownTimer {
        public BaseTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {
            if (!isFinishing()) {
                mLoadingLayout.setVisibility(View.GONE);
                mLoadingAnimation.stop();
            }
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示

        }
    }

    @Override
    protected void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        loadUrl();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basewebview);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initData() {

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "Nautile");
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JsInteration(), "controller");
        webSettings.setSupportZoom(false);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogHelper.d(TAG, "onPageStarted");
                super.onPageStarted(view, url, favicon);
                if (TextUtils.isEmpty(mTitle)) {
                    String viewTitle = view.getTitle();
                    mTitleBar.setTitle(viewTitle);
                }
                mTimer.start();
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingAnimation.start();

                //mWebView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogHelper.d(TAG, "onPageFinished");
                mTimer.cancel();
                if (!isFinishing()) {
                    mErrorLayout.setVisibility(View.GONE);
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                    // mWebView.setVisibility(View.VISIBLE);
                }
                refreshLoadingState(Constants.LOADING_STATE_SUCCESS, true);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                LogHelper.d(TAG, "onPageCommitVisible");
                super.onPageCommitVisible(view, url);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                LogHelper.d(TAG, "onReceivedError");
                super.onReceivedError(view, request, error);
                mTimer.cancel();
                if (!isFinishing()) {
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                LogHelper.d(TAG, "onReceivedSslError");
                mTimer.cancel();
                if (!isFinishing()) {
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                }
                refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.contains("tel"))
                    return false;
                return true;
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
                extraHeaders.put("Accept", Constants.CLINET_ACCEPT);
                extraHeaders.put("client-secret", Constants.CLIENT_SECRET);
                extraHeaders.put("client-id", Constants.CLIENT_ID);
                LogHelper.d(TAG, "extraHeaders: " + extraHeaders.toString());
                LogHelper.d(TAG, "mUrl: " + mUrl);
                mWebView.loadUrl(mUrl, extraHeaders);
            } else {
                refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    private void initView() {
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mWebView = (WebView) findViewById(R.id.webview);
        mUrl = getIntent().getStringExtra(Constants.WEBVIEW_URL);
        mTitle = getIntent().getStringExtra(Constants.WEBVIEW_TITLE);
        mBtnStr = getIntent().getStringExtra(Constants.WEBVIEW_BUTTON);
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleBar.setTitle(mTitle);
        }
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
        mTimer = new BaseTimer(Constants.TIME_OUT, Constants.TIMER_INTERVAL);
    }

    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
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

    public class JsInteration {

        @JavascriptInterface
        public void play(final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.d(TAG, "url: " + url);
                }
            });
            if (!TextUtils.isEmpty(url)) {
                try {
                    JSONObject jsonObject = new JSONObject(url);
                    String uid = jsonObject.getString("uu");
                    String vid = jsonObject.getString("vu");
                    LogHelper.d(TAG, "uid: " + uid + ",vid:" + vid);
                    startLecloudVod(uid, vid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 乐视云点播
     */
    private void startLecloudVod(String uid, String vid) {
        Intent intent = new Intent(BaseWebViewActivity.this, PlayActivity.class);
        LogHelper.d(TAG, "uuid " + uid.trim() + " uvid: " + vid.trim());
        Bundle bundle = LetvParamsUtils.setVodParams(uid.trim(),
                vid.trim(), "", "802439", "");
        intent.putExtra(PlayActivity.DATA, bundle);
        startActivity(intent);
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
