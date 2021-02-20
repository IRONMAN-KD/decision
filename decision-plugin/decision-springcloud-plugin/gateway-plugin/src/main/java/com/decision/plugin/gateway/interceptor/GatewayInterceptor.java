package com.decision.plugin.gateway.interceptor;

import com.decision.core.plugin.common.GlobalIdGenerator;
import com.decision.core.plugin.common.StringUtil;
import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.context.HeaderConfigHolder;
import com.decision.core.plugin.context.ServerInfoHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class GatewayInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        ServerWebExchange request = (ServerWebExchange) allArguments[0];
        HttpHeaders headers = request.getRequest().getHeaders();
        List<String> versions = headers.get(HeaderKey.VERSION);
        List<String> envs = headers.get(HeaderKey.ENV);
        List<String> traceIds = headers.get(HeaderKey.TID);
        List<String> parentIds = headers.get(HeaderKey.PID);
        HeaderConfigHolder headerConfig = HeaderConfigHolder.getInstance();
        Map<String, String> versionMap = headerConfig.getVersion();
        Map<String, String> envMap = headerConfig.getEnv();
        String host = request.getRequest().getURI().getHost();
        String version = versionMap.get(host);
        String env = envMap.get(host);
        //如果请求header中有传version和env的话，优先使用透传的，如果没有则使用配置中心配置的
        if (null != versions && versions.size() > 0) {
            version = versions.get(0);
        }
        if (null != envs && envs.size() > 0) {
            env = envs.get(0);
        }
        ContextModel contextModel = DecisionPluginContext.create();
        ServerInfoHolder serverInfo = ServerInfoHolder.getInstance();
        if (StringUtil.isNotEmpty(version)) {
            logger.debug("gateway trace version: {}", version);
            contextModel.setVdVersion(version);
        }
        if (StringUtil.isNotEmpty(env)) {
            logger.debug("gateway trace env: {}", env);
            contextModel.setVdEnv(env);
        }
        //创建traceId
        String traceId = GlobalIdGenerator.generate();
        if (null != traceIds && traceIds.size() > 0) {
            logger.debug("gateway trace traceId: {}", traceIds.get(0));
            traceId = traceIds.get(0);
        }
        contextModel.setTraceId(traceId);
        contextModel.setAppNames(DecisionConstant.DECISION_TRACE + "[" + traceId + "]: " + serverInfo.getServerName());
        if (null != parentIds && parentIds.size() > 0) {
            logger.debug("gateway trace parentId: {}", parentIds.get(0));
            contextModel.setParentId(parentIds.get(0));
        }
        ServerHttpRequest noModifyRequest = request.getRequest();
        ServerHttpRequest newRequest = noModifyRequest.mutate().headers(httpHeaders -> {
            if (null != contextModel.getVdVersion() && !httpHeaders.containsKey(HeaderKey.VERSION)) {
                httpHeaders.add(HeaderKey.VERSION, contextModel.getVdVersion());
            }
            if (null != contextModel.getVdEnv() && !httpHeaders.containsKey(HeaderKey.ENV)) {
                httpHeaders.add(HeaderKey.ENV, contextModel.getVdEnv());
            }
            httpHeaders.add(HeaderKey.PID, contextModel.getId());
            httpHeaders.add(HeaderKey.TID, contextModel.getTraceId());
            httpHeaders.add(HeaderKey.APP_NAMES, contextModel.getAppNames());
        }).build();
        ServerWebExchange newExchange = request.mutate().request(newRequest).build();
        allArguments[0] = newExchange;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        logger.debug("gateway trace throwing clean ");
        DecisionPluginContext.clean();
    }
}
