package conglin.clrpc.executor;

import java.util.function.Consumer;

import conglin.clrpc.executor.pipeline.AbstractChainExecutor;
import conglin.clrpc.invocation.message.Message;
import conglin.clrpc.invocation.message.ResponsePayload;

public class ResponseExecutor extends AbstractChainExecutor {

    private final Consumer<Object> sender;

    public ResponseExecutor(Consumer<Object> sender) {
        this.sender = sender;
    }


    @Override
    public void outbound(Object object) {
        if (object instanceof Message message && message.payload() instanceof ResponsePayload response) {
            this.sender.accept(message);
        } else {
            super.nextInbound(object);
        }
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 1;
    }
}
