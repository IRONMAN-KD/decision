package com.decision.plugin.ribbon.model;

import com.netflix.loadbalancer.Server;

import java.util.Map;

/**
 * 俘虏NacosServer，解决Nacos不同包版本的兼容
 *
 * @Author KD
 * @Date 2020/12/18 12:20
 */
public interface IServer {
    /**
     * 获取metaData信息
     *
     * @return
     */
    Map<String, String> getMetadata();

    /**
     * 获取metaInfo信息
     *
     * @return
     */
    Server.MetaInfo getMetaInfo();
}
