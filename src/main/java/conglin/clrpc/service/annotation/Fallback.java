package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import conglin.clrpc.service.fallback.DefaultFallbackFactory;
import conglin.clrpc.service.fallback.FallbackFactory;

/**
 * 该注解标记于服务接口上 用于声明 fallback 功能
 * 
 * {@link Fallback#factory()} 标记产生 Fallback 对象的工厂类，该类应当实现
 * {@link conglin.clrpc.service.fallback.FallbackFactory} 接口，并提供一个无参构造方法
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    /**
     * 创建 fallback 的工厂类
     * 
     * @return
     */
    Class<? extends FallbackFactory> factory() default DefaultFallbackFactory.class;
}