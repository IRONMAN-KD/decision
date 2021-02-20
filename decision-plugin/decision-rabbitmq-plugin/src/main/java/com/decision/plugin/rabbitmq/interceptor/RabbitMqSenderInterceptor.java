package com.decision.plugin.rabbitmq.interceptor;

import com.decision.core.plugin.constant.HeaderKey;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.lang.reflect.Method;

/**
 * @Author KD
 * @Date 2021/2/8 14:26
 */
public class RabbitMqSenderInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        logger.debug("----begin RabbitMq pulisher set context -----");
        ContextModel contextModel = DecisionPluginContext.getOrCreate();
        String version = contextModel.getVdVersion();
        String env = contextModel.getVdEnv();
        Message message = (Message) allArguments[4];
        if (null != version) {
            logger.debug("context version :" + version);
            message.getMessageProperties().getHeaders().put(HeaderKey.MQ_VERSION, version);
        }
        if (null != env) {
            logger.debug("context env :" + env);
            message.getMessageProperties().getHeaders().put(HeaderKey.MQ_ENV, env);
        }
        message.getMessageProperties().getHeaders().put(HeaderKey.PID, contextModel.getId());
        message.getMessageProperties().getHeaders().put(HeaderKey.TID, contextModel.getTraceId());
        message.getMessageProperties().getHeaders().put(HeaderKey.APP_NAMES, contextModel.getAppNames());
        logger.debug("----end RabbitMq pulisher set context -----");
        allArguments[4] = message;
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

}
