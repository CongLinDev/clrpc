package conglin.clrpc.service.fallback;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.annotation.AnnotationParser;

public class DefaultFallbackHolder implements FallbackHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackHolder.class);

    private final Map<String, Object> holder;

    private final int MAX_RETRY_TIMES;

    public DefaultFallbackHolder(PropertyConfigurer configurer) {
        MAX_RETRY_TIMES = configurer.getOrDefault("consumer.fallback.max-retry", -1);
        if (enable()) {
            holder = new HashMap<>();
            LOGGER.info("Fallback enabled.");
        } else {
            holder = null;
            LOGGER.info("Fallback disabled.");
        }
    }

    @Override
    public boolean add(String key, Class<?> interfaceClass) {
        if (!enable())
            return true;
        Class<? extends FallbackFactory> factoryClass = AnnotationParser.resolveFallbackFactory(interfaceClass);
        if (factoryClass == null)
            return false;

        try {
            Object fallbackObject = factoryClass.getDeclaredConstructor().newInstance().create(interfaceClass);
            holder.put(key, fallbackObject);
            return true;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Add fallback {} failed. Cause: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean enable() {
        return MAX_RETRY_TIMES > -1;
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