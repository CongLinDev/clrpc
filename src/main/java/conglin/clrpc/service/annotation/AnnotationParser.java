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
     * 解析注解 {@code Service} 服务标识符
     * 
     * @param clazz
     * @return
     */
    public static String serviceIdentifier(Class<?> clazz) {
        Service service = clazz.getAnnotation(Service.class);
        if (service != null && service.enable()) {
            return service.name() + "&" + service.version();
        }
        return null;
    }

    /**
     * 解析注解 {@code Service} 获取父接口的服务标识符
     * 
     * 该方法只返回上一级接口的服务标识符 而不是所有接口的服务名
     * 
     * @param clazz
     * @return
     */
    public static Collection<String> superServiceIdentifiers(Class<?> clazz) {
        return Stream.of(clazz.getInterfaces()).map(AnnotationParser::serviceIdentifier).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 解析注解 {@code Fallback} 标记的工厂
     * 
     * @param clazz
     * @return
     */
    public static Class<? extends FallbackFactory> resolveFallbackFactory(Class<?> clazz) {
        Fallback fallback = clazz.getAnnotation(Fallback.class);
        if (fallback == null)
            return null;
        return fallback.factory();
    }

    /**
     * 返回事务提交对应的方法
     * 
     * @param method
     * @return
     */
    public static String resolveTransactionCommit(Method method) {
        Transaction transaction = method.getAnnotation(Transaction.class);
        if (transaction == null)
            return null;
        String commit = transaction.commit();
        return "".equals(commit) ? null : commit;
    }

    /**
     * 返回事务回滚对应的方法
     * 
     * @param method
     * @return
     */
    public static String resolveTransactionRollback(Method method) {
        Transaction transaction = method.getAnnotation(Transaction.class);
        if (transaction == null)
            return null;
        String rollback = transaction.rollback();
        return "".equals(rollback) ? null : rollback;
    }
}