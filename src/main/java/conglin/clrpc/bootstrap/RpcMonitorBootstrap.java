package conglin.clrpc.bootstrap;

/**
 * RPC monitor端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcMonitorBootstrap bootstrap = ...; 
 *     bootstrap.monitor()
 *              .monitorService("UserService")
 *              .monitorService("AddressService")
 *              .start();
 * </pre></blockquote>
 * 
 * 注意：结束后不要忘记关闭监视器，释放资源。
 */
public interface RpcMonitorBootstrap{

    /**
     * 使用配置文件的形式
     * 设置ZooKeeper的地址和根路径
     * @return
     */
    RpcMonitorBootstrap monitor();

    /**
     * 使用参数形式
     * 设置ZooKeeper的地址和根路径
     * @param zooKeeperAddress ZooKeeper 地址
     * @param rootPath ZooKeeper根路径
     * @return
     */
    RpcMonitorBootstrap monitor(String zooKeeperAddress, String rootPath);

    /**
     * 监视服务
     * 默认监视所有服务
     * @return
     */
    RpcMonitorBootstrap monitorService();

    /**
     * 监视指定的服务
     * @param serviceName
     * @return
     */
    RpcMonitorBootstrap monitorService(String serviceName);

    /**
     * 启动监视器
     * 启动前要调用 <strong>monitor</strong> 方法
     * @throws InterruptedException
     */
    void start() throws InterruptedException;

    /**
     * 关闭监视器
     * @throws InterruptedException
     */
    void stop()  throws InterruptedException;
}