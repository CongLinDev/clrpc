package conglin.clrpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的类型上
 * 
 * {@link Service#name()} 为 设置的服务名
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * 服务名
     * 
     * @return
     */
    public String name();
}