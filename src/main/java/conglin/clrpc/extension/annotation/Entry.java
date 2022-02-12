package conglin.clrpc.extension.annotation;

import java.lang.annotation.*;

/**
 * key value 信息
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry {

    /**
     * key
     * 
     * @return
     */
    String key();

    /**
     * value
     * 
     * @return
     */
    String value();
}
