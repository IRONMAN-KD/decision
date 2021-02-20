package com.decision.plugin.feign.interceptor;

import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import feign.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class FeignInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        ContextModel contextModel = DecisionPluginContext.getOrCreate();
        Request request = (Request) allArguments[0];
        Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
        if (null == request.headers().get(HeaderKey.VERSION) && null != contextModel.getVdVersion()) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(contextModel.getVdVersion());
            headers.put(HeaderKey.VERSION, headerList);
        }
        if (null == request.headers().get(HeaderKey.ENV) && null != contextModel.getVdEnv()) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(contextModel.getVdEnv());
            headers.put(HeaderKey.ENV, headerList);
        }
        if (null != contextModel.getId()) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(contextModel.getId());
            headers.put(HeaderKey.PID, headerList);
        }
        if (null != contextModel.getTraceId()) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(contextModel.getTraceId());
            headers.put(HeaderKey.TID, headerList);
        }
        if (null != contextModel.getAppNames()) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(contextModel.getAppNames());
            headers.put(HeaderKey.APP_NAMES, headerList);
        }

        Map<String, Collection<String>> oldHeaders = request.headers();
        for (Map.Entry<String, Collection<String>> entry : oldHeaders.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        Request newRequest = Request.create(request.httpMethod(), request.url(), headers, request.body(), request.charset());

        allArguments[0] = newRequest;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }
}
