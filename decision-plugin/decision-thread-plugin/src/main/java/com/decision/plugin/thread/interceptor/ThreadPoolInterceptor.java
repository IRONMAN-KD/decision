package com.decision.plugin.thread.interceptor;

import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.decision.plugin.thread.wrapper.DecisionCallableWrapper;
import com.decision.plugin.thread.wrapper.DecisionForkJoinTaskWrapper;
import com.decision.plugin.thread.wrapper.DecisionRunnableWrapper;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class ThreadPoolInterceptor implements InstanceAroundInterceptor {
    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        Object param = threadPoolHandler(allArguments[0]);
        allArguments[0] = param;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }

    public static Object threadPoolHandler(Object param) {
        if (param instanceof Runnable) {
            DecisionRunnableWrapper runnable = new DecisionRunnableWrapper((Runnable) param);
            return runnable;
        }
        if (param instanceof Callable) {
            DecisionCallableWrapper callable = new DecisionCallableWrapper((Callable) param);
            return callable;
        }
        if (param instanceof ForkJoinTask) {
            DecisionForkJoinTaskWrapper task = new DecisionForkJoinTaskWrapper((ForkJoinTask) param);
            return task;
        }
        return param;
    }

}
