package conglin.clrpc.common;

import conglin.clrpc.common.exception.FallbackFailedException;
import conglin.clrpc.common.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;

public interface Fallback {

    /**
     * 执行fallback的对象
     *
     * @return
     */
    Object object();

    /**
     * 是否需要fallback
     *
     * @param retryTimes
     * @return
     */
    default boolean needFallback(int retryTimes) {
        return false;
    }

    /**
     * 执行fallback
     *
     * @param methodName
     * @param args
     * @return
     * @throws FallbackFailedException
     */
    default Object fallback(String methodName, Object[] args) throws FallbackFailedException {
        try {
            return ClassUtils.reflectInvoke(object(), methodName, args);
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new FallbackFailedException(e);
        }
    }
}