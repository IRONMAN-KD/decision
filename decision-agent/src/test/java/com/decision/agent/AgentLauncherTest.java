package com.decision.agent;

import org.junit.Test;

import java.lang.instrument.Instrumentation;

/**
 * @Author linkedong@vv.cn
 * @Date 2021/2/1 13:58
 */
public class AgentLauncherTest {
    @Test
    public void test(){
        AgentLauncher.premain("dd", null);
    }
}
