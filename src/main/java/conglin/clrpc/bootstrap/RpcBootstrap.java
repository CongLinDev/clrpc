package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.role.Role;

/**
 * 抽象的 RpcBootstrap
 * 
 * 用于保存配置对象、控制资源引用计数
 */
abstract public class RpcBootstrap {

    private final PropertyConfigurer CONFIGURER;

    /**
     * 创建启动对象
     * 
     * @param configurer 配置器
     */
    public RpcBootstrap(PropertyConfigurer configurer) {
        if(configurer == null) {
            throw new NullPointerException();
        }
        this.CONFIGURER = configurer;
    }

    /**
     * 准备
     */
    protected void start() {

    }

    /**
     * 销毁
     */
    protected void stop() {

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

    /**
     * 角色
     *
     * @return
     */
    abstract public Role role();
}