package com.decision.core.plugin.interceptor;

import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

/**
 * 方法拦截处理接口
 *
 * @Author KD
 * @Date 2021/2/1 17:28
 */
public interface InstanceAroundInterceptor {
    /**
     * 方法执行之前
     *
     * @param targetObject
     * @param method
     * @param allArguments
     * @param parameterTypes
     * @param result
     */
    void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result);

    /**
     * 方法执行后
     *
     * @param targetObject
     * @param method
     * @param allArguments
     * @param result
     * @param argumentsTypes
     * @return
     */
    Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes);

    /**
     * 方法异常处理
     *
     * @param targetObject
     * @param method
     * @param allArguments
     * @param argumentsTypes
     * @param t
     */
    void handleException(Object targetObject, Method method, Object[] allArguments,
                         Class<?>[] argumentsTypes, Throwable t);
}
