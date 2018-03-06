package com.oceansky.teacher.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.oceansky.teacher.AndroidApplication;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.event.LoginSuccessEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.manager.AcountManager;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.LoginEntity;
import com.oceansky.teacher.network.subscribers.ProgressBarSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.RegexUtil;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class LoginActivity extends BaseActivityWithLoadingState {

    private final static String TAG         = "LoginActivity";
    public static        int    REQUST_CODE = 3000;

    @Bind(R.id.login_name)
    EditText mUserName;

    @Bind(R.id.login_password)
    EditText mPassword;

    @Bind(R.id.login_forget_password)
    TextView mForgetPassword;

    @Bind(R.id.login_register)
    TextView mRegister;

    @Bind(R.id.login_btn)
    Button mLoginBtn;

    @Bind(R.id.login_password_clear)
    ImageButton mClearPwdImageButton;

    @Bind(R.id.root)
    View rootView;

    private String          mUserNameStr;
    private String          mPasswordStr;
    private Context         mContext;
    private String          mRequest_code;
    private LoginSubscriber mLoginSubscriber;
    private Subscription    mLoginSubscription;
    private String          mSimpleClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        mContext = LoginActivity.this;
        initView();
        initData();
    }

    protected void setStatusBar() {

    }

    @OnClick(R.id.root)
    public void OnRootClick() {
        mImm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        rootView.requestFocus();
    }

    private void initView() {
        mTitleBar.setBackButton(R.mipmap.icon_back_blue, this);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.caldroid_transparent));
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mPassword.getText())) {
                    mClearPwdImageButton.setVisibility(View.GONE);
                } else {
                    mClearPwdImageButton.setVisibility(View.VISIBLE);
                }
            }
        });
        mRequest_code = getIntent().getStringExtra(Constants.REQUEST_CODE);
    }

    private void initData() {
        mSimpleClassName = getIntent().getStringExtra(Constants.CLASS_NAME);
    }

    @OnClick(R.id.login_btn)
    public void loginBtnOnClick() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mUserNameStr = mUserName.getText().toString();
            mPasswordStr = mPassword.getText().toString();
            if (TextUtils.isEmpty(mUserNameStr)) {
                Toast.makeText(this, R.string.toast_check_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!RegexUtil.isMobileNO(mUserNameStr)) {
                    Toast.makeText(this, R.string.toast_check_phone_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (TextUtils.isEmpty(mPasswordStr)) {
                Toast.makeText(this, R.string.toast_check_pwd_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            LogHelper.d(TAG, "HttpManager.login");
            login();
        } else {
            Toast.makeText(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        String cid = SharePreferenceUtils.getStringPref(mContext, Constants.GT_CLIENT_ID, "");
        mLoginSubscriber = new LoginSubscriber(mContext);
        mLoginSubscription = HttpManager.login(cid, mUserNameStr, mPasswordStr).subscribe(mLoginSubscriber);
    }

    @OnClick(R.id.login_forget_password)
    public void forgetPwdOnClick() {
        startActivity(new Intent(this, ForgetPwdActivity.class));
    }

    @OnClick(R.id.login_register)
    public void registerOnClick() {
        startActivityForResult(new Intent(this, RegisterActivity.class), REQUST_CODE);
    }

    @OnClick(R.id.login_password_clear)
    public void clearPwdOnClick() {
        mPassword.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUST_CODE) {
            AndroidApplication.isLogined = true;
            setResult(Activity.RESULT_OK);
            Intent intent = new Intent(Constants.LOGIN_SUCCESS_BROADCAST);
            sendBroadcast(intent);
            finish();
        }
    }

    class LoginSubscriber extends ProgressBarSubscriber<LoginEntity> {

        public LoginSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.PWD_OR_USERBANE_ERROR:
                    ToastUtil.showToastBottom(mContext, ApiException.getApiExceptionMessage(e.getMessage()), Toast.LENGTH_SHORT);
                    break;
                default:
                    ToastUtil.showToastBottom(mContext, ApiException.getApiExceptionMessage(e.getMessage()), Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void onNext(LoginEntity loginEntity) {
            LogHelper.d(TAG, "LoginEntity: " + loginEntity);
            //Toast.makeText(mContext, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
            if (mSimpleClassName != null) {
                RxBus.getInstance().post(new LoginSuccessEvent(mSimpleClassName));
            }
            AcountManager.saveAcountInfo(mContext, loginEntity);
            AndroidApplication.isLogined = true;
            if (loginEntity != null) {
                int uid = loginEntity.getUser_id();
                LogHelper.d(TAG, "uid:" + uid);
                if (!TextUtils.isEmpty(uid + "")) {
                    PushManager pushManager = PushManager.getInstance();
                    pushManager.bindAlias(mContext, uid + "");
                }
            }
            Intent intent = new Intent(Constants.LOGIN_SUCCESS_BROADCAST);
            mContext.sendBroadcast(intent);
            if (mRequest_code != null) {
                switch (mRequest_code) {
                    case Constants.REQUEST_MSG:
                        intent = new Intent(Constants.LOGIN_SUCCESS_MSG_BROADCAST);
                        mContext.sendBroadcast(intent);
                        LogHelper.d(TAG, "send LOGIN_SUCCESS_MSG_BROADCAST");
                        break;
                }
            }
            ((Activity) mContext).setResult(Activity.RESULT_OK);
            ((Activity) mContext).finish();
        }

        @Override
        protected String getDialogTextRes() {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginSubscriber != null) {
            mLoginSubscriber.onCancle();
        }
        if (mLoginSubscription != null && !mLoginSubscription.isUnsubscribed()) {
            mLoginSubscription.unsubscribe();
        }
    }
}
