package com.decision.core;

import com.decision.core.util.LogbackUtils;

/**
 * @Author KD
 * @Date 2021/1/21 11:44
 */
public class CoreLauncher {
    private static CoreLauncher instance = new CoreLauncher();

    private CoreLauncher() {

    }


    public CoreLauncher getInstance() {
        return instance;
    }

    public void init(CoreConfigure configure) {

    }
}
