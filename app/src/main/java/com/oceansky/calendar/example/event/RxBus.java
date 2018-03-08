package com.oceansky.example.event;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.thread.ThreadEnforcer;

/**
 * User: dengfa
 * Date: 16/8/22
 * Tel:  18500234565
 */
public class RxBus {
    private static volatile Bus mInstance;
    public static Bus getInstance() {
        if (mInstance == null) {
            synchronized (RxBus.class) {
                if (mInstance == null) {
                    mInstance = new Bus(ThreadEnforcer.ANY);
                }
            }
        }
        return mInstance;
    }
}
