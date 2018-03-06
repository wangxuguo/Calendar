package com.oceansky.teacher.network.subscribers;

import android.content.Context;

import com.oceansky.teacher.utils.LogHelper;

/**
 * User: dengfa
 * Date: 16/7/27
 * Tel:  18500234565
 * Des:  带Loading的Subscriber
 */
public abstract class LoadingSubscriber<T> extends BaseSubscriber<T> {

    private static final String TAG = LoadingSubscriber.class.getSimpleName();

    public LoadingSubscriber(Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        showLoading();
    }

    @Override
    public void onError(Throwable e) {
        dismissLoading();
        super.onError(e);
    }

    @Override
    public void onCompleted() {
        super.onCompleted();
        dismissLoading();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    protected void onTimeout() {
        LogHelper.d(TAG, "timeout");
        dismissLoading();
    }

    protected abstract void showLoading();

    protected abstract void dismissLoading();

    @Override
    public void onCancle() {
        super.onCancle();
        dismissLoading();
    }
}
