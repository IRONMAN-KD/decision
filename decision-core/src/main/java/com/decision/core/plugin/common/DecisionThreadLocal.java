package com.decision.core.plugin.common;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 自定义ThreadLocal（参考TransmittableThreadLocal）解决线程池上下文传递问题
 *
 * @Author KD
 * @Date 2021/2/26 15:54
 */
public class DecisionThreadLocal<T> extends InheritableThreadLocal<T> {
    private static InheritableThreadLocal<WeakHashMap<DecisionThreadLocal<Object>, ?>> holder = new InheritableThreadLocal<WeakHashMap<DecisionThreadLocal<Object>, ?>>() {
        @Override
        protected WeakHashMap<DecisionThreadLocal<Object>, ?> childValue(WeakHashMap<DecisionThreadLocal<Object>, ?> parentValue) {
            return new WeakHashMap<DecisionThreadLocal<Object>, Object>(parentValue);
        }

        @Override
        protected WeakHashMap<DecisionThreadLocal<Object>, ?> initialValue() {
            return new WeakHashMap<DecisionThreadLocal<Object>, Object>();
        }
    };

    @Override
    public T get() {
        T value = super.get();
        if (null != value) {
            addThisToHolder();
        }
        return value;
    }

    private void addThisToHolder() {
        if (!holder.get().containsKey(this)) {
            holder.get().put((DecisionThreadLocal<Object>) this, null);
        }
    }

    @Override
    public void set(T value) {
        if (null == value) {
            removeFromHolder();
            super.remove();
        } else {
            super.set(value);
            addThisToHolder();
        }
    }

    private void removeFromHolder() {
        holder.get().remove(this);
    }

    public static class Transmitter {
        //抓取快照
        public static SnapShot capture() {
            return new SnapShot(capturedtlValues());
        }

        private static WeakHashMap<DecisionThreadLocal<Object>, Object> capturedtlValues() {
            WeakHashMap<DecisionThreadLocal<Object>, Object> dtlValues = new WeakHashMap<>();
            for (DecisionThreadLocal<Object> ctlItem : holder.get().keySet()) {
                dtlValues.put(ctlItem, ctlItem.get());
            }
            return dtlValues;
        }

        public static DecisionThreadLocal.SnapShot replay(DecisionThreadLocal.SnapShot snapShot) {
            WeakHashMap<DecisionThreadLocal<Object>, Object> capture = snapShot.dtlValue;
            WeakHashMap<DecisionThreadLocal<Object>, Object> backValue = new WeakHashMap<>();
            Iterator<DecisionThreadLocal<Object>> iterator = holder.get().keySet().iterator();
            while (iterator.hasNext()) {
                DecisionThreadLocal<Object> threadLocal = iterator.next();
                backValue.put(threadLocal, threadLocal.get());
                if (!capture.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.remove();
                }
            }
            setDtlValuesTo(capture);
            return new SnapShot(backValue);
        }

        private static void setDtlValuesTo(WeakHashMap<DecisionThreadLocal<Object>, Object> dtlValues) {
            for (Map.Entry<DecisionThreadLocal<Object>, Object> entry : dtlValues.entrySet()) {
                DecisionThreadLocal<Object> threadLocal = (DecisionThreadLocal<Object>) entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }


        public static void restore(DecisionThreadLocal.SnapShot backUp) {
            Iterator<DecisionThreadLocal<Object>> iterator = holder.get().keySet().iterator();
            while (iterator.hasNext()) {
                DecisionThreadLocal<Object> threadLocal = iterator.next();
                if (!backUp.dtlValue.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.remove();
                }
            }
            setDtlValuesTo(backUp.dtlValue);
        }
    }

    public static class SnapShot {
        final WeakHashMap<DecisionThreadLocal<Object>, Object> dtlValue;

        private SnapShot(WeakHashMap<DecisionThreadLocal<Object>, Object> dtlValue) {
            this.dtlValue = dtlValue;
        }
    }
}
