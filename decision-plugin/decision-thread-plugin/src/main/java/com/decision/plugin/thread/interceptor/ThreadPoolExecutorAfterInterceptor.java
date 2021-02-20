package com.decision.plugin.thread.interceptor;

import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class ThreadPoolExecutorAfterInterceptor implements InstanceAroundInterceptor {
    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        DecisionPluginContext.clean();
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }

}
