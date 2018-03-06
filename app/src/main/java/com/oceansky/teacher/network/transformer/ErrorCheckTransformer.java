package com.oceansky.teacher.network.transformer;

import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.response.HttpResponse;

import rx.Observable;

/**
 * User: dengfa
 * Date: 16/7/20
 * Tel:  18500234565
 */
public class ErrorCheckTransformer<T extends HttpResponse<R>, R> implements Observable.Transformer<T, R> {
    public static final int SUCCESS_CODE = 200;

    @Override
    public Observable<R> call(Observable<T> observable) {
        return observable.map(httpResponse -> {
            if (httpResponse.getCode() != SUCCESS_CODE) {
                throw (new ApiException(httpResponse.getCode() + ""));
            }
            return httpResponse.getData();
        });
    }
}