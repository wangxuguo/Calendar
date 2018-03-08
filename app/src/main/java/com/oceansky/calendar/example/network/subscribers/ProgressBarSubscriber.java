package com.oceansky.calendar.example.network.subscribers;

import android.content.Context;

import com.oceansky.calendar.example.customviews.CustomProgressDialog;

/**
 * User: dengfa
 * Date: 16/7/20
 * Tel:  18500234565
 * Des:  带ProgressBar的Subscriber
 */
public abstract class ProgressBarSubscriber<T> extends BaseSubscriber<T> {

    private CustomProgressDialog mDialog;

    public ProgressBarSubscriber(Context context) {
        super(context);
        mDialog = CustomProgressDialog.createDialog(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgress();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        hideProgress();
    }

    @Override
    public void onCompleted() {
        hideProgress();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    protected void onTimeout() {
        hideProgress();
    }

    protected abstract String getDialogTextRes();

    private void showProgress() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void hideProgress() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onCancle() {
        super.onCancle();
        hideProgress();
    }
}
