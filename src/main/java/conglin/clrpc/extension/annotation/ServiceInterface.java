package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.Fallback;

import java.lang.annotation.*;

/**
 * 该注解用于提供服务的接口的类型上
 *
 * @see conglin.clrpc.service.ServiceInterface
 *
 * 注意：该注解只能被使用在服务接口上。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceInterface {

    /**
     * name
     *
     * @return
     */
    String name() default "";

    /**
     * fallback
     *
     * @return
     */
    Class<? extends Fallback> fallback() default Fallback.class;

    /**
     * version
     *
     * @return
     */
    ServiceVersion version() default @ServiceVersion;
}
