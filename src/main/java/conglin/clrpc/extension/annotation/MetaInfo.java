package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.config.MapPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;

/**
 * 元信息
 * 
 * @see conglin.clrpc.service.ServiceObject
 * @see conglin.clrpc.extension.annotation.ServiceObject
 */
public @interface MetaInfo {

    /**
     * mataClass
     * 
     * 默认是 MapPropertyConfigurer
     * 
     * @return
     */
    Class<? extends PropertyConfigurer> metaClass() default MapPropertyConfigurer.class;

    /**
     * entries
     * 
     * @return
     */
    Entry[] entries() default {};
}
