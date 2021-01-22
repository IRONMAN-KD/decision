package com.decision.core.util;

import java.io.File;

/**
 * Decision工具类
 *
 * @Author KD
 * @Date 2021/1/20 16:00
 */
public class DecisionUtils {
    private static String decisionHome;

    public static String getDecisionHomePath() {
        if (null == decisionHome) {
            synchronized (DecisionUtils.class) {
                if (null == decisionHome) {
                    String filePath = DecisionUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
                    decisionHome = new File(filePath).getParentFile().getAbsolutePath();
                }
            }
        }
        return decisionHome;
    }
}
