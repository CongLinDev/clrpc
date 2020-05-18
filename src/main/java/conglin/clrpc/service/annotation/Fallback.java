package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解标记于服务接口上 用于声明 fallback 功能
 * 
 * {@link Fallback#value()} Fallback 的 class 全限定名，要求该 class 需要提供一个无参构造方法
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    /**
     * Fallback 的 class 全限定名
     * 
     * 要求该 class 需要提供一个无参构造方法
     */
    String value();
}