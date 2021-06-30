package com.decision.plugin.headerinfo.interceptor;

import com.decision.core.plugin.constant.DecisionConstant;
import com.decision.core.plugin.context.HeaderConfigHolder;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author KD
 * @Date 2021/2/8 14:26
 */
public class HeaderInfoInterceptor implements InstanceAroundInterceptor {
    private static final String DOT = ".";

    @Override
    public void before(Object targetObject, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
    }

    @Override
    public Object after(Object targetObject, Method method, Object[] allArguments, Object result, Object[] argumentsTypes) {
        handleChange(result);
        return result;
    }

    @Override
    public void handleException(Object targetObject, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

    public void handleChange(Object argument) {
        String configStr = "";
        //兼容高低版本
        if (argument instanceof String[]) {
            String[] configArray = (String[]) argument;
            configStr = configArray[0];
        } else {
            configStr = (String) argument;
        }
        Yaml yaml = new Yaml();
        //获取yml文件
        if (StringUtils.isNotEmpty(configStr)) {
            Map<String, Object> config = yaml.load(configStr);
            if (null != config && config.containsKey(DecisionConstant.DECISION)) {
                setHeaderConfigByYaml(config);
            }
        }
    }

    /**
     * 配置header
     *
     * @param yamlConfig
     */
    private void setHeaderConfigByYaml(Map<String, Object> yamlConfig) {
        Map<String, Object> versionConfig = new HashMap<>();
        forEachYaml(DecisionConstant.DECISION, (Map<String, Object>) yamlConfig.get(DecisionConstant.DECISION), versionConfig, 1, DecisionConstant.DECISION_HEADER_VERSION.split(DOT));
        if (versionConfig.size() > 0) {
            setHeaderConfig(versionConfig, true);
        }
        Map<String, Object> envConfig = new HashMap<>();
        forEachYaml(DecisionConstant.DECISION, (Map<String, Object>) yamlConfig.get(DecisionConstant.DECISION), versionConfig, 1, DecisionConstant.DECISION_HEADER_ENV.split(DOT));
        if (envConfig.size() > 0) {
            setHeaderConfig(envConfig, false);
        }
    }

    /**
     * 处理yml文件
     *
     * @param key_str
     * @param obj
     * @param result
     * @param i
     * @param keys
     * @return
     */
    private Map<String, Object> forEachYaml(String key_str, Map<String, Object> obj, Map<String, Object> result, int i, String[] keys) {
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (keys.length > i && !keys[i].equals(key)) {
                continue;
            }
            String str_new = "";
            if (StringUtils.isNotEmpty(key_str)) {
                str_new = key_str + "." + key;
            } else {
                str_new = key;
            }
            if (val instanceof Map) {
                forEachYaml(str_new, (Map<String, Object>) val, result, ++i, keys);
                i--;
            } else if (val instanceof List) {
                List<Map<String, String>> list = (List<Map<String, String>>) val;
                result.put(str_new, list);
            }
        }

        return result;
    }

    private void setHeaderConfig(Map<String, Object> configs, boolean isVersion) {
        HeaderConfigHolder headerConfigHolder = HeaderConfigHolder.getInstance();
        Map<String, String> versions = headerConfigHolder.getVersion();
        Map<String, String> envs = headerConfigHolder.getEnv();
        for (String configKey : configs.keySet()) {
            if (configKey.equals(DecisionConstant.DECISION_HEADER_VERSION) || configKey.equals(DecisionConstant.DECISION_HEADER_ENV)) {
                //变更时先清空版本映射信息，再全量设置
                if (isVersion) {
                    versions.clear();
                } else {
                    envs.clear();
                }

                List<Map<String, String>> list = (List<Map<String, String>>) configs.get(configKey);
                for (Map<String, String> map : list) {
                    if (isVersion) {
                        versions.putAll(map);
                    } else {
                        envs.putAll(map);
                    }

                }
            }
        }
    }
}
