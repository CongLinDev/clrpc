package conglin.clrpc.transport.router;

import java.util.function.Consumer;

import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.transport.message.Message;

public class RouterResult {
    private final ServiceInstance instance;
    private final Consumer<Message> sender;

    /**
     * @param target
     * @param sender
     */
    public RouterResult(ServiceInstance instance, Consumer<Message> sender) {
        this.instance = instance;
        this.sender = sender;
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
    public void send(Message message) {
        sender.accept(message);
    }
}
