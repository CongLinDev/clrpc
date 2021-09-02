package conglin.clrpc.service;

import conglin.clrpc.common.Fallback;

public class AbstractServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final Class<T> interfaceClass;

    protected final Fallback fallback;

    public AbstractServiceInterface(String name, Class<T> interfaceClass, Fallback fallback) {
        this.name = name;
        this.interfaceClass = interfaceClass;
        this.fallback = fallback;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Class<T> interfaceClass() {
        return null;
    }

    @Override
    public Fallback fallback() {
        return null;
    }
}
