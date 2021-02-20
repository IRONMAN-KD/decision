package com.decision.core.plugin.interceptor;

import org.slf4j.Logger;

/**
 * @Author KD
 * @Date 2021/2/7 18:02
 */
public abstract class AbstractInstanceAroundInterceptor implements InstanceAroundInterceptor {
    private Logger logger;

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
