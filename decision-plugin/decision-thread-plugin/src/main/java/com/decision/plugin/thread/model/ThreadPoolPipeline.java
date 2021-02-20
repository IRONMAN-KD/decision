package com.decision.plugin.thread.model;

import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对于线程池上下文传递处理类
 *
 * @Author KD
 * @Date 2020/12/15 15:31
 */
public class ThreadPoolPipeline {
    private static final Map<Runnable, ContextModel> THREAD_POOL_CONTEXTS = new ConcurrentHashMap<Runnable, ContextModel>();

    public static void send(Runnable runnable, ContextModel contextModel) {
        THREAD_POOL_CONTEXTS.put(runnable, contextModel);
    }

    public static void accept(Runnable runnable) {
        ContextModel contextModel = THREAD_POOL_CONTEXTS.remove(runnable);
        if (null != contextModel) {
            DecisionPluginContext.set(contextModel);
        }
    }
}
