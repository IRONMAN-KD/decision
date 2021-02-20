package com.decision.plugin.serverinfo;

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
@DecisionPlugin(id = "serverInfo", version = "1.0.0", author = "KD")
public class ServerInfoOfNacosPlugin implements DecisionPluginDefine {
    private static final String NACOS_ENHANCE_CLASS_OF_ALIBABA = "com.alibaba.cloud.nacos.registry.NacosServiceRegistry";
    private static final String NACOS_ENHANCE_CLASS_OF_SPRING_CLOUD = "org.springframework.cloud.alibaba.nacos.registry.NacosServiceRegistry";
    private static final String NACOS_INTERCEPT_CLASS = "com.decision.plugin.serverinfo.interceptor.ServerInfoOfNacosInterceptor";

    @Override
    public PluginInterceptPoint[] getInterceptPoint() {
        return new PluginInterceptPoint[]{
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(NACOS_ENHANCE_CLASS_OF_ALIBABA))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.takesArguments(1))
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.springframework.cloud.client.serviceregistry.Registration")))
                                .and(ElementMatchers.<MethodDescription>named("register"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return NACOS_INTERCEPT_CLASS;
                    }
                },
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(NACOS_ENHANCE_CLASS_OF_SPRING_CLOUD))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.takesArguments(1))
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.springframework.cloud.client.serviceregistry.Registration")))
                                .and(ElementMatchers.<MethodDescription>named("register"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return NACOS_INTERCEPT_CLASS;
                    }
                }
        };
    }
}
