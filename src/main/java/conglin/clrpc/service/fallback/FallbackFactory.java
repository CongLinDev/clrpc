package conglin.clrpc.service.fallback;

/**
 * 用于创建 Fallback 对象
 * 
 * {@link FallbackFactory} 的实现类必须提供一个无参构造方法
 */
@FunctionalInterface
public interface FallbackFactory {

    /**
     * 创建 Fallback 对象
     * 
     * @param clazz
     * @return
     */
    Object create(Class<?> clazz);
}