package com.oceansky.teacher.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.event.ModifyHomeworkTitleEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class ModifyHomeworkTitleActivity extends BaseActivityWithLoadingState {
    private static final String TAG = ModifyHomeworkTitleActivity.class.getSimpleName();

    @Bind(R.id.hw_et_title)
    EditText    mEtTitle;
    @Bind(R.id.loading)
    ImageView   mIvLoading;
    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    private CustomProgressDialog     mDialog;
    private String                   mHomeworkTitle;
    private int                      mHomeworkId;
    private ModifyHomeworkSubscriber mModifyHomeworkSubscriber;
    private Subscription             mModifyHomeworkSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_homework_title);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_homework_title));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTitleBar.setTvSettingVisibility(true);
        mDialog = CustomProgressDialog.createDialog(this);
    }

    private void initData() {
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        mEtTitle.setText(mHomeworkTitle);
        mEtTitle.setSelection(mHomeworkTitle.length());
    }

    @OnClick(R.id.tv_setting)
    public void save() {
        mHomeworkTitle = mEtTitle.getText().toString().trim();
        if (TextUtils.isEmpty(mHomeworkTitle)) {
            ToastUtil.showToastBottom(this, R.string.toast_homework_title_empty, Toast.LENGTH_SHORT);
            return;
        }
        saveHomeworkHttp(mHomeworkTitle);
    }

    private void saveHomeworkHttp(String title) {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mModifyHomeworkSubscriber = new ModifyHomeworkSubscriber(this);
            mModifyHomeworkSubscription = HttpManager.modifyHomework(token, mHomeworkId, title)
                    .subscribe(mModifyHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    private class ModifyHomeworkSubscriber extends LoadingSubscriber<SimpleResponse> {

        public ModifyHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
        }

        @Override
        protected void showLoading() {
            mDialog.show();
        }

        @Override
        protected void dismissLoading() {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, false);
            }
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            RxBus.getInstance().post(new RefreshUnassignedHomeworkListEvent());
            RxBus.getInstance().post(new ModifyHomeworkTitleEvent(mHomeworkTitle));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mModifyHomeworkSubscriber != null) {
            mModifyHomeworkSubscriber.onCancle();
        }
        if (mModifyHomeworkSubscription != null && !mModifyHomeworkSubscription.isUnsubscribed()) {
            mModifyHomeworkSubscription.unsubscribe();
        }
    }
}
