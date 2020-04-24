package conglin.clrpc.service.context;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ProviderContext extends CommonContext {

    /**
     * 获取单例服务对象 {@link java.util.Map}
     * 
     * @return
     */
    Map<String, Object> getObjectBeans();

    /**
     * 设置单例服务对象 {@link java.util.Map}
     * 
     * @param objectsHolder
     */
    void setObjectBeans(Map<String, Object> objectHolder);

    /**
     * 返回服务对象工厂 {@link java.util.Map}
     * 
     * @return
     */
    Map<String, Supplier<?>> getObjectFactories();

    /**
     * 设置服务对象工厂 {@link java.util.Map}
     * 
     * @param objectFactory
     */
    void setObjectFactories(Map<String, Supplier<?>> objectFactory);

    /**
     * 获得服务注册器
     * 
     * @return
     */
    Consumer<String> getServiceRegister();

    /**
     * 设置服务注册器
     * 
     * @param serviceRegister
     */
    void setServiceRegister(Consumer<String> serviceRegister);
}