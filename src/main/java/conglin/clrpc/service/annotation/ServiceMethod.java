package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的方法上
 * 
 * {@link ServiceMethod#enable()} 为 {@code false} 时不向外界提供该方法的服务调用
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceMethod {
    /**
     * 是否开启服务
     * 
     * @return
     */
    boolean enable() default false;
}