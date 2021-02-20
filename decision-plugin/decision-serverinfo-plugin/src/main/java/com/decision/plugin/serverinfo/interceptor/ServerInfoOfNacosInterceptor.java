package com.decision.plugin.serverinfo.interceptor;

import com.decision.core.plugin.constant.DecisionMateData;
import com.decision.core.plugin.context.ServerInfoHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 获取当前服务的元数据信息
 *
 * @Author KD
 * @Date 2021/2/8 15:25
 */
public class ServerInfoOfNacosInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        Registration registration = (Registration) allArguments[0];
        ServerInfoHolder serverInfo = ServerInfoHolder.getInstance();
        serverInfo.setServerName(registration.getServiceId());
        Map<String, String> metadata = registration.getMetadata();
        if (null != metadata.get(DecisionMateData.VERSION)) {
            serverInfo.setServerVersion(metadata.get(DecisionMateData.VERSION));
        }
        if (null != metadata.get(DecisionMateData.ENV)) {
            serverInfo.setServerEnv(metadata.get(DecisionMateData.ENV));
        }
        logger.info(" get server metadata info success ");
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
