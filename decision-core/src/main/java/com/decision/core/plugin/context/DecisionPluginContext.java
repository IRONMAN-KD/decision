package com.decision.core.plugin.context;


import com.decision.core.plugin.common.GlobalIdGenerator;

/**
 * decision 上线文
 *
 * @Author linkedong@vv.cn
 * @Date 2020/12/9 16:07
 */
public class DecisionPluginContext {
    private static final InheritableThreadLocal<ContextModel> decisionContext = new InheritableThreadLocal<ContextModel>();
    private static final String APPEND_SIGN = "->";
    private static final String VERSION_APPEND_SIGN = ":";

    public static void clean() {
        decisionContext.remove();
    }

    public static void set(ContextModel contextModel) {
        decisionContext.set(contextModel);
    }
    
    public static ContextModel getOrCreate() {
        ContextModel context = decisionContext.get();
        if (null == context) {
            context = create();
            decisionContext.set(context);
        }
        return context;
    }

    public static ContextModel create() {
        ContextModel context = new ContextModel();
        context.setId(GlobalIdGenerator.generate());
        decisionContext.set(context);
        return context;
    }

    public static String appendAppNames(String appNames, String curServerName, String curServerVersion) {
        StringBuilder builder = new StringBuilder(appNames);
        builder.append(APPEND_SIGN)
                .append(curServerName)
                .append(VERSION_APPEND_SIGN)
                .append(curServerVersion);
        String appendedAppNames = builder.toString();
        decisionContext.get().setAppNames(appendedAppNames);
        return appendedAppNames;
    }


}
