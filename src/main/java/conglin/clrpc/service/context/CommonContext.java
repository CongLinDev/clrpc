package conglin.clrpc.service.context;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;

public interface CommonContext {

    /**
     * 获取线程池
     * 
     * @return
     */
    ExecutorService getExecutorService();

    /**
     * 设置线程池
     * 
     * @param executorService
     */
    void setExecutorService(ExecutorService executorService);

    /**
     * 获取配置信息
     * 
     * @return
     */
    PropertyConfigurer getPropertyConfigurer();

    /**
     * 设置配置信息
     * 
     * @param propertyConfigurer
     */
    void setPropertyConfigurer(PropertyConfigurer propertyConfigurer);

    /**
     * 获取本地地址
     * 
     * @return
     */
    InetSocketAddress getLocalAddress();

    /**
     * 设置本地地址
     */
    void setLocalAddress(InetSocketAddress localAddress);

    /**
     * 获取序列化处理器
     * 
     * @return
     */
    SerializationHandler getSerializationHandler();

    /**
     * 设置序列化处理器
     * 
     * @param serializationHandler
     */
    void setSerializationHandler(SerializationHandler serializationHandler);

    /**
     * 获取本机的元信息
     * 
     * @return
     */
    String getMetaInformation();

    /**
     * 设置本机的元信息
     * 
     * @param metaInfo
     */
    void setMetaInformation(String metaInfo);
    
    /**
     * 对于扩展所需的对象则放入该 {@link java.util.Map} 
     * 
     * @return
     */
    Map<String, Object> getExtensionObject();
}