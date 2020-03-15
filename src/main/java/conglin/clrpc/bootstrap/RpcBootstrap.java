package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;

abstract public class RpcBootstrap {

    private final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(null);
    }

    public RpcBootstrap(PropertyConfigurer configurer) {
        this.CONFIGURER = (configurer != null) ? configurer : JsonPropertyConfigurer.fromFile();
    }

    /**
     * 准备
     */
    protected void start() {
        GlobalResourceManager.manager().acquire();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.manager().release();
    }
    
    /**
     * 虚拟机钩子
     * 
     * @param runnable
     */
    public void hook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }

    /**
     * 返回配置器
     * 
     * @return
     */
    protected PropertyConfigurer configurer() {
        return CONFIGURER;
    }
}