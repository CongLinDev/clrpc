package conglin.clrpc.extension.annotation;

import conglin.clrpc.service.instance.condition.DefaultInstanceCondition;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailFast;
import conglin.clrpc.service.strategy.FailStrategy;

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
     * 失败策略
     * 
     * @return
     */
    Class<? extends FailStrategy> failStrategy() default FailFast.class;

    /**
     * version
     *
     * @return
     */
    ServiceVersion version() default @ServiceVersion;

    /**
     * condition class
     * 
     * @return
     */
    Class<? extends InstanceCondition> conditionClass() default DefaultInstanceCondition.class;
}
