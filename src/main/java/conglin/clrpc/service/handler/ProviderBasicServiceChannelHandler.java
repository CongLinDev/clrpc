package conglin.clrpc.service.handler;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderBasicServiceChannelHandler extends ProviderAbstractServiceChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBasicServiceChannelHandler.class);

    @Override
    protected boolean accept(Payload payload) {
        return payload instanceof RequestPayload;
    }

    @Override
    protected ResponsePayload execute(Payload payload) {
        try {
            RequestPayload request = (RequestPayload)payload;
            ServiceObject serviceObject = findServiceBean(request.serviceName());
            Object result = jdkReflectInvoke(serviceObject.object(), request);
            return new ResponsePayload(result);
        } catch (UnsupportedServiceException | ServiceExecutionException e) {
            LOGGER.error("Request failed: ", e);
            return new ResponsePayload(true, e);
        }
    }
}