package com.oceansky.teacher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;

public class TokenInvalidDialogActivity extends Activity implements View.OnClickListener {
    private TextView mTvMessage;
    private String   mSimpleClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert_custom);
        initView();
        initData();
    }

    private void initView() {
        mTvMessage = (TextView) findViewById(R.id.message);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.neutral_btn).setVisibility(View.GONE);
        findViewById(R.id.second_line).setVisibility(View.GONE);
        mTvMessage.setText(R.string.btn_login_expiry);
    }

    private void initData() {
        mSimpleClassName = getIntent().getStringExtra(Constants.CLASS_NAME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                finishActivity();
                break;
            case R.id.confirm_btn:
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constants.CLASS_NAME, mSimpleClassName);
                startActivity(intent);
                finishActivity();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finishActivity();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }
}
