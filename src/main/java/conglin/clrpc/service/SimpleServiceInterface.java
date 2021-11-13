package conglin.clrpc.service;

import conglin.clrpc.common.Fallback;

public class SimpleServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final Class<T> interfaceClass;

    protected final Fallback fallback;

    public SimpleServiceInterface(String name, Class<T> interfaceClass, Fallback fallback) {
        this.name = name;
        this.interfaceClass = interfaceClass;
        this.fallback = fallback;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<T> interfaceClass() {
        return interfaceClass;
    }

    @Override
    public Fallback fallback() {
        return fallback;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected String name;

        protected Class<T> interfaceClass;

        protected Fallback fallback;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> interfaceClass(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
            return this;
        }

        public Builder<T> fallback(Fallback fallback) {
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
            return new SimpleServiceInterface<>(name, interfaceClass, fallback) {
            };
        }
    }
}
