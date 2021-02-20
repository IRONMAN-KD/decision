package com.decision.plugin.thread.interceptor;

import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.decision.plugin.thread.model.ThreadPoolPipeline;

import java.lang.reflect.Method;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class ThreadPoolExecutorBeforeInterceptor implements InstanceAroundInterceptor {

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        if (allArguments != null
                && allArguments.length > 1
                && allArguments[1] instanceof Runnable) {
            Runnable r = (Runnable) allArguments[1];
            //将 context 存放到 ThreadLocal 中
            ThreadPoolPipeline.accept(r);

        }
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }

}
