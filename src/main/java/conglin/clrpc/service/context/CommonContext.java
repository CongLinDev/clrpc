package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.registry.ServiceLogger;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.role.Role;

public interface CommonContext {

    /**
     * 返回当前角色
     * 
     * @return
     */
    Role role();

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
     * 获取服务记录器
     * 
     * @return
     */
    ServiceLogger getSerivceLogger();

    /**
     * 设置服务记录器
     * 
     * @param serviceLogger
     */
    void setServiceLogger(ServiceLogger serviceLogger);
}