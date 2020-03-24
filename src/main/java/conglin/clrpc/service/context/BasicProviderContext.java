package conglin.clrpc.service.context;

import java.util.function.Consumer;
import java.util.function.Function;

public class BasicProviderContext extends BasicCommonContext implements ProviderContext {

    @Override
    public String role() {
        return "provider";
    }

    private Function<String, Object> objectHolder;

    @Override
    public Function<String, Object> getObjectsHolder() {
        return objectHolder;
    }

    @Override
    public void setObjectsHolder(Function<String, Object> objectHolder) {
        this.objectHolder = objectHolder;
    }

    private Consumer<String> serviceRegister;

    @Override
    public Consumer<String> getServiceRegister() {
        return serviceRegister;
    }

    @Override
    public void setServiceRegister(Consumer<String> serviceRegister) {
        this.serviceRegister = serviceRegister;
    }
}