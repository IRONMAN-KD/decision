package com.decision.plugin.rabbitmq.interceptor;

import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.context.ServerInfoHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.lang.reflect.Method;

/**
 * @Author KD
 * @Date 2021/2/8 14:26
 */
public class RabbitMqListenerInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        boolean isNeedReject = false;
        Channel channel = (Channel) allArguments[0];
        Message message = (Message) allArguments[1];
        Object headerVersion = message.getMessageProperties().getHeaders().get(HeaderKey.MQ_VERSION);
        Object headerEnv = message.getMessageProperties().getHeaders().get(HeaderKey.MQ_ENV);
        Object traceId = message.getMessageProperties().getHeaders().get(HeaderKey.TID);
        Object parentId = message.getMessageProperties().getHeaders().get(HeaderKey.PID);
        Object appNames = message.getMessageProperties().getHeaders().get(HeaderKey.APP_NAMES);
        ContextModel contextModel = DecisionPluginContext.getOrCreate();
        try {
            ServerInfoHolder serverInfoHolder = ServerInfoHolder.getInstance();
            String version = serverInfoHolder.getServerVersion();
            String serverName = serverInfoHolder.getServerName();
            String env = serverInfoHolder.getServerEnv();
            if (null != headerVersion) {
                String headerVersionStr = headerVersion.toString();
                logger.debug("context version :" + headerVersionStr);
                String serverVersion = null != version ? String.format("\"%s\":\"%s\"", serverName, version) : null;
                contextModel.setVdVersion(headerVersionStr);
                if (null != serverVersion) {
                    isNeedReject = !headerVersionStr.contains(serverVersion);
                }
            }
            if (null != headerEnv) {
                String headerEnvStr = headerEnv.toString();
                logger.debug("context version :" + headerEnvStr);
                contextModel.setVdEnv(headerEnvStr);
                isNeedReject = !headerEnvStr.equals(env);
            }
            if (isNeedReject) {
                logger.debug("---- RabbitMq consumer reject message -----");
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isNeedReject) {
            logger.debug("----end  RabbitMq consumer return after reject -----");
            result.defineReturnValue(null);
        }
        ServerInfoHolder serverInfo = ServerInfoHolder.getInstance();
        //判断链路信息是否完整，完整的话进行往下传递，并打印调用的服务信息
        if (null != traceId && null != parentId) {
            contextModel.setTraceId(traceId.toString());
            contextModel.setParentId(parentId.toString());
            DecisionPluginContext.appendAppNames(appNames.toString(), serverInfo.getServerName(), serverInfo.getServerVersion());
            logger.debug(contextModel.getAppNames());
        }
        logger.debug("----end  RabbitMq consumer do normal invoke -----");
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

}
