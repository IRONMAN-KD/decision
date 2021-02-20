package com.decision.core.plugin;

/**
 * @Author KD
 * @Date 2021/2/1 11:10
 */
public abstract class AbstractDecisionPluginDefine implements DecisionPluginDefine{

    /**
     * 全局唯一编号
     */
    private String uniqueId;


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
