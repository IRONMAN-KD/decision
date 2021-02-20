package com.decision.core.plugin.context;

/**
 * 当前服务信息单例类
 *
 * @Author KD
 * @Date 2020/12/29 17:35
 */
public class ServerInfoHolder {

    private String serverName;
    private String serverVersion;
    private String serverEnv;


    public ServerInfoHolder() {

    }

    private static class Holder {
        public static final ServerInfoHolder SERVER_INFO = new ServerInfoHolder();
    }

    public static ServerInfoHolder getInstance() {
        return Holder.SERVER_INFO;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerEnv() {
        return serverEnv;
    }

    public void setServerEnv(String serverEnv) {
        this.serverEnv = serverEnv;
    }
}
