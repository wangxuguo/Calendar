package com.oceansky.teacher.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.oceansky.teacher.BuildConfig;
import com.oceansky.teacher.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IntroductionActivity extends BaseActivityWithLoadingState {
    @Bind(R.id.introduction_version)
    TextView mTvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_introduction);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_introduce));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        String versionName = BuildConfig.VERSION_NAME;
        mTvVersion.setText(String.format(getString(R.string.introducation_version), versionName));
    }
}
