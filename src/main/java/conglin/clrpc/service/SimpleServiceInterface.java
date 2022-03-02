package conglin.clrpc.service;

import conglin.clrpc.service.instance.condition.DefaultInstanceCondition;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailFast;
import conglin.clrpc.service.strategy.FailStrategy;

public class SimpleServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final ServiceVersion version;

    protected final Class<T> interfaceClass;

    protected final FailStrategy failStrategy;

    protected final InstanceCondition instanceCondition;

    protected SimpleServiceInterface(String name, ServiceVersion version, Class<T> interfaceClass,
            FailStrategy failStrategy, InstanceCondition instanceCondition) {
        this.name = name;
        this.version = version == null ? ServiceVersion.defaultVersion() : version;
        this.interfaceClass = interfaceClass;
        this.failStrategy = failStrategy;
        this.instanceCondition = instanceCondition;
    }

    @Override
    public ServiceVersion version() {
        return version;
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
    public FailStrategy failStrategy() {
        return failStrategy;
    }

    @Override
    public InstanceCondition instanceCondition() {
        return instanceCondition;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected String name;

        protected ServiceVersion version;

        protected final Class<T> interfaceClass;

        protected FailStrategy failStrategy;

        protected InstanceCondition instanceCondition;

        public Builder(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> version(ServiceVersion version) {
            this.version = version;
            return this;
        }

        public Builder<T> failStrategy(FailStrategy failStrategy) {
            this.failStrategy = failStrategy;
            return this;
        }

        public Builder<T> instanceCondition(InstanceCondition instanceCondition) {
            this.instanceCondition = instanceCondition;
            return this;
        }

        public ServiceInterface<T> build() {
            if (interfaceClass == null) {
                throw new IllegalArgumentException();
            }
            if (name == null) {
                name = interfaceClass.getName();
            }
            if (version == null) {
                version = ServiceVersion.defaultVersion();
            }
            if (instanceCondition == null) {
                instanceCondition = new DefaultInstanceCondition();
            }
            instanceCondition.currentVersion(version);
            if (failStrategy == null) {
                failStrategy = new FailFast();
            }
            return new SimpleServiceInterface<>(name, version, interfaceClass, failStrategy, instanceCondition);
        }
    }
}
