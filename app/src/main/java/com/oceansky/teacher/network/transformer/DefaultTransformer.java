package com.oceansky.teacher.network.transformer;

import com.oceansky.teacher.network.response.HttpResponse;

import rx.Observable;

/**
 * User: dengfa
 * Date: 16/7/20
 * Tel:  18500234565
 */
public class DefaultTransformer<T extends HttpResponse<R>, R> implements Observable.Transformer<T, R> {

    @Override
    public Observable<R> call(Observable<T> observable) {
        return observable
                .compose(new DefaultSchedulerTransformer<>())
                .compose(new ErrorCheckTransformer<>());
    }
}