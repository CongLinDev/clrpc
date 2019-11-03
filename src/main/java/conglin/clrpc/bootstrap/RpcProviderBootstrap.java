package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.codec.ProtostuffSerializationHandler;
import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.config.YamlPropertyConfigurer;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.service.context.BasicProviderContext;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transfer.ProviderTransfer;
import io.netty.bootstrap.ServerBootstrap;

/**
 * RPC provider端启动类
 * 
 * 使用如下代码启动 <blockquote>
 * 
 * <pre>
 * RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
 * bootstrap.publish("service1", ServiceBean1.class).publish("service2", new ServiceBean2())
 *         .publish(Interface3.class, Implement3.class).start();
 * </pre>
 * 
 * </blockquote>
 * 
 * 注意：若服务接口相同，先添加的服务会被覆盖。 结束后不要忘记关闭服务端，释放资源。
 */

public class RpcProviderBootstrap extends Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcProviderBootstrap.class);

    // 管理传输
    private ProviderTransfer providerTransfer;

    // 管理服务
    private ProviderServiceHandler serviceHandler;

    public RpcProviderBootstrap() {
        this(new YamlPropertyConfigurer());
    }

    public RpcProviderBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        serviceHandler = new ProviderServiceHandler(configurer);
        providerTransfer = new ProviderTransfer();
    }

    /**
     * 保存即将发布的服务
     * @param interfaceClass   接口类
     * @param serviceBeanClass 实现类
     * @return
     */
    public RpcProviderBootstrap publish(Class<?> interfaceClass, Class<?> serviceBeanClass) {
        if (!interfaceClass.isAssignableFrom(serviceBeanClass)) {
            log.error("Service is not permitted. Because "
                 + interfaceClass.getName() + "is not assignableFrom " + serviceBeanClass.getName());
            return this;
        } else {
            return publish(interfaceClass.getSimpleName(), serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceBeanClass 类名必须满足 'xxxServiceImpl' 格式
     * @return
     */
    public RpcProviderBootstrap publish(Class<?> serviceBeanClass) {
        String serviceBeanClassName = serviceBeanClass.getSimpleName();
        if (!serviceBeanClassName.endsWith("ServiceImpl")) {
            log.error(serviceBeanClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
            return this;
        } else {
            return publish(serviceBeanClassName.substring(0, serviceBeanClassName.length() - 4),
                    serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBeanClass
     * @return
     */
    public RpcProviderBootstrap publish(String serviceName, Class<?> serviceBeanClass) {
        if (serviceBeanClass.isInterface()) {
            log.error(serviceBeanClass.getName() + " is not a service class. And it will not be published");
        } else {
            try {
                Object serviceBean = serviceBeanClass.getDeclaredConstructor().newInstance();
                return publish(serviceName, serviceBean);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                log.error("Can not publish service. " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBean
     * @return
     */
    public RpcProviderBootstrap publish(String serviceName, Object serviceBean) {
        log.info("Publish service named " + serviceName);
        serviceHandler.publish(serviceName, serviceBean);
        return this;
    }

    /**
     * 移除已经发布的服务
     * @param interfaceClass
     * @return
     */
    public RpcProviderBootstrap removeService(Class<?> interfaceClass) {
        return removeService(interfaceClass.getSimpleName());
    }

    /**
     * 移除已经发布的服务
     * @param serviceName
     * @return
     */
    public RpcProviderBootstrap removeService(String serviceName) {
        serviceHandler.removeService(serviceName);
        return this;
    }

    /**
     * 启动
     * 该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭
     * 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     * 序列化处理器默认使用 {@link ProtostuffSerializationHandler}
     */
    public void start() {
        start(new ProtostuffSerializationHandler());
    }
    
    /**
     * 启动
     * 该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭
     * 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     * @param serializationHandler 序列化处理器
     */
    public void start(SerializationHandler serializationHandler){
        ProviderContext context = new BasicProviderContext();
        // 设置本地地址
        context.setLocalAddress(configurer.getOrDefault("provider.address", "localhost:5100"));
        // 设置属性配置器
        context.setPropertyConfigurer(configurer);
        // 设置cache管理器
        context.setCacheManager(cacheManager);
        // 设置序列化处理器
        context.setSerializationHandler(serializationHandler);

        serviceHandler.start(context);
        providerTransfer.start(context);
    }

    /**
     * 关闭
     */
    public void stop() {
        serviceHandler.stop();
        providerTransfer.stop();
    }

}