package conglin.clrpc.bootstrap;

/**
 * RPC monitor端启动类
 * 
 * 使用如下代码启动 <blockquote>
 * 
 * <pre>
 *     RpcMonitorBootstrap bootstrap = ...; // choose you like 
 *     bootstrap.monitor("UserService")
 *              .monitor("AddressService")
 *              .start();
 * </pre>
 * 
 * </blockquote>
 * 
 * 注意：结束后不要忘记关闭监视器，释放资源。
 */
public interface RpcMonitorBootstrap {

    /**
     * 监视服务 默认监视所有服务
     * 
     * @return
     */
    RpcMonitorBootstrap monitor();

    /**
     * 监视指定的服务
     * 
     * @param serviceName
     * @return
     */
    RpcMonitorBootstrap monitor(String serviceName);

    /**
     * 监视指定的服务
     * 
     * @param interfaceClass
     * @return
     */
    default RpcMonitorBootstrap monitor(Class<?> interfaceClass) {
        return monitor(interfaceClass.getSimpleName());
    }

    /**
     * 启动监视器 启动前要调用 <strong>monitor</strong> 方法
     */
    void start();

    /**
     * 关闭监视器
     */
    void stop();
}