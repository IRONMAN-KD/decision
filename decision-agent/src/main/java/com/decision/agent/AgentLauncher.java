package com.decision.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * DecisionAgent启动器
 *
 * @Author KD
 * @Date 2021/1/20 15:46
 */
public class AgentLauncher {

    private static final String CORE_CONFIGURE_CLASS = "com.decision.core.CoreConfigure";
    private static final String CORE_AGENT_BUILDER_PROXY_CLASS = "com.decision.agent.AgentBuilderProxy";

    public static void premain(String arguments, Instrumentation inst) {
        try {
            String decisionHome = getDecisionHomePath();

            //使用自定义classloader，尽量避免agent类影响业务系统
            final DecisionClassLoader decisionClassLoader = new DecisionClassLoader(getDecisionCoreJarPath(decisionHome));
            Class<?> classOfPluginLoader = decisionClassLoader.loadClass(CORE_AGENT_BUILDER_PROXY_CLASS);
            Object instanceOfPluginLoader = classOfPluginLoader.newInstance();
            classOfPluginLoader.getMethod("buildAgent", Instrumentation.class, String.class)
                    .invoke(instanceOfPluginLoader, inst, decisionHome);
        } catch (Exception e) {
            throw new RuntimeException("init decision agent error ", e);
        }
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
        return decisionHome + File.separatorChar + "decision-agent.jar";
    }


}
