package com.decision.agent;

import com.decision.core.logging.LogbackUtils;
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

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * DecisionAgent启动器
 *
 * @Author KD
 * @Date 2021/1/20 15:46
 */
public class AgentLauncher {

    public static void premain(String arguments, Instrumentation inst) {

        try {
            //获取包路径
            String decisionHome = getDecisionHomePath();
            // 初始化日志
            LogbackUtils.init(decisionHome);
            AgentBuilder agentBuilder = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(buildListener())
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
                                            .to(new InstanceInterceptorProxy(interceptPoint.getMethodInterceptor(), classLoader)));
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

    private static AgentBuilder.Listener buildListener() {
        return new AgentBuilder.Listener() {

            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
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

    public static String getDecisionHomePath() {
        String filePath = AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String decisionHome = new File(filePath).getParentFile().getAbsolutePath();
        return decisionHome;
    }


    private static String getDecisionCfgPath(String decisionHome) {
        return decisionHome + File.separatorChar + "cfg";
    }


    private static String getDecisionPropertiesPath(String decisionHome) {
        return decisionHome + File.separatorChar + "decision.properties";
    }


    private static String getDecisionCoreJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "lib" + File.separatorChar + "decision-core.jar";
    }

    private static String getDecisionSpyJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "spy" + File.separatorChar + "decision-spy.jar";
    }


}
