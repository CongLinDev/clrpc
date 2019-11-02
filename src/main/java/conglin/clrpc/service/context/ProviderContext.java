package conglin.clrpc.service.context;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ProviderContext extends CommonContext {

    /**
     * 获取服务对象持有者
     * @return
     */
    Function<String, Object> getObjectsHolder();

    /**
     * 设置服务对象持有者
     * @param objectHolder
     */
    void setObjectsHolder(Function<String, Object> objectHolder);

    /**
     * 获得服务注册器
     * @return
     */
    Consumer<String> getServiceRegister();

    /**
     * 设置服务注册器
     * @param serviceRegister
     */
    void setServiceRegister(Consumer<String> serviceRegister);

}