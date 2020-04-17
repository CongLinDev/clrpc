package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于方法上，标识该方法是事务方法,该方法的返回值必须为 {@link conglin.clrpc.common.DataCallback}
 * 
 * 当事务需要提交时, 调用 {@link conglin.clrpc.common.DataCallback#success(Object)}，并调用 {@link conglin.clrpc.common.DataCallback#data()} 返回结果
 * 
 * 当事务需要回滚时，调用 {@link conglin.clrpc.common.DataCallback#fail(Exception)}
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
}