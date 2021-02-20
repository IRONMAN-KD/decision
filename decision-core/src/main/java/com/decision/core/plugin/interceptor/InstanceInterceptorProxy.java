package com.decision.core.plugin.interceptor;

import com.decision.core.manager.loader.InterceptorInstanceLoader;
import com.decision.core.plugin.interceptor.enhance.MethodInterceptResult;
import com.decision.core.plugin.interceptor.enhance.OverrideCallable;
import net.bytebuddy.implementation.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @Author KD
 * @Date 2021/2/1 17:32
 */
public class InstanceInterceptorProxy {
    private Logger logger = LoggerFactory.getLogger(InstanceInterceptorProxy.class);
    private InstanceAroundInterceptor interceptor;

    public InstanceInterceptorProxy(String interceptorClassName, ClassLoader classLoader, String decisionHome) {
        try {
            interceptor = InterceptorInstanceLoader.load(interceptorClassName, classLoader, decisionHome);
        } catch (Throwable t) {
            logger.error("create interceptor error {}", t.getMessage());
        }
    }

    @RuntimeType
    public Object intercept(@This Object obj, @AllArguments Object[] allArguments,
                            @Origin Method method, @Morph OverrideCallable callable) throws Throwable {
        MethodInterceptResult result = new MethodInterceptResult();
        try {
            interceptor.before(obj, method, allArguments, method.getParameterTypes(),result);
        } catch (Throwable t) {
            logger.error("class[{}] before method[{}] intercept failure", obj.getClass(), method.getName());
        }

        Object ret = null;
        try {
            if (!result.isContinue()) {
                ret = result.getRet();
            } else {
                ret = callable.call(allArguments);
            }
        } catch (Throwable t) {
            try {
                interceptor.handleException(obj, method, allArguments, method.getParameterTypes(), t);
            } catch (Throwable t2) {
                logger.error("class[{}] handle method[{}] exception failure,{}", obj.getClass(), method.getName(), t2);
            }
            throw t;
        } finally {
            try {
                ret = interceptor.after(obj, method, allArguments, ret, method.getParameterTypes());
            } catch (Throwable t) {
                logger.error("class[{}] after method[{}] intercept failure,{}", obj.getClass(), method.getName(), t);
            }
        }
        return ret;
    }
}
