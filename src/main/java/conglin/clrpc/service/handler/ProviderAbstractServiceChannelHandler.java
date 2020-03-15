package conglin.clrpc.service.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.annotation.AnnotationParser;
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
        LOGGER.error("ExceptionCaught : {}", cause);
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
            throw new UnsupportedServiceException(serviceName);
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
        BasicResponse response = new BasicResponse(request.getMessageId());

        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = ClassUtils.getClasses(parameters);
        LOGGER.debug("Invoking class={} method={}", serviceBeanClass.getName(), methodName);

        try {
            Method method = serviceBeanClass.getMethod(methodName, parameterTypes);

            // 服务是否被忽略
            if (AnnotationParser.enableServiceMethod(method))
                throw new ServiceExecutionException(request, new NoSuchMethodException(methodName));
            
            method.setAccessible(true);
            response.setResult(method.invoke(serviceBean, parameters));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(request, e);
        }
        return response;
    }
}