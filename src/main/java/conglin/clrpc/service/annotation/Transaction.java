package conglin.clrpc.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于方法上，标识该方法是事务方法
 * 
 * 其中 {@link #commit()} 指向提交的方法名，其参数类型为事务方法的返回值类型
 * 
 * 其中 {@link #rollback()} 指向回滚的方法名，其参数类型为事务方法的返回值类型
 * 
 * 若不存在 {@link #commit()} 或 {@link #rollback()} 指向的方法，默认直接提交
 * 
 * 对于事务请求来说，事务方法和事务提交方法分别产生了一个结果，若事务提交方法产生的结果不为空，则返回事务提交方法产生的结果 反之，返回事务方法产生的结果
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {

    /**
     * 提交方法
     * 
     * @return
     */
    String commit() default "";

    /**
     * 回滚方法
     * 
     * @return
     */
    String rollback() default "";
}