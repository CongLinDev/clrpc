package conglin.clrpc.service;

import conglin.clrpc.common.Fallback;

public class SimpleServiceInterfaceBuilder<T> {
    protected String name;

    protected Class<T> interfaceClass;

    protected Fallback fallback;

    public static <T> SimpleServiceInterfaceBuilder<T> builder() {
        return new SimpleServiceInterfaceBuilder<>();
    }

    public SimpleServiceInterfaceBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public SimpleServiceInterfaceBuilder<T> interfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }

    public SimpleServiceInterfaceBuilder<T> fallback(Fallback fallback) {
        this.fallback = fallback;
        return this;
    }

    public ServiceInterface<T> build() {
        if (interfaceClass == null) {
            throw new IllegalArgumentException();
        }
        if (name == null) {
            name = interfaceClass.getName();
        }
        return new AbstractServiceInterface<>(name, interfaceClass, fallback) {
        };
    }
}
