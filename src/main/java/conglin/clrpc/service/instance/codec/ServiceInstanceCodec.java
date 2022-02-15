package conglin.clrpc.service.instance.codec;

import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.instance.ServiceInstance;

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
    String toString(ServiceObject<?> serviceObject, String address);
}
