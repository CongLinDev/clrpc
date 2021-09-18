package conglin.clrpc.service.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class ProviderAbstractServiceChannelHandler<T> extends SimpleChannelInboundHandler<T> implements ContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAbstractServiceChannelHandler.class);

    private RpcContext context;

    private ChannelPipeline pipeline;

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    public RpcContext getContext() {
        return context;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.pipeline = ctx.pipeline();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        ExecutorService executorService = context.getWith(RpcContextEnum.EXECUTOR_SERVICE);
        executorService.submit(() -> {
            next(msg, execute(msg));
        });
    }

    /**
     * pipeline
     *
     * @return
     */
    protected ChannelPipeline pipeline() {
        return pipeline;
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
     * @param msg
     * @param result
     */
    protected void next(T msg, Object result) {
        if (result != null)
            pipeline.fireChannelRead(new Pair<T, BasicResponse>(msg, (BasicResponse) result));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }

    /**
     * 查找服务对象
     * 
     * @param serviceName
     * @return
     * @throws UnsupportedServiceException
     */
    protected ServiceObject findServiceBean(String serviceName) throws UnsupportedServiceException {
        // 获取服务实现类
        Map<String, ServiceObject> serviceObjectHolder = context.getWith(RpcContextEnum.SERVICE_OBJECT_HOLDER);
        ServiceObject serviceObject = serviceObjectHolder.get(serviceName);
        // 如果服务实现类没有注册，抛出异常
        if (serviceObject == null) {
            throw new UnsupportedServiceException(serviceName);
        }
        return serviceObject;
    }

    /**
     * 使用jdk反射来调用方法
     * 
     * @param serviceBean
     * @param request
     * @return
     * @throws ServiceExecutionException
     */
    protected Object jdkReflectInvoke(Object serviceBean, BasicRequest request) throws ServiceExecutionException {
        return jdkReflectInvoke(serviceBean, request.methodName(), request.parameters());
    }

    /**
     * 使用jdk反射来调用方法
     * 
     * @param serviceBean
     * @param methodName
     * @param parameters
     * @return
     * @throws ServiceExecutionException
     */
    protected Object jdkReflectInvoke(Object serviceBean, String methodName, Object[] parameters)
            throws ServiceExecutionException {
        try {
            return ClassUtils.reflectInvoke(serviceBean, methodName, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(e);
        }
    }

    /**
     * 使用jdk反射来调用方法
     * 
     * @param serviceBean
     * @param method
     * @param parameters
     * @return
     * @throws ServiceExecutionException
     */
    protected Object jdkReflectInvoke(Object serviceBean, Method method, Object[] parameters)
            throws ServiceExecutionException {
        try {
            return ClassUtils.reflectInvoke(serviceBean, method, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(e);
        }
    }
}