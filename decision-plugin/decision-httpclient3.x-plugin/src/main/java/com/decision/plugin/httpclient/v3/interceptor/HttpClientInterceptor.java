package com.decision.plugin.httpclient.v3.interceptor;

import com.decision.core.plugin.common.StringUtil;
import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.commons.httpclient.HttpMethod;

import java.lang.reflect.Method;

/**
 * @Author KD
 * @Date 2021/2/8 14:26
 */
public class HttpClientInterceptor implements InstanceAroundInterceptor {
    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        HttpMethod req = (HttpMethod) allArguments[1];
        ContextModel contextModel = DecisionPluginContext.getOrCreate();
        req.setRequestHeader(HeaderKey.TID, contextModel.getTraceId());
        req.setRequestHeader(HeaderKey.APP_NAMES, contextModel.getAppNames());
        req.setRequestHeader(HeaderKey.VERSION, contextModel.getVdVersion());
        req.setRequestHeader(HeaderKey.ENV, contextModel.getVdEnv());
        if (StringUtil.isNotEmpty(contextModel.getParentId())) {
            req.setRequestHeader(HeaderKey.PID, contextModel.getParentId());
        }
        allArguments[1] = req;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
