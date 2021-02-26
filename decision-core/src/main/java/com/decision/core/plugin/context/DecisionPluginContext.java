package com.decision.core.plugin.context;


import com.decision.core.plugin.common.DecisionThreadLocal;
import com.decision.core.plugin.common.GlobalIdGenerator;
import com.decision.core.plugin.common.StringUtil;

/**
 * decision 上线文
 *
 * @Author KD
 * @Date 2020/12/9 16:07
 */
public class DecisionPluginContext {
    private static final DecisionThreadLocal<ContextModel> DECISION_CONTEXT = new DecisionThreadLocal<ContextModel>();
    private static final String APPEND_SIGN = "->";
    private static final String VERSION_APPEND_SIGN = ":";

    public static void clean() {
        DECISION_CONTEXT.remove();
    }

    public static void set(ContextModel contextModel) {
        DECISION_CONTEXT.set(contextModel);
    }

    public static ContextModel getOrCreate() {
        ContextModel context = DECISION_CONTEXT.get();
        if (null == context) {
            context = create();
            DECISION_CONTEXT.set(context);
        }
        return context;
    }

    public static ContextModel create() {
        ContextModel context = new ContextModel();
        context.setId(GlobalIdGenerator.generate());
        DECISION_CONTEXT.set(context);
        return context;
    }

    public static String appendAppNames(String appNames, String curServerName, String curServerVersion) {
        StringBuilder builder = new StringBuilder(StringUtil.isEmpty(appNames) ? "" : appNames);
        builder.append(APPEND_SIGN)
                .append(curServerName)
                .append(VERSION_APPEND_SIGN)
                .append(curServerVersion);
        String appendedAppNames = builder.toString();
        DECISION_CONTEXT.get().setAppNames(appendedAppNames);
        return appendedAppNames;
    }


}
