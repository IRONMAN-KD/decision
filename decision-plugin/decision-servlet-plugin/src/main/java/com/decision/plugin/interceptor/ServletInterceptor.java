package com.decision.plugin.interceptor;

import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;

import java.lang.reflect.Method;


/**
 * 注意：实例方法使用@Advice.This注解，静态方法使用@Advice.Origin 两者不能混用
 *
 * @author yuan
 * @date 2018/08/05
 */
public class ServletInterceptor implements InstanceAroundInterceptor {


    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes) {

    }

    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return null;
    }

    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
