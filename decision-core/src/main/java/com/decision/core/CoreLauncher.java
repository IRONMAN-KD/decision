package com.decision.core;

import com.decision.core.manager.loader.InterceptorInstanceLoader;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import com.decision.core.plugin.PluginLoader;
import com.decision.core.plugin.interceptor.InstanceInterceptorProxy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * 该类在初始化时与业务系统进行隔离
 *
 * @Author linkedong@vv.cn
 * @Date 2021/2/3 16:10
 */
public class CoreLauncher {
    private final Logger LOGGER = LoggerFactory.getLogger(CoreLauncher.class);

    public void init(Instrumentation inst, String decisionHome) {
        try {

            AgentBuilder agentBuilder = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(buildListener())
                    .disableClassFormatChanges()
                    .ignore(ElementMatchers.<TypeDescription>none().and(ElementMatchers.nameStartsWith("com.decision.")));
            List<DecisionPluginDefine> plugins = new PluginLoader().loadPlugins(decisionHome);
            for (int i = 0; i < plugins.size(); i++) {
                final DecisionPluginDefine plugin = plugins.get(i);
                PluginInterceptPoint[] interceptPoints = plugin.getInterceptPoint();
                for (int j = 0; j < interceptPoints.length; j++) {
                    final PluginInterceptPoint interceptPoint = interceptPoints[j];
                    AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                                TypeDescription typeDescription,
                                                                ClassLoader classLoader, JavaModule javaModule) {
                            builder = builder.method(interceptPoint.buildMethodsMatcher())
                                    .intercept(MethodDelegation.withDefaultConfiguration()
                                            .to(new InstanceInterceptorProxy(interceptPoint.getMethodInterceptor(),classLoader)));
                            return builder;
                        }
                    };
                    agentBuilder = agentBuilder.type(interceptPoint.buildTypesMatcher()).transform(transformer);
                }
            }
            agentBuilder.installOn(inst);
        } catch (Exception e) {
            throw new RuntimeException("init bytebuddy AgentBuilder error ", e);
        }

    }

    private AgentBuilder.Listener buildListener() {
        return new AgentBuilder.Listener() {

            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
                LOGGER.info("织入成功");
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {

            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }
        };
    }
}
