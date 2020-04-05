package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于提供服务的接口的类型上
 * 
 * {@link Service#name()} 为 设置的服务名
 * 
 * 注意：该注解只能被使用在服务接口上，服务实现类通过继承服务接口来发布服务。
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
    String name();

    /**
     * 是否开启该服务
     * 
     * @return
     */
    boolean enable() default true;

    /**
     * 服务版本号
     * 
     * {@see <a href="https://semver.org/">https://semver.org/</a>}
     * 
     * @return
     */
    String version() default "0.0.1-SNAPSHOT";
}