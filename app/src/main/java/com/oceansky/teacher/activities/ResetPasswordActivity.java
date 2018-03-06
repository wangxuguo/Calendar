package com.oceansky.teacher.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.ProgressBarSubscriber;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.StringUtils;
import com.oceansky.teacher.utils.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class ResetPasswordActivity extends BaseActivityWithLoadingState {

    private static final String TAG = "ResetPasswordActivity";

    @Bind(R.id.reset_password_old)
    EditText mEtOldPwd;

    @Bind(R.id.reset_password_new)
    EditText mEtNewPwd;

    @Bind(R.id.root)
    View rootView;

    @Bind(R.id.old_password_clear)
    ImageButton mClearOldPwdImageButton;

    @Bind(R.id.new_password_clear)
    ImageButton mClearNewPwdImageButton;

    private String                mOldPwd;
    private String                mNewPwd;
    private ResetPasswordActivity mContext;
    private ResetPwdSubscriber    mResetPwdSubscriber;
    private Subscription          mResetPwdSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_reset_password);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
    }

    @OnClick(R.id.root)
    public void OnRootClick() {
        mImm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        rootView.requestFocus();
    }

    private void initView() {
        mTitleBar.setTitle(R.string.title_bar_reset_pwd);
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mContext = this;
        mEtOldPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mEtOldPwd.getText())) {
                    mClearOldPwdImageButton.setVisibility(View.GONE);
                } else {
                    mClearOldPwdImageButton.setVisibility(View.VISIBLE);
                }
            }
        });

        mEtNewPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mEtNewPwd.getText())) {
                    mClearNewPwdImageButton.setVisibility(View.GONE);
                } else {
                    mClearNewPwdImageButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @OnClick(R.id.setting_reset_confirm)
    void confirm() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mOldPwd = mEtOldPwd.getText().toString();
            mNewPwd = mEtNewPwd.getText().toString();
            if (TextUtils.isEmpty(mOldPwd)) {
                ToastUtil.showToastBottom(this, R.string.reset_pwd_old_empty, Toast.LENGTH_SHORT);
                return;
            }
            if (TextUtils.isEmpty(mNewPwd)) {
                ToastUtil.showToastBottom(this, R.string.reset_pwd_new_empty, Toast.LENGTH_SHORT);
                return;
            }
            if (!StringUtils.checkPassWord(mNewPwd)) {
                ToastUtil.showToastBottom(this, R.string.reset_pwd_new_not_conform, Toast.LENGTH_SHORT);
                return;
            }
            if (mOldPwd.equals(mNewPwd)) {
                ToastUtil.showToastBottom(this, R.string.reset_pwd_same, Toast.LENGTH_SHORT);
                return;
            }
            String token = "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            String gt_clientID = SharePreferenceUtils.getStringPref(this, Constants.GT_CLIENT_ID, null);
            mResetPwdSubscriber = new ResetPwdSubscriber(mContext);
            mResetPwdSubscription = HttpManager.resetPwd(token, mOldPwd, mNewPwd, gt_clientID).subscribe(mResetPwdSubscriber);
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
        }
    }

    @OnClick(R.id.old_password_clear)
    public void clearOldPwd() {
        mEtOldPwd.setText("");
    }

    @OnClick(R.id.new_password_clear)
    public void clearNewPwd() {
        mEtNewPwd.setText("");
    }

    public class ResetPwdSubscriber extends ProgressBarSubscriber<SimpleResponse> {
        public ResetPwdSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            ToastUtil.showToastBottom(mContext, R.string.toast_error_time_out, Toast.LENGTH_SHORT);
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
            ToastUtil.showToastBottom(mContext, R.string.reset_pwd_success, Toast.LENGTH_SHORT);
            setResult(Activity.RESULT_OK);
            finish();
        }

        @Override
        protected String getDialogTextRes() {
            return getResources().getString(R.string.progress_modifying);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case "4001":
                    ToastUtil.showToastBottom(mContext, R.string.reset_pwd_old_not_match, Toast.LENGTH_SHORT);
                    break;
                case "4013":
                    showTokenInvalidDialog();
                    break;
                default:
                    ToastUtil.showToastBottom(mContext, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mResetPwdSubscriber != null) {
            mResetPwdSubscriber.onCancle();
        }
        if (mResetPwdSubscription != null && !mResetPwdSubscription.isUnsubscribed()) {
            mResetPwdSubscription.unsubscribe();
        }
    }
}
