
package com.decision.core.plugin.common;


import java.util.UUID;

/**
 * 全局ID生成
 *
 * @author KD
 */
public final class GlobalIdGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }

}
