package com.decision.plugin.headerinfo;

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
@DecisionPlugin(id = "headerInfo", version = "1.0.0", author = "KD")
public class HeaderInfoPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_CLASS = "org.springframework.cloud.context.refresh.ContextRefresher";
    private static final String INTERCEPT_CLASS = "com.decision.plugin.headerinfo.interceptor.HeaderInfoInterceptor";
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
                                .and(ElementMatchers.takesArguments(2))
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.util.Map")))
                                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("java.util.Map")))
                                .and(ElementMatchers.<MethodDescription>named("changes"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_CLASS;
                    }
                }
        };
    }
}
