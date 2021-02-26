package com.decision.plugin.thread.wrapper;

import com.decision.core.plugin.common.DecisionThreadLocal;
import com.decision.core.plugin.common.InterfaceProxyUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author KD
 * @Date 2020/12/21 13:51
 */
public class DecisionForkJoinTaskWrapper<V> extends ForkJoinTask<V> {
    private ForkJoinTask<V> task;
    private AtomicReference<DecisionThreadLocal.SnapShot> captureRef;
    private static Method setRawResultMethod;
    private static Method execMethod;
    private static List<Method> taskMethodList;

    public DecisionForkJoinTaskWrapper(ForkJoinTask<V> task) {
        this.task = task;
        this.captureRef = new AtomicReference<>(DecisionThreadLocal.Transmitter.capture());
    }

    @Override
    public V getRawResult() {
        return task.getRawResult();
    }

    @Override
    protected void setRawResult(V value) {
        if (setRawResultMethod == null) {
            setRawResultMethod = getMethod("setRawResult");
        }
        try {
            setRawResultMethod.setAccessible(true);
            setRawResultMethod.invoke(task, value);
        } catch (Throwable e) {
            throw new RuntimeException("反射执行setRawResult方法失败", e);
        }
    }

    @Override
    protected boolean exec() {
        DecisionThreadLocal.SnapShot capture = captureRef.get();
        DecisionThreadLocal.SnapShot backUp = DecisionThreadLocal.Transmitter.replay(capture);
        if (execMethod == null) {
            execMethod = getMethod("exec");
        }
        try {
            execMethod.setAccessible(true);
            return (Boolean) execMethod.invoke(task);
        } catch (Throwable e) {
            throw new RuntimeException("反射执行exec方法失败", e);
        } finally {
            DecisionThreadLocal.Transmitter.restore(backUp);
        }
    }

    private Method getMethod(String name) {
        if (taskMethodList == null) {
            taskMethodList = InterfaceProxyUtils.getAllMethod(task.getClass());
        }
        for (Method m : taskMethodList) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}
