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
 * 注意：该注解只能被使用在服务接口上，服务实现类通过继承服务接口来发布服务。
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

    /**
     * 是否忽略该服务
     * 
     * @return
     */
    public boolean ignore() default false;
}