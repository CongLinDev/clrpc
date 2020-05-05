package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;

/**
 * 抽象的 RpcBootstrap
 * 
 * 用于保存配置对象、控制资源引用计数
 */
abstract public class RpcBootstrap {

    private final PropertyConfigurer CONFIGURER;

    /**
     * @see #RpcBootstrap(PropertyConfigurer)
     */
    public RpcBootstrap() {
        this(null);
    }

    /**
     * 创建启动对象
     * 
     * @param configurer 配置器
     */
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