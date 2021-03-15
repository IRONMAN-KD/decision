package com.decision.plugin.ribbon.interceptor;

import com.decision.core.plugin.common.StringUtil;
import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.constant.DecisionMateData;
import com.decision.core.plugin.context.ContextModel;
import com.decision.core.plugin.context.DecisionPluginContext;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.decision.plugin.ribbon.model.IServer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.decision.core.plugin.common.InterfaceProxyUtils.puppet;

/**
 * @Author KD
 * @Date 2021/2/3 17:01
 */
public class RibbonInterceptor implements InstanceAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {

    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        ContextModel contextModel = DecisionPluginContext.getOrCreate();
        String vdVersion = contextModel.getVdVersion();
        String vdEnv = contextModel.getVdEnv();
        logger.debug(" begin ribbon loadBalance route ");
        List<Server> servers = (List<Server>) result;
        List<Server> resultServers = new ArrayList<Server>();
        if (StringUtil.isEmpty(vdEnv) && StringUtil.isEmpty(vdVersion)) {
            //如果不传参数的话，默认使用common版本进行处理
            resultServers = handleDefault(servers);
        } else {
            resultServers = handleHeaderParams(servers, vdVersion, vdEnv);
        }


        for (Server resultServer : resultServers) {
            logger.debug("route to server :" + resultServer.getMetaInfo().getInstanceId());
        }
        logger.debug(" end ribbon loadBalance route ");

        return Collections.unmodifiableList(resultServers);
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }

    private List<Server> handleDefault(List<Server> servers) {

        if (null == servers || servers.size() == 0) {
            return servers;
        }
        List<Server> commonServer = new ArrayList<Server>();
        for (Server server : servers) {
            String mateDataVersion = null;
            String mateDataEnv = null;
            // 俘虏nacosServer参数为傀儡,解决兼容性问题
            final IServer decisionServer = puppet(
                    IServer.class,
                    server
            );
            if (null != decisionServer) {
                mateDataVersion = decisionServer.getMetadata().get(DecisionMateData.VERSION);
                mateDataEnv = decisionServer.getMetadata().get(DecisionMateData.ENV);
            }
            //当header信息中未配置env和version时，默认走common版本，如该服务的所有实例都为配置common版本，则按原来规则过滤
            boolean isHit = (StringUtil.isNotEmpty(mateDataEnv) && mateDataEnv.equals(DecisionConstant.COMMON))
                    && (StringUtil.isNotEmpty(mateDataVersion) && mateDataVersion.equals(DecisionConstant.COMMON));
            if (isHit) {
                commonServer.add(server);
            }
        }
        return commonServer.size() == 0 ? servers : commonServer;

    }

    private List<Server> handleHeaderParams(List<Server> servers, String vdVersion, String vdEnv) {
        List<Server> resultServers = new ArrayList<Server>();
        boolean isHitEnv = false;
        for (Server server : servers) {
            String mateDataVersion = null;
            String mateDataEnv = null;
            String serviceName = null;
            // 俘虏nacosServer参数为傀儡,解决兼容性问题
            final IServer decisionServer = puppet(
                    IServer.class,
                    server
            );
            if (null != decisionServer) {
                mateDataVersion = decisionServer.getMetadata().get(DecisionMateData.VERSION);
                mateDataEnv = decisionServer.getMetadata().get(DecisionMateData.ENV);
                serviceName = decisionServer.getMetaInfo().getAppName();
            }

            String applicationName = serviceName.substring(serviceName.lastIndexOf("@") + 1);
            boolean filterFlag = false;
            if (StringUtil.isNotEmpty(vdEnv)) {
                if (vdEnv.equals(mateDataEnv)) {
                    isHitEnv = true;
                    filterFlag = isHitVersion(vdVersion, applicationName, mateDataVersion);
                }
            } else if (StringUtil.isNotEmpty(mateDataEnv) && mateDataEnv.equals(DecisionConstant.COMMON)) {
                filterFlag = isHitVersion(vdVersion, applicationName, mateDataVersion);
            }
            if (filterFlag) {
                resultServers.add(server);
            }
        }
        //当所有服务都未命中所设置的环境时，再一次从common环境中查询是否有命中的服务
        if (resultServers.size() == 0 && !isHitEnv) {
            resultServers = servers.stream().filter(server -> {
                final IServer decisionServer = puppet(
                        IServer.class,
                        server
                );
                String mateDataVersion = decisionServer.getMetadata().get(DecisionMateData.VERSION);
                String serviceName = decisionServer.getMetaInfo().getAppName();
                String mateDataEnv = decisionServer.getMetadata().get(DecisionMateData.ENV);
                String applicationName = serviceName.substring(serviceName.lastIndexOf("@") + 1);
                return DecisionConstant.COMMON.equals(mateDataEnv) && isHitVersion(vdVersion, applicationName, mateDataVersion);
            }).collect(Collectors.toList());
        }
        return resultServers;
    }

    private Boolean isHitVersion(String vdVersion, String applicationName, String mateDataVersion) {
        if (StringUtil.isNotEmpty(vdVersion) && vdVersion.contains("\"" + applicationName + "\"")) {
            return vdVersion.contains("\"" + applicationName + "\":\"" + mateDataVersion + "\"");
        } else {
            return StringUtil.isNotEmpty(mateDataVersion) && mateDataVersion.equals(DecisionConstant.COMMON);
        }
    }
}
