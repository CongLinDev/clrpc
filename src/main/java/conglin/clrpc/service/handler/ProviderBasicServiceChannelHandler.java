package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.context.channel.ProviderChannelContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class ProviderBasicServiceChannelHandler extends ProviderAbstractServiceChannelHandler<BasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBasicServiceChannelHandler.class);

    public ProviderBasicServiceChannelHandler(ProviderChannelContext context) {
        super(context);
    }

    @Override
    protected Object execute(BasicRequest msg) {
        try {
            LOGGER.debug("Receive basic request messageId={}", msg.messageId());
            Object serviceBean = findServiceBean(msg.serviceName());
            Object result = jdkReflectInvoke(serviceBean, msg);
            return new BasicResponse(msg.messageId(), result);
        } catch (UnsupportedServiceException | ServiceExecutionException e) {
            LOGGER.error("Request failed: {}", e.getMessage());
            return new BasicResponse(msg.messageId(), true, e);
        }
    }
}