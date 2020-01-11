package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;

abstract public class RpcBootstrap {

    protected final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(null);
    }

    public RpcBootstrap(PropertyConfigurer configurer) {
        if (configurer == null) {
            this.CONFIGURER = JsonPropertyConfigurer.fromFile(); // default configurer
        } else {
            this.CONFIGURER = configurer;
        }
    }

    /**
     * 准备
     */
    protected void start() {
        GlobalResourceManager.manager().retain();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.manager().release();
    }

}