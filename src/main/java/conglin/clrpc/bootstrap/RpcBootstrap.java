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
     * @param clazz
     * @return
     */
    public String resolveServiceName(Class<?> clazz) {
        Service service = clazz.getAnnotation(Service.class);
        if (service != null && service.enable())
            return service.name();
        LOGGER.warn("Unavailable service from {}", clazz.getName());
        return null;
    }

    /**
     * 获取父接口的注解服务名
     * 
     * 该方法只返回上一级接口的服务名 而不是所有接口的服务名
     * 
     * @param clazz
     * @return
     */
    public Collection<String> resolveSuperServiceName(Class<?> clazz) {
        return Stream.of(clazz.getInterfaces()).map(this::resolveServiceName).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 虚拟机钩子
     * 
     * @param runnable
     */
    public void hook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
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