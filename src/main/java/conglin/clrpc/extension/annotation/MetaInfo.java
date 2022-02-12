package conglin.clrpc.extension.annotation;

import java.lang.annotation.*;

/**
 * 元信息
 * 
 * @see conglin.clrpc.service.ServiceObject
 * @see conglin.clrpc.extension.annotation.ServiceObject
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaInfo {

    /**
     * entries
     * 
     * @return
     */
    Entry[] entries() default {};
}
