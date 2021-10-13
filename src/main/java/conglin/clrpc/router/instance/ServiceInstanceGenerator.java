package conglin.clrpc.router.instance;

import conglin.clrpc.service.ServiceObject;

public interface ServiceInstanceGenerator {
    /**
     * generate instance
     *
     * @param object
     * @param address
     * @return
     */
    ServiceInstance instance(ServiceObject object, String address);
}
