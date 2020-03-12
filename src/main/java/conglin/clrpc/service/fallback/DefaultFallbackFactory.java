package conglin.clrpc.service.fallback;

import conglin.clrpc.common.util.ClassUtils;

public class DefaultFallbackFactory implements FallbackFactory {

    @Override
    public Object create(Class<?> clazz) {
        return ClassUtils.defaultObject(clazz);
    }
}