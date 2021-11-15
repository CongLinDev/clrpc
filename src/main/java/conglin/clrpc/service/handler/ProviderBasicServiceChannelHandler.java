package conglin.clrpc.service.handler;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderBasicServiceChannelHandler extends ProviderAbstractServiceChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBasicServiceChannelHandler.class);

    @Override
    protected boolean accept(Message msg) {
        return msg.payload() instanceof RequestPayload;
    }

    @Override
    public void init() {

    }

    @Override
    protected ResponsePayload execute(Message msg) {
        try {
            LOGGER.debug("Receive basic request messageId={}", msg.messageId());
            RequestPayload request = (RequestPayload)msg.payload();
            ServiceObject serviceObject = findServiceBean(request.serviceName());
            Object result = jdkReflectInvoke(serviceObject.object(), request);
            return new ResponsePayload(result);
        } catch (UnsupportedServiceException | ServiceExecutionException e) {
            LOGGER.error("Request failed: {}", e.getMessage());
            return new ResponsePayload(true, e);
        }
    }
}