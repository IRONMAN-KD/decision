package com.decision.plugin.headerinfo.interceptor;

import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

/**
 * @Author linkedong@vv.cn
 * @Date 2021/4/23 15:30
 */
public class NacosHeaderInfoInterceptor extends HeaderInfoInterceptor {
    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        handleFirstTime(allArguments[0]);
        return result;
    }
}
