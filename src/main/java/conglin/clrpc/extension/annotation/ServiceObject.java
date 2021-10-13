package conglin.clrpc.extension.annotation;

import java.lang.annotation.*;

/**
 * @see conglin.clrpc.service.ServiceObject
 * @see conglin.clrpc.service.Service
 * @see conglin.clrpc.service.ServiceVersion
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceObject {
    /**
     * name
     *
     * @return
     */
    String name() default "";

    /**
     * 元信息
     *
     * @return
     */
    MetaInfo metaInfo() default @MetaInfo;

    /**
     * version
     *
     * @return
     */
    ServiceVersion version() default @ServiceVersion;
}
