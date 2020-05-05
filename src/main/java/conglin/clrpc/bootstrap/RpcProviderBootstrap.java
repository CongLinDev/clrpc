package conglin.clrpc.bootstrap;

import java.util.Collection;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcProviderOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.service.annotation.AnnotationParser;
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
 * bootstrap.publish(new ServiceBean()).hookStop().start();
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

    /**
     * @see #RpcProviderBootstrap(PropertyConfigurer)
     */
    public RpcProviderBootstrap() {
        this(null);
    }

    /**
     * 创建 服务提供者 启动对象
     * 
     * @param configurer 配置器
     */
    public RpcProviderBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ProviderServiceHandler(configurer());
        PROVIDER_TRANSFER = new ProviderTransfer();
    }

    /**
     * 发布单例服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param serviceBean 服务实现对象
     * @return
     */
    public RpcProviderBootstrap publish(Object serviceBean) {
        Class<?> clazz = serviceBean.getClass();
        doPublish(clazz).forEach(serviceName -> {
            SERVICE_HANDLER.publish(serviceName, serviceBean);
            LOGGER.info("Publish service named {} with bean.", serviceName);
        });
        return this;
    }

    /**
     * 发布服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param serviceFactory 服务工厂对象
     * @return
     */
    public RpcProviderBootstrap publishFactory(Supplier<?> serviceFactory) {
        Class<?> clazz = serviceFactory.get().getClass();
        doPublish(clazz).forEach(serviceName -> {
            SERVICE_HANDLER.publishFactory(serviceName, serviceFactory);
            LOGGER.info("Publish service named {} with factory.", serviceName);
        });
        return this;
    }

    /**
     * 发布服务具体方法
     * 
     * @param clazz
     * @return 服务名列表
     */
    protected Collection<String> doPublish(Class<?> clazz) {
        Collection<String> superServiceNames = AnnotationParser.superServiceNames(clazz,
                SERVICE_HANDLER::publishServiceMetaInfo);
        if (superServiceNames.isEmpty()) {
            LOGGER.error("Please Add a service name for {} by @Service.", clazz);
            throw new UnsupportedOperationException();
        }
        return superServiceNames;
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
        LOGGER.info("RpcProvider is starting.");
        super.start();
        ProviderContext context = initContext(option);

        SERVICE_HANDLER.start(context);
        PROVIDER_TRANSFER.start(context);
    }

    /**
     * 关闭
     */
    public void stop() {
        LOGGER.info("RpcProvider is stopping.");
        SERVICE_HANDLER.stop();
        PROVIDER_TRANSFER.stop();
        super.stop();
    }

    /**
     * 关闭钩子
     * 
     * @return this
     */
    public RpcProviderBootstrap hookStop() {
        hook(this::stop);
        return this;
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
        context.setSerializationHandler(option.serializationHandler());

        return context;
    }

}