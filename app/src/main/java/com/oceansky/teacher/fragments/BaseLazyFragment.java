package com.oceansky.teacher.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.ToastUtil;
import com.shizhefei.fragment.LazyFragment;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseLazyFragment extends LazyFragment {
    private static final String TAG = BaseLazyFragment.class.getSimpleName();
    protected ImageView      mErrorImg;
    protected TextView       mErrorDesc;
    protected RelativeLayout mErrorLayout;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);

        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mErrorImg = (ImageView) mErrorLayout.findViewById(R.id.error_img);
        mErrorDesc = (TextView) mErrorLayout.findViewById(R.id.error_desc);
        mErrorLayout.setVisibility(View.GONE);
        mErrorLayout.setOnClickListener(null);
    }

    protected void refreshLoadingState(int loadingState, boolean isFirstLoading) {
        switch (loadingState) {
            case Constants.LOADING_STATE_NO_NET:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                if (isFirstLoading) {
                    mErrorImg.setImageResource(R.mipmap.icon_common_wifi);
                    mErrorDesc.setText(R.string.error_msg_no_net);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    ToastUtil.showToastBottom(getActivity(), R.string.toast_error_no_net, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_FAIL:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                if (isFirstLoading) {
                    mErrorImg.setImageResource(R.mipmap.icon_error_load_failure);
                    mErrorDesc.setText(R.string.error_msg_load_failure);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    ToastUtil.showToastBottom(getActivity(), R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_TIME_OUT:
                LogHelper.d(TAG, "isFirstLoading: " + isFirstLoading);
                if (isFirstLoading) {
                    mErrorImg.setImageResource(R.mipmap.icon_error_load_failure);
                    mErrorDesc.setText(R.string.error_msg_load_failure);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mErrorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onErrorLayoutClick();
                        }
                    });
                } else {
                    LogHelper.d(TAG, "getActivity: " + getActivity());
                    ToastUtil.showToastBottom(getActivity(), R.string.toast_error_time_out, Toast.LENGTH_SHORT);
                }
                break;
            case Constants.LOADING_STATE_SUCCESS:
                mErrorLayout.setVisibility(View.GONE);
                mErrorLayout.setOnClickListener(null);
                break;
            case Constants.LOADING_STATE_UNLOGIN:
                mErrorImg.setImageResource(R.mipmap.icon_error_unlogin);
                mErrorDesc.setTextColor(getResources().getColor(R.color.text_gray_deep));
                mErrorDesc.setText(Html.fromHtml(getString(R.string.error_msg_unlogin)));
                mErrorLayout.setVisibility(View.VISIBLE);
                mErrorDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                });
                break;
        }
    }


    abstract void onErrorLayoutClick();

    /**
     * token失效弹框
     */
    protected void showTokenInvalidDialog() {
        String simpleName = this.getClass().getSimpleName();
        LogHelper.d(TAG, "simpleName: " + simpleName);
        CustomDialog.Builder ibuilder = new CustomDialog.Builder(getActivity());
        ibuilder.setTitle(R.string.prompt);
        ibuilder.setMessage(getString(R.string.btn_login_expiry));
        ibuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                intent.putExtra(Constants.CLASS_NAME,simpleName);
//                startActivity(intent);
            }
        });
        ibuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ibuilder.create().show();
    }

    @Override
    protected void onResumeLazy() {
        super.onResumeLazy();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }
}
