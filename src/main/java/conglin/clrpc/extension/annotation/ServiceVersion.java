package conglin.clrpc.extension.annotation;

import java.lang.annotation.*;

/**
 * 服务版本 major.minor.build
 *
 * @see conglin.clrpc.service.ServiceVersion
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceVersion {

    int major() default 1;

    int minor() default 0;

    int build() default 0;
}
