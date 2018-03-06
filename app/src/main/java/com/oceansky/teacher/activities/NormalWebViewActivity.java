package com.oceansky.teacher.activities;

import android.os.Bundle;

import com.oceansky.teacher.R;


public class NormalWebViewActivity extends BaseWebView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
    }
}
