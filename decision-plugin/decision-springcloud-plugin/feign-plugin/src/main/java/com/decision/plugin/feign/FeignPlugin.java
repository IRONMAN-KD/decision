package com.decision.plugin.feign;

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
 * @Date 2021/2/3 16:58
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "feign", version = "1.0.0", author = "KD")
public class FeignPlugin implements DecisionPluginDefine {
    //private static final String ENHANCE_CLASS = "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient";
    private static final String ENHANCE_CLASS = "feign.Client";
    private static final String INTERCEPT_CLASS = "com.decision.plugin.feign.interceptor.FeignInterceptor";

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
                        return ElementMatchers.isMethod().and(ElementMatchers.<MethodDescription>named("execute"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_CLASS;
                    }
                }
        };
    }
}
