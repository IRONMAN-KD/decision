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
 * @Date 2021/2/1 14:06
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "servlet", version = "1.0.0", author = "KD")
public class ServletPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_CLASS = "javax.servlet.http.HttpServlet";
    private static final String INTERCEPT_CLASS = "com.decision.plugin.interceptor.ServletInterceptor";

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
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
                                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")))
                                .and(ElementMatchers.<MethodDescription>nameStartsWith("do"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_CLASS;
                    }
                }
        };
    }

}
