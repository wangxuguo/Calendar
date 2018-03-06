package com.oceansky.teacher.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.ProgressBarSubscriber;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.RegexUtil;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class SettingFeedbackActivity extends BaseActivityWithLoadingState {
    private static final String TAG = SettingFeedbackActivity.class.getSimpleName();

    @Bind(R.id.root)
    View rootView;

    @Bind(R.id.feedback_suggestion)
    EditText mEtSuggestion;

    @Bind(R.id.feedback_contact)
    EditText mEtContact;

    private String             mSuggestion;
    private String             mContact;
    private FeedbackSubscriber mFeedbackSubscriber;
    private Subscription       mFeedbackSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting_feedback);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_feedback));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mEtSuggestion.setFilters(new InputFilter[]{new MaxTextLengthFilter(501)});
    }

    @OnClick(R.id.feedback_commit)
    public void commit() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            String str = mEtSuggestion.getText().toString();
            try {
                mSuggestion = new String(str.getBytes(), "UTF-8");
                mContact = mEtContact.getText().toString().trim();
                if (TextUtils.isEmpty(mSuggestion)) {
                    ToastUtil.showToastBottom(this, R.string.toast_feedback_suggestion_empty, Toast.LENGTH_SHORT);
                    return;
                }

                if (!TextUtils.isEmpty(mContact) && !RegexUtil.isPhoneOrQQNumber(mContact)) {
                    ToastUtil.showToastBottom(this, R.string.toast_feedback_number_invalid, Toast.LENGTH_SHORT);
                    return;
                }
                String uid = SecurePreferences.getInstance(this, true).getString(Constants.KEY_USER_ID);
                mFeedbackSubscriber = new FeedbackSubscriber(this);
                mFeedbackSubscription = HttpManager.feedback(mContact, uid, mSuggestion).subscribe(mFeedbackSubscriber);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
        }
    }

    class MaxTextLengthFilter implements InputFilter {
        private int mMaxLength;

        public MaxTextLengthFilter(int max) {
            mMaxLength = max - 1;

        }

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            int keep = mMaxLength - (dest.length() - (dend - dstart));
            if (keep < (end - start)) {
                ToastUtil.showToastBottom(SettingFeedbackActivity.this, R.string.toast_feedback_word_number_limit, Toast.LENGTH_SHORT);
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                return source.subSequence(start, start + keep);
            }
        }
    }

    public class FeedbackSubscriber extends ProgressBarSubscriber<SimpleResponse> {
        public FeedbackSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case "4000":
                    ToastUtil.showToastBottom(mContext,R.string.toast_feedback_number_invalid, Toast.LENGTH_SHORT);
                    break;
                case "4013":
                    showTokenInvalidDialog();
                    break;
                default:
                    ToastUtil.showToastBottom(mContext, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
            ToastUtil.showToastBottom(mContext, R.string.toast_feedback_commit_success, Toast.LENGTH_SHORT);
            finish();
        }

        @Override
        protected String getDialogTextRes() {
            return null;
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            ToastUtil.showToastBottom(mContext, R.string.toast_error_time_out, Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFeedbackSubscriber != null) {
            mFeedbackSubscriber.onCancle();
        }
        if (mFeedbackSubscription != null && !mFeedbackSubscriber.isUnsubscribed()) {
            mFeedbackSubscription.unsubscribe();
        }
    }
}
