package conglin.clrpc.service;

import conglin.clrpc.invocation.strategy.FailFast;
import conglin.clrpc.invocation.strategy.FailStrategy;
import conglin.clrpc.service.instance.condition.DefaultInstanceCondition;
import conglin.clrpc.service.instance.condition.InstanceCondition;

public class SimpleServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final ServiceVersion version;

    protected final Class<T> interfaceClass;

    protected final FailStrategy failStrategy;

    protected final InstanceCondition instanceCondition;

    protected final long timeoutThreshold;

    protected SimpleServiceInterface(String name, ServiceVersion version, Class<T> interfaceClass,
            long timeoutThreshold, FailStrategy failStrategy, InstanceCondition instanceCondition) {
        this.name = name;
        this.version = version == null ? ServiceVersion.defaultVersion() : version;
        this.timeoutThreshold = timeoutThreshold;
        this.interfaceClass = interfaceClass;
        this.failStrategy = failStrategy;
        this.instanceCondition = instanceCondition;
        this.instanceCondition.bindServiceInterface(this);
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

    @Override
    public long timeoutThreshold() {
        return timeoutThreshold;
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

        protected long timeoutThreshold;

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

        public Builder<T> timeoutThreshold(long timeoutThreshold) {
            this.timeoutThreshold = timeoutThreshold;
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
            if (failStrategy == null) {
                failStrategy = new FailFast();
            }
            return new SimpleServiceInterface<>(name, version, interfaceClass, timeoutThreshold, failStrategy, instanceCondition);
        }
    }
}
