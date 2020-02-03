package conglin.clrpc.bootstrap.monitor;

import java.util.Collection;

import conglin.clrpc.bootstrap.RpcBootstrap;
import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.registry.ServiceMonitor;
import conglin.clrpc.registry.ZooKeeperServiceMonitor;

abstract public class AbstractRpcMonitorBootstrap extends RpcBootstrap implements RpcMonitorBootstrap {

    private final ServiceMonitor serviceMonitor;

    public AbstractRpcMonitorBootstrap() {
        this(null);
    }

    public AbstractRpcMonitorBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        serviceMonitor = new ZooKeeperServiceMonitor(IPAddressUtils.localAddressString(), CONFIGURER);
    }

    @Override
    public RpcMonitorBootstrap monitor() {
        serviceMonitor.monitor(this::handleProvider, this::handleConsumer);
        return this;
    }

    @Override
    public RpcMonitorBootstrap monitor(String serviceName) {
        serviceMonitor.monitor(serviceName, this::handleProvider, this::handleConsumer);
        return this;
    }

    @Override
    public RpcMonitorBootstrap monitor(Class<?> serviceClass) {
        return monitor(getServiceName(serviceClass));
    }

    @Override
    public Collection<String> listServices() {
        return serviceMonitor.listServices();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * 处理服务消费者节点和数据
     * 
     * @param serviceName
     * @param nodeList
     */
    abstract protected void handleConsumer(String serviceName, Collection<Pair<String, String>> nodeList);

    /**
     * 处理服务提供者节点和数据
     * 
     * @param serviceName
     * @param nodeList
     */
    abstract protected void handleProvider(String serviceName, Collection<Pair<String, String>> nodeList);
}