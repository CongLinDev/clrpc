package conglin.clrpc.bootstrap;

import java.util.Collection;

/**
 * RPC monitor端启动类
 * 
 * 使用如下代码启动
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * RpcMonitorBootstrap bootstrap = ...; // choose you like 
 * bootstrap.monitor(Interface1.class)
 *          .monitor(Interface2.class)
 *          .hookStop()
 *          .start();
 * 
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
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param serviceClass
     * @return
     */
    RpcMonitorBootstrap monitor(Class<?> serviceClass);

    /**
     * 列出所有的可用服务
     * 
     * @return
     */
    Collection<String> listServices();

    /**
     * 启动监视器
     * 
     * 启动前要调用 {@link RpcMonitorBootstrap#monitor()} 或
     * {@link RpcMonitorBootstrap#monitor(Class)} 或
     * {@link RpcMonitorBootstrap#monitor(String)} 方法
     */
    void start();

    /**
     * 关闭监视器
     */
    void stop();

    /**
     * 关闭钩子
     * 
     * @return this
     */
    RpcMonitorBootstrap hookStop();
}