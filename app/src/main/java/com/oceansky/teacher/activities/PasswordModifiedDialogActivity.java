package com.oceansky.teacher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.oceansky.teacher.R;

public class PasswordModifiedDialogActivity extends Activity implements View.OnClickListener {
    private View     mContentView;
    private TextView mTvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = LayoutInflater.from(this).inflate(R.layout.dialog_alert_custom, null);
        setContentView(mContentView);
        mTvMessage = (TextView) findViewById(R.id.message);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.neutral_btn).setVisibility(View.GONE);
        findViewById(R.id.second_line).setVisibility(View.GONE);
        mTvMessage.setText(R.string.dialog_msg_pwd_modified);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                finishActivity();
                break;
            case R.id.confirm_btn:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finishActivity();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }
}
