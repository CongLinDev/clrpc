package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcProviderOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.service.context.BasicProviderContext;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.ProviderTransfer;
import io.netty.bootstrap.ServerBootstrap;

/**
 * RPC provider端启动类
 * 
 * 使用如下代码启动
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
 * bootstrap.publish(ServiceBean1.class).publish("service2", new ServiceBean2()).hookStop().start();
 * 
 * </pre>
 * 
 * </blockquote>
 * 
 * 注意：若服务接口相同，先添加的服务会被覆盖。 结束后不要忘记关闭服务端，释放资源。
 */

public class RpcProviderBootstrap extends RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderBootstrap.class);

    // 管理传输
    private final ProviderTransfer PROVIDER_TRANSFER;

    // 管理服务
    private final ProviderServiceHandler SERVICE_HANDLER;

    public RpcProviderBootstrap() {
        this(null);
    }

    public RpcProviderBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ProviderServiceHandler(configurer());
        PROVIDER_TRANSFER = new ProviderTransfer();
    }

    /**
     * 保存即将发布的服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param serviceBeanClass 该类必须提供一个无参构造方法
     * @return
     */
    public RpcProviderBootstrap publish(Class<?> serviceBeanClass) {
        try {
            Object serviceBean = serviceBeanClass.getDeclaredConstructor().newInstance();
            return publish(serviceBean);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Can not publish service. Cause: {}", e.getMessage());
        }
        return this;
    }

    /**
     * 保存即将发布的服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param serviceBean 服务实现对象
     * @return
     */
    public RpcProviderBootstrap publish(Object serviceBean) {
        getSuperServiceName(serviceBean.getClass()).forEach(serviceName -> publish(serviceName, serviceBean));
        return this;
    }

    /**
     * 保存即将发布的服务
     * 
     * @param serviceName 服务名
     * @param serviceBean 服务实现对象
     * @return
     */
    public RpcProviderBootstrap publish(String serviceName, Object serviceBean) {
        if (serviceName == null)
            throw new NullPointerException();
        SERVICE_HANDLER.publish(serviceName, serviceBean);
        LOGGER.info("Publish service named {}.", serviceName);
        return this;
    }

    /**
     * 启动。该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     */
    public void start() {
        start(new RpcProviderOption());
    }

    /**
     * 启动。该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     * 
     * @param option 启动选项
     */
    public void start(RpcProviderOption option) {
        super.start();
        ProviderContext context = initContext(option);

        SERVICE_HANDLER.start(context);
        PROVIDER_TRANSFER.start(context);
    }

    /**
     * 关闭
     */
    public void stop() {
        SERVICE_HANDLER.stop();
        PROVIDER_TRANSFER.stop();
        super.stop();
    }

    /**
     * 虚拟机钩子
     * 
     * @param runnable
     * @return
     */
    public RpcProviderBootstrap hook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
        return this;
    }

    /**
     * 关闭钩子
     * 
     * @return this
     */
    public RpcProviderBootstrap hookStop() {
        return hook(this::stop);
    }

    /**
     * 初始化上下文
     * 
     * @param option
     * @return
     */
    private ProviderContext initContext(RpcProviderOption option) {
        ProviderContext context = new BasicProviderContext();

        // 设置属性配置器
        context.setPropertyConfigurer(configurer());
        // 设置序列化处理器
        context.setSerializationHandler(option.getSerializationHandler());

        return context;
    }

}