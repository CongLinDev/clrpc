package conglin.clrpc.service.instance;

import conglin.clrpc.service.ServiceObject;

abstract public class AbstractServiceInstance implements ServiceInstance {

    protected final ServiceObject<?> serviceObject;

    protected final String id;
    protected final String address;

    public AbstractServiceInstance(String id, String address, ServiceObject<?> serviceObject) {
        this.id = id == null ? address : id;
        this.address = address;
        this.serviceObject = serviceObject;
        serviceObject.metaInfo().putIfAbsent(ServiceInstance.INSTANCE_ID, id);
        serviceObject.metaInfo().putIfAbsent(ServiceInstance.INSTANCE_ADDRESS, address);
    }

    @Override
    public ServiceObject<?> serviceObject() {
        return serviceObject;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public String name() {
        return serviceObject.name();
    }

    @Override
    abstract public String toString();
}
