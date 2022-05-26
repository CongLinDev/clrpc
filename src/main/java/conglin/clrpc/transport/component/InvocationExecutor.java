package conglin.clrpc.transport.component;

import conglin.clrpc.service.context.InvocationContext;

public interface InvocationExecutor {
    
    /**
     * execute
     * 
     * @param invocationContext
     */
    void execute(InvocationContext invocationContext);
}
