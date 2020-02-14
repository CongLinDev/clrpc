package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class ProviderBasicServiceChannelHandler extends ProviderAbstractServiceChannelHandler<BasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBasicServiceChannelHandler.class);

    public ProviderBasicServiceChannelHandler(ProviderContext context) {
        super(context);
    }

    @Override
    protected Object execute(BasicRequest msg) {
        try {
            LOGGER.debug("Receive basic request requestId={}", msg.getRequestId());
            return doExecute(msg);
        } catch (UnsupportedServiceException | ServiceExecutionException e) {
            LOGGER.error("Request failed: {}", e.getMessage());
            BasicResponse response = new BasicResponse(msg.getRequestId(), true);
            response.setResult(e);
            return response;
        }
    }
}