package conglin.clrpc.router.instance;

import conglin.clrpc.service.ServiceObject;

public interface ServiceInstanceCodec {

    /**
     * 转为 对象
     *
     * @param content
     * @return
     */
    ServiceInstance fromContent(String content);

    /**
     * 转为 string
     *
     * @param serviceObject
     * @param address
     * @return
     */
    String toString(ServiceObject serviceObject, String address);
}
