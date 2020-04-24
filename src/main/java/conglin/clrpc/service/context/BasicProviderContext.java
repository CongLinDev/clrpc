package conglin.clrpc.service.context;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import conglin.clrpc.global.role.Role;

public class BasicProviderContext extends BasicCommonContext implements ProviderContext {

    @Override
    public Role role() {
        return Role.PROVIDER;
    }

    private Map<String, Object> objectBean;

    @Override
    public Map<String, Object> getObjectBeans() {
        return objectBean;
    }

    @Override
    public void setObjectBeans(Map<String, Object> objectBean) {
        this.objectBean = objectBean;
    }

    private Map<String, Supplier<?>> objectFactories;

    @Override
    public Map<String, Supplier<?>> getObjectFactories() {
        return objectFactories;
    }

    @Override
    public void setObjectFactories(Map<String, Supplier<?>> objectFactories) {
        this.objectFactories = objectFactories;
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