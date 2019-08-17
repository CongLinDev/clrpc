package conglin.clrpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 被标记方法的结果可缓存
 * 
 * {@link CacheableService#provider()} 为 true 代表 支持在服务提供者处缓存
 * {@link CacheableService#provider_exprie()} 取值为 [0-511] 代表 在在服务提供者处最大缓存时间（0 为 最大缓存时间即无限期）
 * 
 * {@link CacheableService#consumer()} 为 true 代表 支持在服务消费者处缓存
 * {@link CacheableService#consumer_exprie()} 取值为 [0-511] 代表 在在服务消费者处最大缓存时间（0 为 最大缓存时间即无限期）
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableService{
    boolean provider() default true;
    boolean consumer() default true;

    int provider_exprie() default 0; //默认为无限期
    int consumer_exprie() default 0; //默认为无限期
}