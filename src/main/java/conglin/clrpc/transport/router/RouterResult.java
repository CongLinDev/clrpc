package conglin.clrpc.transport.router;

import java.util.function.Consumer;

import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.transport.message.Message;

public class RouterResult {
    private final ServiceInstance instance;
    private final Consumer<Message> executor;

    /**
     * @param target
     * @param sender
     */
    public RouterResult(ServiceInstance instance, Consumer<Message> executor) {
        this.instance = instance;
        this.executor = executor;
    }

    /**
     * @return the instance
     */
    public ServiceInstance getInstance() {
        return instance;
    }

    /**
     * 发送 message
     * 
     * @param message
     */
    public void execute(Message message) {
        executor.accept(message);
    }
}
