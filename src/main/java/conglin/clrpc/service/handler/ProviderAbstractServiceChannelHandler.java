package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class ProviderAbstractServiceChannelHandler extends SimpleChannelInboundHandler<Message>
        implements ComponentContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAbstractServiceChannelHandler.class);

    private ComponentContext context;

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (accept(msg.payload())) {
            LOGGER.debug("Receive request messageId={}", msg.messageId());
            ResponsePayload response = execute(msg.payload());
            if (response != null) {
                ctx.fireChannelRead(new Message(msg.messageId(), response));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 执行具体的业务逻辑
     * 
     * @param payload
     * @return
     */
    abstract protected ResponsePayload execute(Payload payload);

    /**
     * 是否处理
     *
     * @param payload
     * @return
     */
    abstract protected boolean accept(Payload payload);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }

    /**
     * 使用反射 invoke request
     * 
     * @param request
     * @return
     * @throws UnsupportedServiceException
     * @throws ServiceExecutionException
     */
    protected Object invoke(RequestPayload request) throws UnsupportedServiceException, ServiceExecutionException {
        ServiceObjectHolder serviceObjectHolder = context.getWith(ComponentContextEnum.SERVICE_OBJECT_HOLDER);
        return serviceObjectHolder.invoke(request.serviceName(), request.methodName(), request.parameters());
    }
}