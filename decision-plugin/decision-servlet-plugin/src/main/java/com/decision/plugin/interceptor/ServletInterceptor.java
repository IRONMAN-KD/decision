package com.decision.plugin.interceptor;

import com.decision.core.plugin.common.GlobalIdGenerator;
import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.context.ServerInfoHolder;
import com.decision.core.plugin.interceptor.AbstractInstanceAroundInterceptor;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author KD
 */
public class ServletInterceptor extends AbstractInstanceAroundInterceptor {
    private Logger logger = getLogger();

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes) {
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
            System.out.println(contextModel.getAppNames());

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
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return null;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
