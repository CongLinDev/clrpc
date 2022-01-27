package conglin.clrpc.service.instance;

import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.ServiceVersion;

abstract public class AbstractServiceInstance implements ServiceInstance {

    protected final ServiceObject serviceObject;

    protected final String address;

    public AbstractServiceInstance(ServiceObject serviceObject, String address) {
        this.serviceObject = serviceObject;
        this.address = address;
    }

    public ServiceObject serviceObject() {
        return serviceObject;
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
    public ServiceVersion version() {
        return serviceObject.version();
    }

    @Override
    abstract public String toString();
}
