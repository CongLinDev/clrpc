package conglin.clrpc.extension.annotation;

/**
 * 元信息
 * 
 * @see conglin.clrpc.service.ServiceObject
 * @see conglin.clrpc.extension.annotation.ServiceObject
 */
public @interface MetaInfo {

    /**
     * entries
     * 
     * @return
     */
    Entry[] entries() default {};
}
