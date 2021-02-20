package com.decision.core.plugin.context;

/**
 * 上下文对象
 *
 * @Author KD
 * @Date 2020/12/9 16:07
 */
public class ContextModel {
    private String id;
    private String traceId;
    private String parentId;
    private String appNames;
    private String vdVersion;
    private String vdEnv;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getAppNames() {
        return appNames;
    }

    public void setAppNames(String appNames) {
        this.appNames = appNames;
    }

    public String getVdVersion() {
        return vdVersion;
    }

    public void setVdVersion(String vdVersion) {
        this.vdVersion = vdVersion;
    }

    public String getVdEnv() {
        return vdEnv;
    }

    public void setVdEnv(String vdEnv) {
        this.vdEnv = vdEnv;
    }

    public ContextModel copy() {
        ContextModel model = new ContextModel();
        model.setId(this.id);
        model.setTraceId(this.traceId);
        model.setParentId(this.parentId);
        model.setAppNames(this.appNames);
        model.setVdEnv(this.vdEnv);
        model.setVdVersion(this.vdVersion);
        return model;
    }
}
