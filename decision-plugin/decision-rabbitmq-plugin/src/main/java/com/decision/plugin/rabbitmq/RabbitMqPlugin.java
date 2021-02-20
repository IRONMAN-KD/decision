package com.decision.plugin.rabbitmq;

import com.decision.core.plugin.DecisionPlugin;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.kohsuke.MetaInfServices;

/**
 * @Author KD
 * @Date 2021/2/8 14:23
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "rabbitMq", version = "1.0.0", author = "KD")
public class RabbitMqPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_SENDER_CLASS = "org.springframework.amqp.rabbit.core.RabbitTemplate";
    private static final String ENHANCE_LISTENER_CLASS = "org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer";
    private static final String INTERCEPT_SENDER_CLASS = "com.decision.plugin.rabbitmq.interceptor.RabbitMqSenderInterceptor";
    private static final String INTERCEPT_LISTENER_CLASS = "com.decision.plugin.rabbitmq.interceptor.RabbitMqListenerInterceptor";

    @Override
    public PluginInterceptPoint[] getInterceptPoint() {
        return new PluginInterceptPoint[]{
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_SENDER_CLASS))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod().and(ElementMatchers.<MethodDescription>named("sendToRabbit"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_SENDER_CLASS;
                    }
                },
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_LISTENER_CLASS))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod().and(ElementMatchers.<MethodDescription>named("invokeListener"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_LISTENER_CLASS;
                    }
                }
        };
    }
}
