package conglin.clrpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 若注解不存在 或 {@link IgnoreService#ignore()} 为 false
 * 则向外部提供该方法的服务
 * 若 {@link IgnoreService#ignore()} 为 true 则不提供该方法的服务
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreService{
    boolean ignore() default true;
}