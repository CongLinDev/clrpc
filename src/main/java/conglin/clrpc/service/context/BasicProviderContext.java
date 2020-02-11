package conglin.clrpc.service.context;

import java.util.function.Function;

import conglin.clrpc.common.Task;

public class BasicProviderContext extends BasicCommonContext implements ProviderContext {

    private Function<String, Object> objectHolder;

    @Override
    public Function<String, Object> getObjectsHolder() {
        return objectHolder;
    }

    @Override
    public void setObjectsHolder(Function<String, Object> objectHolder) {
        this.objectHolder = objectHolder;
    }
    

    private Task serviceRegister;

    @Override
    public Task getServiceRegister() {
        return serviceRegister;
    }

    @Override
    public void setServiceRegister(Task serviceRegister) {
        this.serviceRegister = serviceRegister;
    }
}