package com.oceansky.calendar.example.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oceansky.calendar.example.constant.Constants;
import com.oceansky.calendar.example.utils.ToastUtil;
import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.utils.LogHelper;

public abstract class BaseActivityWithLoadingState extends BaseActivity {

    private final static String TAG = BaseActivityWithLoadingState.class.getSimpleName();

    protected ImageView      mErrorImg;
    protected TextView       mErrorDesc;
    protected RelativeLayout mErrorLayout;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mErrorImg = (ImageView) findViewById(R.id.error_img);
        mErrorDesc = (TextView) findViewById(R.id.error_desc);
        mErrorLayout.setVisibility(View.GONE);
        mErrorLayout.setOnClickListener(null);
    }

    protected void refreshLoadingState(int loadingState, boolean isFirstLoading) {
        switch (loadingState) {
            case Constants.LOADING_STATE_NO_NET:
                mErrorImg.setImageResource(R.mipmap.icon_common_wifi);
                mErrorDesc.setText(R.string.error_msg_no_net);
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                if (isFirstLoading) {
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_FAIL:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                mErrorImg.setImageResource(R.mipmap.icon_error_load_failure);
                mErrorDesc.setText(R.string.error_msg_load_failure);
                if (isFirstLoading) {
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    ToastUtil.showToastBottom(this, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_CAN_NOT_VISIT:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                mErrorImg.setImageResource(R.mipmap.icon_cannot_visit);
                mErrorDesc.setText(R.string.error_msg_can_not_visit);
                mErrorLayout.setVisibility(View.VISIBLE);
                break;
            case Constants.LOADING_STATE_TIME_OUT:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                mErrorImg.setImageResource(R.mipmap.icon_error_load_failure);
                mErrorDesc.setText(R.string.error_msg_load_failure);
                if (isFirstLoading) {
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    ToastUtil.showToastBottom(this, R.string.toast_error_time_out, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_SUCCESS:
                mErrorLayout.setVisibility(View.GONE);
                mErrorLayout.setOnClickListener(null);
                break;
            case Constants.LOADING_STATE_HOMEWORK_EMPTY:
                mErrorImg.setImageResource(R.mipmap.icon_homework_select_empty);
                mErrorDesc.setText(R.string.error_msg_load_homework_empty);
                mErrorLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    protected void onErrorLayoutClick() {

    }
}
