package com.decision.plugin.thread.wrapper;


import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;

import java.util.concurrent.Callable;

/**
 * @Author KD
 * @Date 2020/12/21 13:51
 */
public class DecisionCallableWrapper<T> implements Callable<T> {
    private Callable<T> callable;

    private ContextModel contextModel;

    public DecisionCallableWrapper(Callable<T> callable, ContextModel contextModel) {
        this.callable = callable;
        this.contextModel = contextModel;
    }

    @Override
    public T call() throws Exception {
        DecisionPluginContext.set(contextModel);
        return callable.call();
    }

    /**
     * 返回原始的Callable
     *
     * @return
     */
    public Callable<T> getOrigin() {
        return callable;
    }
}
