package conglin.clrpc.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 
 * {@link IdempotentService#idempotence()} 为 true 则标识该方法是一个幂等的方法
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentService {
    /**
     * 是否是幂等的方法
     * 
     * @return
     */
    boolean idempotence() default true;
}