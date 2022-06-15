package conglin.clrpc.transport.component;

import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.message.Message;

public interface InvocationExecutor {
    
    /**
     * execute
     * 
     * @param invocationContext
     */
    void execute(InvocationContext invocationContext);

    /**
     * 接收消息
     * 
     * @param message
     */
    void receive(Message message);
}
