package conglin.clrpc.service.fallback;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.annotation.Fallback;

public class DefaultFallbackHolder implements FallbackHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackHolder.class);

    private final Map<String, Object> holder;

    private final int MAX_RETRY_TIMES;

    public DefaultFallbackHolder(PropertyConfigurer configurer) {
        boolean enable = configurer.getOrDefault("service.fallback.enable", false);
        if (enable) {
            MAX_RETRY_TIMES = configurer.getOrDefault("service.fallback.max-retry", 5);
            holder = new HashMap<>();
        } else {
            holder = null;
            MAX_RETRY_TIMES = -1;
        }
        LOGGER.debug("Fallback enable={}.", enable);
    }

    @Override
    public boolean add(String key, Class<?> interfaceClass) {
        if (!enable())
            return true;
        Fallback fallback = interfaceClass.getAnnotation(Fallback.class);
        if (fallback == null)
            return false;
        Class<? extends FallbackFactory> factoryClass = fallback.factory();

        try {
            Object fallbackObject = factoryClass.getDeclaredConstructor().newInstance().create(interfaceClass);
            put(key, fallbackObject);
            return true;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Add fallback {} failed. Cause: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean enable() {
        return MAX_RETRY_TIMES < 0;
    }

    @Override
    public Object put(String key, Object fallback) {
        if (!enable())
            return null;
        return holder.put(key, fallback);

    }

    @Override
    public Object get(String key) {
        return holder.get(key);
    }

    @Override
    public boolean needFallback(int retryTimes) {
        return enable() && retryTimes > MAX_RETRY_TIMES;

    }

    @Override
    public Object fallback(String service, String methodName, Object[] args) throws FallbackFailedException {
        try {
            Object fallback = holder.get(service);
            return fallback.getClass().getMethod(methodName, ClassUtils.getClasses(args)).invoke(fallback, args);
        } catch (Exception e) {
            throw new FallbackFailedException(e);
        }
    }
}