package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.annotation.Service;
import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;

abstract public class RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

    protected final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(null);
    }

    public RpcBootstrap(PropertyConfigurer configurer) {
        this.CONFIGURER = (configurer != null) ? configurer : JsonPropertyConfigurer.fromFile();
    }

    /**
     * 准备
     */
    protected void start() {
        GlobalResourceManager.manager().retain();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.manager().release();
    }

    /**
     * 返回服务类的服务名
     * 
     * @param serviceClass
     * @return
     */
    protected String getServiceName(Class<?> serviceClass) {
        Service service = serviceClass.getAnnotation(Service.class);
        if (service != null)
            return service.name();
        LOGGER.error("Unnamed service from {}", serviceClass.getName());
        return null;
    }

}