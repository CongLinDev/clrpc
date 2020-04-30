package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import conglin.clrpc.common.CallbackFunction;

/**
 * 该注解用于方法上，标识该方法是事务方法,该方法的返回值必须为 {@link conglin.clrpc.common.CallbackFunction }
 * 
 * 对于事务需要提交或回滚时，调用 {@link CallbackFunction#apply(Boolean)}
 * 
 * 提交的参数为 {@code ture} 回滚的参数为 {@code false}
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {

    /**
     * 预提交方法名
     * 
     * 指向本对象的一个方法，该方法的返回值必须为 {@link conglin.clrpc.common.CallbackFunction}
     * 
     * @return
     */
    String precommit();
}