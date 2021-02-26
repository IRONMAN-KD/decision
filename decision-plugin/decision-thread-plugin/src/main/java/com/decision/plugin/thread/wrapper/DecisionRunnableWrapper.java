package com.decision.plugin.thread.wrapper;

import com.decision.core.plugin.common.DecisionThreadLocal;
import com.decision.core.plugin.context.ContextModel;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author KD
 * @Date 2020/12/21 13:51
 */
public class DecisionRunnableWrapper implements Runnable {
    private Runnable runnable;
    private AtomicReference<DecisionThreadLocal.SnapShot> captureRef;

    public DecisionRunnableWrapper(Runnable runnable, ContextModel contextModel) {
        this.runnable = runnable;
        this.captureRef = new AtomicReference<>(DecisionThreadLocal.Transmitter.capture());
    }

    @Override
    public void run() {
        DecisionThreadLocal.SnapShot capture = captureRef.get();
        DecisionThreadLocal.SnapShot backUp = DecisionThreadLocal.Transmitter.replay(capture);
        try {
            runnable.run();
        } finally {
            DecisionThreadLocal.Transmitter.restore(backUp);
        }
    }

}
