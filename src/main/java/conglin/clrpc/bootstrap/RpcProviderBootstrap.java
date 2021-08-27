package conglin.clrpc.bootstrap;

import conglin.clrpc.common.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.ProviderServiceHandler;
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
 * bootstrap.publish(new ServiceImpl1()).publishFactory(ServiceImpl2::new).hookStop().start();
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
     * @param serviceObject 服务对象
     * @return
     */
    public RpcProviderBootstrap publish(ServiceObject serviceObject) {
        String serviceName = serviceObject.name();
        SERVICE_HANDLER.publish(serviceObject);
        SERVICE_HANDLER.publishServiceMetaInfo(serviceName, serviceObject.metaInfo().toString());
        return this;
    }

    @Override
    final public Role role() {
        return Role.PROVIDER;
    }

    /**
     * 启动。该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     */
    public void start() {
        start(new RpcOption());
    }

    /**
     * 启动。该方法会一直阻塞，直到Netty的{@link ServerBootstrap} 被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     * 
     * @param option 启动选项
     */
    public void start(RpcOption option) {
        LOGGER.info("RpcProvider is starting.");
        super.start();
        RpcContext context = initContext(option);
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
    private RpcContext initContext(RpcOption option) {
        RpcContext context = new RpcContext();
        // 设置角色
        context.put(RpcContextEnum.ROLE, role());
        // 设置属性配置器
        context.put(RpcContextEnum.PROPERTY_CONFIGURER, configurer());
        // 设置序列化处理器
        context.put(RpcContextEnum.SERIALIZATION_HANDLER, ClassUtils.loadObject(configurer().get(role().item(".message.serializationHandlerClassName"), String.class)));
        return context;
    }

}