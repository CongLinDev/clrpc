package conglin.clrpc.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上 被标记方法的结果可缓存
 * 
 * {@link CacheableService#provider()} 为 true 代表 支持在服务提供者处缓存
 * {@link CacheableService#consumer()} 为 true 代表 支持在服务消费者处缓存
 * {@link CacheableService#exprie()} 取值为 [0~(2^20-1)] 代表 最大缓存时间（0 为
 * 最大缓存时间即无限期），单位为毫秒
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableService {
    /**
     * 是否在服务提供者处缓存
     * 
     * @return
     */
    boolean provider() default true;

    /**
     * 是否在服务消费者缓存
     * 
     * @return
     */
    boolean consumer() default true;

    /**
     * 缓存时间
     * 
     * @return
     */
    int exprie() default 0; // 默认为无限期，单位为毫秒
}