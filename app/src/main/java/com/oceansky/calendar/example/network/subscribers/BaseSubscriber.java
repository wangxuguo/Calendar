package com.oceansky.calendar.example.network.subscribers;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.oceansky.calendar.example.constant.Constants;
import com.oceansky.calendar.example.network.http.ApiException;
import com.oceansky.calendar.example.utils.ToastUtil;
import com.oceansky.calendar.example.utils.LogHelper;

import rx.Subscriber;

/**
 * User: dengfa
 * Date: 16/7/18
 * Tel:  18500234565
 */
public abstract class BaseSubscriber<T> extends Subscriber<T> {
    private static final String TAG = BaseSubscriber.class.getSimpleName();
    protected Timer   mTimer;
    protected Context mContext;

    public BaseSubscriber(Context context) {
        mContext = context;
        mTimer = new Timer(Constants.TIME_OUT, Constants.TIMER_INTERVAL);
    }

    class Timer extends CountDownTimer {
        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {
            unsubscribe();
            onTimeout();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示

        }
    }

    protected abstract void onTimeout();

    @Override
    public void onStart() {
        super.onStart();
        mTimer.start();
    }

    @Override
    public void onCompleted() {
        LogHelper.d(TAG, "onCompleted");
        mTimer.cancel();
        LogHelper.d(TAG, "Timer cancle");
    }

    /**
     * 对错误进行统一处理
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        LogHelper.d(TAG, "onError: " + e.getMessage());
        e.printStackTrace();
        mTimer.cancel();
        String message = e.getMessage();
        //有时message会为null，为null时作为默认的异常处理
        if (message != null) {
            switch (message) {
                case ApiException.CLINENT_INVALID:
                    ToastUtil.showToastBottom(mContext, ApiException.getApiExceptionMessage(e.getMessage()), Toast.LENGTH_SHORT);
                    break;
            }
            handleError(e);
        } else {
            handleError(new Throwable(ApiException.ERROR_LOAD_FAIL));
        }
    }

    /**
     * 根据抛出的错误，对错误进行单独处理
     * 复写此方法，在不同的接口中单独处理错误
     *
     * @param e
     */
    protected abstract void handleError(Throwable e);

    public void onCancle() {
        LogHelper.d(TAG, "onCancle");
        mTimer.cancel();
    }
}
