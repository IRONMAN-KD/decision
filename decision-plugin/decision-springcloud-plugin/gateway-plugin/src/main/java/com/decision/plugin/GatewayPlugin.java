package com.decision.plugin;

import com.decision.core.plugin.DecisionPlugin;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.kohsuke.MetaInfServices;

/**
 * @Author linkedong@vv.cn
 * @Date 2021/2/3 16:58
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "gateway", version = "1.0.0", author = "KD")
public class GatewayPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_CLASS = "org.springframework.web.reactive.DispatcherHandler";
    private static final String INTERCEPT_CLASS = "com.decision.plugin.interceptor.GatewayInterceptor";
    @Override
    public PluginInterceptPoint[] getInterceptPoint() {
        return new PluginInterceptPoint[]{
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_CLASS))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.takesArguments(1))
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.springframework.web.server.ServerWebExchange")))
                                .and(ElementMatchers.<MethodDescription>named("handle"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_CLASS;
                    }
                }
        };
    }
}
