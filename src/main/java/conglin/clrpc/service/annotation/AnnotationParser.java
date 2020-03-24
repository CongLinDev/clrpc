package conglin.clrpc.service.annotation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conglin.clrpc.service.fallback.FallbackFactory;

public class AnnotationParser {

    private AnnotationParser() {
        // unused
    }

    /**
     * 解析注解 {@code Service} 返回服务类的注解服务名
     * 
     * @param clazz
     * @return
     */
    public static String serviceName(Class<?> clazz) {
        Service service = clazz.getAnnotation(Service.class);
        if (service != null && service.enable())
            return service.name();
        return null;
    }

    /**
     * 解析注解 {@code Service} 获取父接口的注解服务名
     * 
     * 该方法只返回上一级接口的服务名 而不是所有接口的服务名
     * 
     * @param clazz
     * @return
     */
    public static Collection<String> superServiceNames(Class<?> clazz) {
        return Stream.of(clazz.getInterfaces()).map(AnnotationParser::serviceName).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 解析注解 {@code ServiceMethod} 是否开启服务方法
     * 
     * @param method
     * @return
     */
    public static boolean enableServiceMethod(Method method) {
        ServiceMethod serviceMethod = method.getAnnotation(ServiceMethod.class);
        return serviceMethod != null && !serviceMethod.enable();
    }

    /**
     * 解析注解 {@code Fallback} 标记的工厂
     * @param clazz
     * @return
     */
    public static Class<? extends FallbackFactory> resolveFallbackFactory(Class<?> clazz) {
        Fallback fallback = clazz.getAnnotation(Fallback.class);
        if (fallback == null)
            return null;
        return fallback.factory();
    }
}