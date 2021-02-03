package com.decision.core.plugin.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author linkedong@vv.cn
 * @Date 2020/12/31 14:59
 */
public class HeaderConfigHolder {
    private Map<String, String> version = new HashMap<>();
    private Map<String, String> env = new HashMap<>();

    private HeaderConfigHolder() {

    }

    private static class Holder {
        private static final HeaderConfigHolder holder = new HeaderConfigHolder();
    }

    public static HeaderConfigHolder getInstance() {
        return Holder.holder;
    }

    public Map<String, String> getVersion() {
        return version;
    }

    public void setVersion(Map<String, String> version) {
        this.version = version;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }
}
