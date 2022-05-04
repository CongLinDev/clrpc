package conglin.clrpc.thirdparty.zookeeper.registry;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.instance.AbstractServiceInstance;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

public class ZooKeeperServiceRegistry extends AbstractZooKeeperService
        implements ServiceRegistry, ComponentContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private ComponentContext componentContext;

    private String instanceId;
    private String instanceAddress;

    private ServiceInstanceCodec serviceInstanceCodec;

    public ZooKeeperServiceRegistry(UrlScheme url) {
        super(url, "service");
    }

    @Override
    public ComponentContext getContext() {
        return componentContext;
    }

    @Override
    public void setContext(ComponentContext context) {
        this.componentContext = context;
    }

    @Override
    public void init() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        this.instanceAddress = properties.getProperty("provider.instance.address");
        this.instanceId = properties.getProperty("provider.instance.id");
        if (instanceId == null) {
            throw new IllegalArgumentException("properties 'provider.instance.id' must not be null");
        }
        this.serviceInstanceCodec = getContext().getWith(ComponentContextEnum.SERVICE_INSTANCE_CODEC);
    }

    @Override
    public void registerProvider(ServiceObject<?> serviceObject) {
        ServiceInstance instance = new AbstractServiceInstance(instanceId, instanceAddress, serviceObject) {
            @Override
            public String toString() {
                return serviceInstanceCodec.toContent(this);
            }
        };
        String providerNode = buildPath(serviceObject.name(), "provider", instanceId);
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), providerNode, instance.toString(),
                CreateMode.EPHEMERAL) != null) {
            LOGGER.debug("register provider(id={}, name={}) successfully.", instanceId, serviceObject.name());
        } else {
            LOGGER.error("register provider(id={}, name={}) failed.", instanceId, serviceObject.name());
        }
    }

    @Override
    public void unregisterProvider(ServiceObject<?> serviceObject) {
        String providerNode = buildPath(serviceObject.name(), "provider", instanceId);
        if (ZooKeeperUtils.deleteNode(keeperInstance.instance(), providerNode) != null) {
            LOGGER.debug("Unregister provider(id={}, name={}) successfully.", instanceId, serviceObject.name());
        } else {
            LOGGER.error("Unregister provider(id={}, name={}) failed.", instanceId, serviceObject.name());
        }
    }

    @Override
    public void subscribeProvider(ServiceInterface<?> serviceInterface,
            Consumer<Collection<ServiceInstance>> callback) {
        String providerNodes = buildPath(serviceInterface.name(), "provider");
        ZooKeeperUtils.watchChildrenList(keeperInstance.instance(), providerNodes,
                values -> callback.accept(
                        values.stream()
                                .map(Pair::getSecond)
                                .map(serviceInstanceCodec::fromContent)
                                .toList()));
    }

    @Override
    public void registerConsumer(ServiceInterface<?> serviceInterface) {
        String consumerNode = buildPath(serviceInterface.name(), "consumer", instanceId);
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), consumerNode, "",
                CreateMode.EPHEMERAL) != null) {
            LOGGER.debug("register consumer(id={}, name={}) successfully.", instanceId, serviceInterface.name());
        } else {
            LOGGER.error("register consumer(id={}, name={}) failed.", instanceId, serviceInterface.name());
        }
    }

    @Override
    public void unregisterConsumer(ServiceInterface<?> serviceInterface) {
        String consumerNode = buildPath(serviceInterface.name(), "consumer", instanceId);
        if (ZooKeeperUtils.deleteNode(keeperInstance.instance(), consumerNode) != null) {
            LOGGER.debug("Unregister consumer(id={}, name={}) successfully.", instanceId, serviceInterface.name());
        } else {
            LOGGER.error("Unregister consumer(id={}, name={}) failed.", instanceId, serviceInterface.name());
        }
    }

    public List<ServiceInstance> listProviders(ServiceInterface<?> serviceInterface) {
        String providerNodes = buildPath(serviceInterface.name(), "provider");
        return ZooKeeperUtils.listChildrenData(keeperInstance.instance(), providerNodes)
                .stream()
                .map(serviceInstanceCodec::fromContent)
                .toList();
    }
}
