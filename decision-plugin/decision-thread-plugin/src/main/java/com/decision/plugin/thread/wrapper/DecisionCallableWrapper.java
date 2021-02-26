package com.decision.plugin.thread.wrapper;


import com.decision.core.plugin.common.DecisionThreadLocal;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author KD
 * @Date 2020/12/21 13:51
 */
public class DecisionCallableWrapper<T> implements Callable<T> {
    private Callable<T> callable;

    private AtomicReference<DecisionThreadLocal.SnapShot> captureRef;

    public DecisionCallableWrapper(Callable<T> callable) {
        this.callable = callable;
        this.captureRef = new AtomicReference<>(DecisionThreadLocal.Transmitter.capture());
    }

    @Override
    public T call() throws Exception {
        DecisionThreadLocal.SnapShot capture = captureRef.get();
        DecisionThreadLocal.SnapShot backUp = DecisionThreadLocal.Transmitter.replay(capture);
        T result;
        try {
            result = callable.call();
        } finally {
            DecisionThreadLocal.Transmitter.restore(backUp);
        }
        return result;
    }

}
