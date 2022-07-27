package conglin.clrpc.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.executor.pipeline.CommonChainExecutor;
import conglin.clrpc.invocation.UnsupportedServiceException;
import conglin.clrpc.invocation.message.Message;
import conglin.clrpc.invocation.message.RequestPayload;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Initializable;
import conglin.clrpc.service.ServiceObjectHolder;

public class CommonRequestExecutor extends CommonChainExecutor implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRequestExecutor.class);
    private ServiceObjectHolder serviceObjectHolder;

    @Override
    public void init() {
        serviceObjectHolder = getContext().getWith(ComponentContextEnum.SERVICE_OBJECT_HOLDER);
    }

    @Override
    public void inbound(Object object) {
        if (object instanceof Message message && message.payload() instanceof RequestPayload request) {
            try {
                Object result = serviceObjectHolder.invoke(request.serviceName(), request.methodName(),
                        request.parameters());
                super.nextInbound(new Message(message.messageId(), new ResponsePayload(result)));
            } catch (UnsupportedServiceException | ServiceExecutionException e) {
                LOGGER.error("Request failed: ", e);
                super.nextInbound(new Message(message.messageId(), new ResponsePayload(true, e)));
            }
        } else {
            super.nextInbound(object);
        }
    }

    @Override
    public int order() {
        return 10;
    }
}
