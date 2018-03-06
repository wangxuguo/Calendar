package com.oceansky.teacher.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.oceansky.teacher.R;
import com.oceansky.teacher.utils.LetvParamsUtils;
import com.oceansky.teacher.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;


public class TeacherDetailActivity extends BaseWebView {
    private final static String TAG = TeacherDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        initView();
    }

    private void initView() {
        mWebView.addJavascriptInterface(new JsInteration(), "controller");
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

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.onPause(); // 暂停网页中正在播放的视频
        }
    }

    //乐视云点播
    private void startLecloudVod(String uid, String vid) {
        Intent intent = new Intent(TeacherDetailActivity.this, PlayActivity.class);
        Bundle bundle = LetvParamsUtils.setVodParams(uid.trim(),
                vid.trim(), "", "802439", "");
        intent.putExtra(PlayActivity.DATA, bundle);
        startActivity(intent);
    }
}
