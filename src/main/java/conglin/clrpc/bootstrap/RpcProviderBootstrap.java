package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcProviderOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.IPAddressUtils;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderBootstrap.class);

    // 管理传输
    private final ProviderTransfer PROVIDER_TRANSFER;

    // 管理服务
    private final ProviderServiceHandler SERVICE_HANDLER;

    public RpcProviderBootstrap() {
        super();
        SERVICE_HANDLER = new ProviderServiceHandler(CONFIGURER);
        PROVIDER_TRANSFER = new ProviderTransfer(); 
    }

    public RpcProviderBootstrap(String configFilename) {
        super(configFilename);
        SERVICE_HANDLER = new ProviderServiceHandler(CONFIGURER);
        PROVIDER_TRANSFER = new ProviderTransfer(); 
    }

    public RpcProviderBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ProviderServiceHandler(configurer);
        PROVIDER_TRANSFER = new ProviderTransfer();
    }

    /**
     * 保存即将发布的服务
     * @param <T>
     * @param interfaceClass    服务接口类
     * @param serviceBeanClass  服务实现类，该类必须提供一个无参构造函数
     * @return
     */
    public <T> RpcProviderBootstrap publish(Class<T> interfaceClass, Class<? extends T> serviceBeanClass) {
        return publish(interfaceClass.getSimpleName(), serviceBeanClass);
    }

    /**
     * 保存即将发布的服务
     * @param serviceBeanClass 类名必须满足 'xxxServiceImpl' 格式
     * @return
     */
    public RpcProviderBootstrap publish(Class<?> serviceBeanClass) {
        String serviceBeanClassName = serviceBeanClass.getSimpleName();
        if (!serviceBeanClassName.endsWith("ServiceImpl")) {
            LOGGER.error(serviceBeanClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
            return this;
        } else {
            return publish(serviceBeanClassName.substring(0, serviceBeanClassName.length() - 4),
                    serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName 服务名
     * @param serviceBeanClass 服务实现类，该类必须提供一个无参构造函数
     * @return
     */
    public RpcProviderBootstrap publish(String serviceName, Class<?> serviceBeanClass) {
        if (serviceBeanClass.isInterface()) {
            LOGGER.error(serviceBeanClass.getName() + " is not a service class. And it will not be published");
        } else {
            try {
                Object serviceBean = serviceBeanClass.getDeclaredConstructor().newInstance();
                return publish(serviceName, serviceBean);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error("Can not publish service. " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * 保存即将发布的服务
     * @param serviceName 服务名
     * @param serviceBean 服务实现对象
     * @return
     */
    public RpcProviderBootstrap publish(String serviceName, Object serviceBean) {
        LOGGER.info("Publish service named " + serviceName);
        SERVICE_HANDLER.publish(serviceName, serviceBean);
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
        SERVICE_HANDLER.removeService(serviceName);
        return this;
    }

    /**
     * 启动
     * 该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭
     * 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     */
    public void start() {
        start(new RpcProviderOption());
    }
    
    /**
     * 启动
     * 该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭
     * 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     * @param option 启动选项
     */
    public void start(RpcProviderOption option){
        ProviderContext context = new BasicProviderContext();
        // 设置本地地址
        context.setLocalAddress(IPAddressUtils.getHostnameAndPort(CONFIGURER.getOrDefault("provider.port", 5100)));
        // 设置属性配置器
        context.setPropertyConfigurer(CONFIGURER);
        // 设置cache管理器
        context.setCacheManager(CACHE_MANAGER);
        // 设置序列化处理器
        context.setSerializationHandler(option.getSerializationHandler());

        SERVICE_HANDLER.start(context);
        PROVIDER_TRANSFER.start(context);
    }

    /**
     * 关闭
     */
    public void stop() {
        SERVICE_HANDLER.stop();
        PROVIDER_TRANSFER.stop();
    }

}