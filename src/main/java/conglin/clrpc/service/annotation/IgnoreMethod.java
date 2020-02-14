package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 
 * {@link IgnoreMethod#ignore()} 为 true 时不向外界提供该方法的服务调用
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreMethod {
    /**
     * 是否忽略服务
     * 
     * @return
     */
    boolean ignore() default true;
}