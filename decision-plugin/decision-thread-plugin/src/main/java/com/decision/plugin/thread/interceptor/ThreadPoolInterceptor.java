package com.decision.plugin.thread.interceptor;

import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.decision.plugin.thread.wrapper.DecisionCallableWrapper;
import com.decision.plugin.thread.wrapper.DecisionForkJoinTaskWrapper;
import com.decision.plugin.thread.wrapper.DecisionRunnableWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class ThreadPoolInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        Object param = threadPoolHandler(allArguments[0], parameterTypes[0]);
        allArguments[0] = param;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }

    public Object threadPoolHandler(Object param, Class type) {
        if (type.isAssignableFrom(Runnable.class)) {
            DecisionRunnableWrapper runnable = new DecisionRunnableWrapper((Runnable) param);
            return runnable;
        }
        if (type.isAssignableFrom(Callable.class)) {
            DecisionCallableWrapper callable = new DecisionCallableWrapper((Callable) param);
            return callable;
        }
        if (type.isAssignableFrom(ForkJoinTask.class)) {
            DecisionForkJoinTaskWrapper task = new DecisionForkJoinTaskWrapper((ForkJoinTask) param);
            return task;
        }
        return param;
    }

}
