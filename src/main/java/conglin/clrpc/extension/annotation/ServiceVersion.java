package conglin.clrpc.extension.annotation;


/**
 * 服务版本 major.minor.build
 *
 * @see conglin.clrpc.service.ServiceVersion
 */
public @interface ServiceVersion {

    int major() default 1;

    int minor() default 0;

    int build() default 0;
}
