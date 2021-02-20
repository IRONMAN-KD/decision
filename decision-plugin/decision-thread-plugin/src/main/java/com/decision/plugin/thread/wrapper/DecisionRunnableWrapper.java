package com.decision.plugin.thread.wrapper;

import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;

/**
 * @Author KD
 * @Date 2020/12/21 13:51
 */
public class DecisionRunnableWrapper implements Runnable {
    private Runnable runnable;
    private ContextModel contextModel;

    public DecisionRunnableWrapper(Runnable runnable, ContextModel contextModel) {
        this.runnable = runnable;
        this.contextModel = contextModel;
    }

    @Override
    public void run() {
        DecisionPluginContext.set(contextModel);
        runnable.run();
    }

    /**
     * 返回原始的Runnable
     *
     * @return
     */
    public Runnable getOrigin() {
        return runnable;
    }
}
