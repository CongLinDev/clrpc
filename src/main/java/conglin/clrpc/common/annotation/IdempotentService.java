package conglin.clrpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 若注解不存在 或 {@link IdempotentService#idempotence()} 为 false
 * 则标识该方法不是一个幂等的方法
 * 若 {@link IdempotentService#idempotence()} 为 true 则标识该方法是一个幂等的方法
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentService{
    boolean idempotence() default true;
}