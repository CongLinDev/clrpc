package conglin.clrpc.transport.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.annotation.CacheableService;
import conglin.clrpc.common.annotation.IgnoreService;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class ProviderAbstractServiceChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAbstractServiceChannelHandler.class);

    protected final ProviderContext context;

    private ChannelPipeline pipeline;

    public ProviderAbstractServiceChannelHandler(ProviderContext context) {
        this.context = context;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.pipeline = ctx.pipeline();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        context.getExecutorService().submit(() -> {
            next(msg, execute(msg));
        });
    }

    /**
     * 执行具体的业务逻辑
     * 
     * @param msg
     * @return
     */
    abstract protected Object execute(T msg);

    /**
     * 传递给下一个 ChannelHandler
     * 
     * @param object
     */
    protected void next(T msg, Object result) {
        if (result != null)
            pipeline.fireChannelRead(new Pair<T, BasicResponse>(msg, (BasicResponse)result));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }

    /**
     * 执行请求
     * 
     * @param request
     * @return
     * @throws UnsupportedServiceException
     * @throws ServiceExecutionException
     */
    protected BasicResponse doExecute(BasicRequest request)
            throws UnsupportedServiceException, ServiceExecutionException {
        String serviceName = request.getServiceName();
        // 获取服务实现类
        Object serviceBean = context.getObjectsHolder().apply(serviceName);
        // 如果服务实现类没有注册，抛出异常
        if (serviceBean == null) {
            throw new UnsupportedServiceException(request);
        }

        return jdkReflectInvoke(serviceBean, request);
    }

    /**
     * 使用jdk反射来调用方法
     * 
     * @param serviceBean
     * @param request
     * @return
     * @throws ServiceExecutionException
     */
    protected BasicResponse jdkReflectInvoke(Object serviceBean, BasicRequest request)
            throws ServiceExecutionException {
        BasicResponse response = new BasicResponse(request.getRequestId());

        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        LOGGER.debug("Invoking class={} method={}", serviceBeanClass.getName(), methodName);

        try {
            Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
            handleAnnotation(method, response);
            method.setAccessible(true);
            response.setResult(method.invoke(serviceBean, parameters));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(request, e);
        }
        return response;
    }

    /**
     * 处理注解
     * 
     * @param method
     * @param response
     * @throws NoSuchMethodException
     */
    protected void handleAnnotation(Method method, BasicResponse response) throws NoSuchMethodException {
        // 处理 {@link IgnoreService}
        IgnoreService ignoreService = method.getAnnotation(IgnoreService.class);
        if (ignoreService != null && ignoreService.ignore())
            throw new NoSuchMethodException(method.getName());

        // 处理{@link CacheableService}
        CacheableService cacheableService = method.getAnnotation(CacheableService.class);
        if (cacheableService == null)
            return;// 默认值为0，不必调用方法设置为0
        if (cacheableService.consumer()) {
            response.canCacheForConsumer();
            response.setExpireTime(cacheableService.exprie());
        }
        if (cacheableService.provider()) {
            response.canCacheForProvider();
            response.setExpireTime(cacheableService.exprie());
        }
    }
}