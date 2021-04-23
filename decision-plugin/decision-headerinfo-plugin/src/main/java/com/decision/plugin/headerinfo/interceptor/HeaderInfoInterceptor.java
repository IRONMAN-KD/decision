package com.decision.plugin.headerinfo.interceptor;

import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.context.HeaderConfigHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author KD
 * @Date 2021/2/8 14:26
 */
public class HeaderInfoInterceptor implements InstanceAroundInterceptor {
    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        handleFirstTime(allArguments[0]);
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        handleChange(result);
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

    public void handleFirstTime(Object argument) {
        HeaderConfigHolder headerConfigHolder = HeaderConfigHolder.getInstance();
        Map<String, Object> config = (Map<String, Object>) argument;
        if (null == headerConfigHolder.getVersion() || headerConfigHolder.getVersion().size() == 0) {
            setHeaderConfig(config, true, false);
        }
        if (null == headerConfigHolder.getEnv() || headerConfigHolder.getEnv().size() == 0) {
            setHeaderConfig(config, false, true);
        }
    }

    public void handleChange(Object argument) {
        Map<String, Object> changedConfig = (Map<String, Object>) argument;
        setHeaderConfig(changedConfig, true, true);
    }

    public void setHeaderConfig(Map<String, Object> configs, Boolean isSetVersion, Boolean isSetEnv) {
        HeaderConfigHolder headerConfigHolder = HeaderConfigHolder.getInstance();
        for (String configKey : configs.keySet()) {
            if (configKey.contains(DecisionConstant.DECISION_HEADER_VERSION) && isSetVersion) {
                Map<String, String> versions = headerConfigHolder.getVersion();
                String hostKey = configKey.substring(DecisionConstant.DECISION_HEADER_VERSION.length() + 4);
                versions.put(hostKey, null == configs.get(configKey) ? null : configs.get(configKey).toString());
            }
            if (configKey.contains(DecisionConstant.DECISION_HEADER_ENV) && isSetEnv) {
                Map<String, String> envs = headerConfigHolder.getEnv();
                String hostKey = configKey.substring(DecisionConstant.DECISION_HEADER_ENV.length() + 4);
                envs.put(hostKey, null == configs.get(configKey) ? null : configs.get(configKey).toString());
            }
        }
    }
}
