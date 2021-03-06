package com.decision.plugin.servlet.interceptor;

import com.decision.core.plugin.common.GlobalIdGenerator;
import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.context.ServerInfoHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author KD
 */
public class ServletInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        HttpServletRequest request = (HttpServletRequest) allArguments[0];
        String version = request.getHeader(HeaderKey.VERSION);
        String env = request.getHeader(HeaderKey.ENV);
        String traceId = request.getHeader(HeaderKey.TID);
        String parentId = request.getHeader(HeaderKey.PID);
        String appNames = request.getHeader(HeaderKey.APP_NAMES);
        ContextModel contextModel = DecisionPluginContext.create();
        ServerInfoHolder serverInfo = ServerInfoHolder.getInstance();
        if (null == traceId) {
            traceId = GlobalIdGenerator.generate();
        }
        contextModel.setTraceId(traceId);
        if (null != parentId) {
            if (null == contextModel.getParentId() || !parentId.equals(contextModel.getId())) {
                DecisionPluginContext.appendAppNames(appNames, serverInfo.getServerName(), serverInfo.getServerVersion());
            }
            //打印经过的链路信息
            logger.warn(contextModel.getAppNames());

        } else {
            contextModel.setAppNames(DecisionConstant.DECISION_TRACE + "[" + traceId + "]:" + serverInfo.getServerName());
        }
        contextModel.setParentId(contextModel.getId());
        if (null != version) {
            contextModel.setVdVersion(version);
        }
        if (null != env) {
            contextModel.setVdEnv(env);
        }
        allArguments[0] = request;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
