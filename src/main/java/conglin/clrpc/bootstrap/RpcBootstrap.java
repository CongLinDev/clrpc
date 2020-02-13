package conglin.clrpc.bootstrap;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;
import conglin.clrpc.service.annotation.Service;

abstract public class RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

    private final PropertyConfigurer CONFIGURER;

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
        GlobalResourceManager.manager().acquire();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.manager().release();
    }

    /**
     * 返回服务类的注解服务名
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

    /**
     * 获取父接口的注解服务名
     * 
     * @param serviceClass
     * @return
     */
    protected Collection<String> getSuperServiceName(Class<?> serviceClass) {
        return Stream.of(serviceClass.getInterfaces()).map(this::getServiceName).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 返回配置器
     * 
     * @return
     */
    protected PropertyConfigurer configurer() {
        return CONFIGURER;
    }
}