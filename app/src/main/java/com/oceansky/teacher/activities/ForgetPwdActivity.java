package com.oceansky.teacher.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
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

public class ForgetPwdActivity extends BaseActivityWithLoadingState {

    private final static String TAG                   = ForgetPwdActivity.class.getSimpleName();
    public static final  String ERROR_PHONE_UNREGIRST = "4004";
    public static final  String ERROR_PHONR_INVALID   = "4000";
    public static final  String ERROR_CLIENT_INVALID  = "4012";
    public static final  String ERROR_PHONE_NOT_EXIST = "4004";
    public static final  String ERROR_VERIFY_CODE     = "4002";

    @Bind(R.id.forget_pwd_name)
    EditText mUserName;

    @Bind(R.id.forget_get_verfy_code)
    TextView mGetVerfyCode;

    @Bind(R.id.forget_pwd_verfy_code)
    EditText mVerfyCode;

    @Bind(R.id.reset_password)
    EditText mPassword;

    @Bind(R.id.forget_pwd_btn)
    Button mForgetPwdBtn;

    @Bind(R.id.root)
    View rootView;

    private String               mUserNameStr;
    private String               mPasswordStr;
    private String               mVerfyCodeStr;
    private TimeCount            mTime;
    private modifyPwdSubscriber  mModifyPwdSubscriber;
    private VerifyCodeSubscriber mVerifyCodeSubscriber;
    private Subscription         mVerifyCodeSubscription;
    private Subscription         mModifyPwdSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_forget_password);
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        initView();
        initData();
    }

    @OnClick(R.id.root)
    public void OnRootClick() {
        mImm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        rootView.requestFocus();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_forget_pwd));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
    }

    private void initData() {
        mTime = new TimeCount(60000, 1000);
    }

    @OnClick(R.id.forget_pwd_btn)
    public void forgetPwdBtnOnClick() {

        if (NetworkUtils.isNetworkAvaialble(this)) {
            mUserNameStr = mUserName.getText().toString();
            mPasswordStr = mPassword.getText().toString();
            mVerfyCodeStr = mVerfyCode.getText().toString();

            if (TextUtils.isEmpty(mUserNameStr)) {
                Toast.makeText(this, R.string.toast_check_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!RegexUtil.isMobileNO(mUserNameStr)) {
                    Toast.makeText(this, R.string.toast_check_phone_invalid, Toast.LENGTH_SHORT).show();
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
            mModifyPwdSubscriber = new modifyPwdSubscriber(this);
            mModifyPwdSubscription = HttpManager.forgetPwd(mUserNameStr, mPasswordStr, mVerfyCodeStr).subscribe(mModifyPwdSubscriber);
        } else {
            Toast.makeText(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.forget_get_verfy_code)
    public void OnGetVerfyCodeOnClick() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mUserNameStr = mUserName.getText().toString();
            if (TextUtils.isEmpty(mUserNameStr)) {
                Toast.makeText(this, R.string.toast_check_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!RegexUtil.isMobileNO(mUserNameStr)) {
                    Toast.makeText(this, R.string.toast_check_phone_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mVerifyCodeSubscriber = new VerifyCodeSubscriber(this);
            mVerifyCodeSubscription = HttpManager.getForgetPwdVerifyCode(mUserNameStr).subscribe(mVerifyCodeSubscriber);
        } else {
            Toast.makeText(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mGetVerfyCode.setClickable(true);
            mGetVerfyCode.setText(getString(R.string.resend_seccode));

        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mGetVerfyCode.setClickable(false);
            mGetVerfyCode.setText(millisUntilFinished / 1000 + "");
        }
    }

    public class VerifyCodeSubscriber extends BaseSubscriber<SimpleResponse> {
        public VerifyCodeSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            Toast.makeText(ForgetPwdActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            mTime.start();
            Toast.makeText(ForgetPwdActivity.this, R.string.toast_send_code_success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable e) {
            switch (e.getMessage()) {
                case ERROR_PHONE_UNREGIRST:
                    Toast.makeText(ForgetPwdActivity.this, R.string.toast_phone_unregiste, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_PHONR_INVALID:
                    Toast.makeText(ForgetPwdActivity.this, R.string.toast_phone_invalid, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_CLIENT_INVALID:
                    Toast.makeText(ForgetPwdActivity.this, R.string.toast_client_invalid, Toast.LENGTH_SHORT).show();
                    mTime.cancel();
                    mGetVerfyCode.setText(R.string.btn_get_verifycode);
                    break;
            }
        }

        @Override
        protected void handleError(Throwable e) {

        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            LogHelper.d(TAG, "simpleResponse: " + simpleResponse);
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
            if (FeatureConfig.DEBUG_LOG) {
                ToastUtil.showToastBottom(ForgetPwdActivity.this, "验证码：" + simpleResponse.getMessage(), Toast.LENGTH_LONG);
            }
        }
    }

    public class modifyPwdSubscriber extends ProgressBarSubscriber<SimpleResponse> {
        public modifyPwdSubscriber(Context context) {
            super(context);
        }

        @Override
        protected String getDialogTextRes() {
            return null;
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ERROR_PHONE_NOT_EXIST:
                    Toast.makeText(ForgetPwdActivity.this, R.string.toast_phone_not_exist, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_VERIFY_CODE:
                    Toast.makeText(ForgetPwdActivity.this, R.string.toast_error_verify_code, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            Toast.makeText(ForgetPwdActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            LogHelper.d(TAG, "simpleResponse: " + simpleResponse);
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            Toast.makeText(ForgetPwdActivity.this, R.string.reset_pwd_success, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModifyPwdSubscriber != null) {
            mModifyPwdSubscriber.onCancle();
        }
        if (mVerifyCodeSubscriber != null) {
            mVerifyCodeSubscriber.onCancle();
        }
        if (mVerifyCodeSubscription != null && !mVerifyCodeSubscription.isUnsubscribed()) {
            mVerifyCodeSubscription.unsubscribe();
        }
        if (mModifyPwdSubscription != null && !mModifyPwdSubscription.isUnsubscribed()) {
            mModifyPwdSubscription.unsubscribe();
        }
    }
}
