package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.manager.AcountManager;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.RegisterEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.BaseSubscriber;
import com.oceansky.teacher.network.subscribers.ProgressBarSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.RegexUtil;
import com.oceansky.teacher.utils.StringUtils;
import com.oceansky.teacher.utils.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class RegisterActivity extends BaseActivityWithLoadingState {

    private final static String TAG                          = "RegisterActivity";
    private static final String ERROR_PHONE_REGISTERED       = "6014";
    private static final String ERROR_PHONE_INVALID          = "4000";
    public static final  String ERROR_VERIFY_CODE            = "4002";
    public static final  String ERROR_REGISTER_PHONE_INVALID = "4015";
    public static final  String ERROR_PWD_INVALID            = "4016";

    @Bind(R.id.register_name)
    EditText mEtUserName;

    @Bind(R.id.register_get_verfy_code)
    TextView mTvGetVerfyCode;

    @Bind(R.id.register_input_verfy_code)
    EditText mEtVerfyCode;

    @Bind(R.id.register_set_password)
    EditText mEtPassword;

    @Bind(R.id.register_checkbox)
    CheckBox mCheckBox;

    @Bind(R.id.login_text)
    TextView mTvLogin;

    @Bind(R.id.register_btn)
    Button mBtnRegister;

    @Bind(R.id.usr_agreement)
    TextView mTvAgreement;

    @Bind(R.id.root)
    View rootView;

    private String                  mPhoneNumbeStr;
    private String                  mPasswordStr;
    private String                  mVerfyCodeStr;
    private TimeCount               mTime;
    private GetVerifyCodeSubscriber mGetVerifyCodeSubscriber;
    private RegisterSubscriber      mRegisterSubscriber;
    private Subscription            mRegisterSubscription;
    private Subscription            mGetVerifyCodesSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_register);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_register));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTvAgreement.setText(Html.fromHtml(getString(R.string.user_agreement, "《用户协议》")));
    }

    private void initData() {
        mTime = new TimeCount(60000, 1000);
    }

    @OnClick(R.id.register_btn)
    public void registerBtnOnClick() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mPhoneNumbeStr = mEtUserName.getText().toString();
            mPasswordStr = mEtPassword.getText().toString();
            mVerfyCodeStr = mEtVerfyCode.getText().toString();

            if (TextUtils.isEmpty(mPhoneNumbeStr)) {
                Toast.makeText(this, R.string.toast_check_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!RegexUtil.isMobileNO(mPhoneNumbeStr)) {
                    Toast.makeText(this, R.string.toast_phone_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (TextUtils.isEmpty(mVerfyCodeStr)) {
                Toast.makeText(this, R.string.toast_check_code_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(mPasswordStr)) {
                Toast.makeText(this, R.string.toast_check_pwd_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!StringUtils.checkPassWord(mPasswordStr)) {
                Toast.makeText(this, R.string.toast_check_pwd_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!mCheckBox.isChecked()) {
                Toast.makeText(this, R.string.toast_check_agreement, Toast.LENGTH_SHORT).show();
                return;
            }
            mRegisterSubscriber = new RegisterSubscriber(this);
            mRegisterSubscription = HttpManager.register(mPhoneNumbeStr, mPasswordStr, mVerfyCodeStr).subscribe(mRegisterSubscriber);
        } else {
            Toast.makeText(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.root)
    public void OnRootClick() {
        mImm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        rootView.requestFocus();
    }

    @OnClick(R.id.usr_agreement)
    public void onAgreementClick() {
        Intent it = new Intent(this, BaseWebViewActivity.class);
        it.putExtra(Constants.WEBVIEW_URL, Constants.USR_AGREEMENT_URL);
        it.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.title_agreement));
        startActivity(it);
    }

    @OnClick(R.id.register_get_verfy_code)
    public void onGetVerfyCodeBtnClick() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mPhoneNumbeStr = mEtUserName.getText().toString();
            if (TextUtils.isEmpty(mPhoneNumbeStr)) {
                Toast.makeText(this, R.string.toast_check_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!RegexUtil.isMobileNO(mPhoneNumbeStr)) {
                    Toast.makeText(this, R.string.toast_phone_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            getVerfyCode(mPhoneNumbeStr);
        } else {
            Toast.makeText(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.login_text)
    public void loginOnClick() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mTvGetVerfyCode.setClickable(true);
            mTvGetVerfyCode.setText(getString(R.string.resend_seccode));
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mTvGetVerfyCode.setClickable(false);
            mTvGetVerfyCode.setText(millisUntilFinished / 1000 + "");
        }

    }

    private void getVerfyCode(String phoneNumber) {
        mGetVerifyCodeSubscriber = new GetVerifyCodeSubscriber(this);
        mGetVerifyCodesSubscription = HttpManager.getVerifyCode(phoneNumber).subscribe(mGetVerifyCodeSubscriber);
    }

    class GetVerifyCodeSubscriber extends BaseSubscriber<SimpleResponse> {
        public GetVerifyCodeSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            Toast.makeText(RegisterActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ERROR_PHONE_REGISTERED:
                    Toast.makeText(RegisterActivity.this, R.string.toast_phone_registered, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_PHONE_INVALID:
                    Toast.makeText(RegisterActivity.this, R.string.toast_phone_invalid, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(RegisterActivity.this, R.string.toast_retrive_verify_code, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            LogHelper.d(TAG, "verifyCodeBean: " + simpleResponse);
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
            if (FeatureConfig.DEBUG_LOG) {
                ToastUtil.showToastBottom(RegisterActivity.this, "验证码：" + simpleResponse.getMessage(), Toast.LENGTH_LONG);
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            mTime.start();
            Toast.makeText(RegisterActivity.this, R.string.toast_send_code_success, Toast.LENGTH_SHORT).show();
        }
    }

    class RegisterSubscriber extends ProgressBarSubscriber<RegisterEntity> {

        public RegisterSubscriber(Context context) {
            super(context);
        }

        @Override
        protected String getDialogTextRes() {
            return null;
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            Toast.makeText(RegisterActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ERROR_VERIFY_CODE:
                    Toast.makeText(RegisterActivity.this, R.string.toast_error_verify_code, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_REGISTER_PHONE_INVALID:
                    Toast.makeText(RegisterActivity.this, R.string.toast_check_phone_invalid, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_PWD_INVALID:
                    Toast.makeText(RegisterActivity.this, R.string.toast_check_pwd_invalid, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onNext(RegisterEntity registerEntity) {
            LogHelper.d(TAG, "registerEntity: " + registerEntity);
            AcountManager.saveAcountInfo(RegisterActivity.this, registerEntity);
            Toast.makeText(RegisterActivity.this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGetVerifyCodeSubscriber != null) {
            mGetVerifyCodeSubscriber.onCancle();
        }
        if (mRegisterSubscriber != null) {
            mRegisterSubscriber.onCancle();
        }
        if (mRegisterSubscription != null && !mRegisterSubscription.isUnsubscribed()) {
            mRegisterSubscription.unsubscribe();
        }
        if (mGetVerifyCodesSubscription != null && !mGetVerifyCodesSubscription.isUnsubscribed()) {
            mGetVerifyCodesSubscription.unsubscribe();
        }
    }
}