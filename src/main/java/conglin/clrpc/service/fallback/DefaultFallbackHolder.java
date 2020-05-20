package conglin.clrpc.service.fallback;

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
    public void add(String key, Class<?> interfaceClass) {
        if (!enable())
            return;

        Class<?> fallbackClass = AnnotationParser.resolveFallback(interfaceClass);
        Object fallback = ClassUtils.loadObjectByType(fallbackClass, interfaceClass);

        if(fallback == null) {
            LOGGER.warn("Construct fallback for {} failed. Use default fallback.", key);
            holder.put(key, ClassUtils.defaultObject(interfaceClass));
        } else {
            LOGGER.warn("Add fallback({}) for {}.", fallbackClass, key);
            holder.put(key, fallback);
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