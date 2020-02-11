package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的 ServiceBean 的类型上
 * 
 * {@link Service#name()} 为 设置的服务名
 * 
 * 注意：接口上的注解是不能被继承的。如果在接口上使用该注解，那么在使用具体的实现类时必须重写该注解。
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Service {
    /**
     * 服务名
     * 
     * @return
     */
    public String name();
}