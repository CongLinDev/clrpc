package conglin.clrpc.service;

import conglin.clrpc.service.future.strategy.FailStrategy;

public class SimpleServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final Class<T> interfaceClass;

    protected final Class<? extends FailStrategy> failStrategyClass;

    public SimpleServiceInterface(String name, Class<T> interfaceClass, Class<? extends FailStrategy> failStrategyClass) {
        this.name = name;
        this.interfaceClass = interfaceClass;
        this.failStrategyClass = failStrategyClass;
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
    public Class<? extends FailStrategy> failStrategyClass() {
        return failStrategyClass;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected String name;

        protected Class<T> interfaceClass;

        protected Class<? extends FailStrategy> failStrategyClass;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> interfaceClass(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
            return this;
        }

        public Builder<T> failStrategyClass(Class<? extends FailStrategy> failStrategyClass) {
            this.failStrategyClass = failStrategyClass;
            return this;
        }

        public ServiceInterface<T> build() {
            if (interfaceClass == null) {
                throw new IllegalArgumentException();
            }
            if (name == null) {
                name = interfaceClass.getName();
            }
            return new SimpleServiceInterface<>(name, interfaceClass, failStrategyClass) {
            };
        }
    }
}
