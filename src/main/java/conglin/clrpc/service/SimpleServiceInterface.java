package conglin.clrpc.service;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.condition.InstanceCondition;

public class SimpleServiceInterface<T> implements ServiceInterface<T> {

    protected final String name;

    protected final ServiceVersion version;

    protected final Class<T> interfaceClass;

    protected final Class<? extends FailStrategy> failStrategyClass;

    protected final InstanceCondition instanceCondition;

    protected SimpleServiceInterface(String name, ServiceVersion version, Class<T> interfaceClass, Class<? extends FailStrategy> failStrategyClass, InstanceCondition instanceCondition) {
        this.name = name;
        this.version = version == null ? ServiceVersion.defaultVersion() : version;
        this.interfaceClass = interfaceClass;
        this.failStrategyClass = failStrategyClass;
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
    public Class<? extends FailStrategy> failStrategyClass() {
        return failStrategyClass;
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

        protected Class<T> interfaceClass;

        protected Class<? extends FailStrategy> failStrategyClass;

        protected Class<? extends InstanceCondition> instanceConditionClass;

        protected InstanceCondition instanceCondition;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> version(ServiceVersion version) {
            this.version = version;
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

        public Builder<T> instanceConditionClass(Class<? extends InstanceCondition> instanceConditionClass) {
            this.instanceConditionClass = instanceConditionClass;
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
            if (instanceCondition == null && instanceConditionClass != null) {
                instanceCondition = ClassUtils.loadObjectByType(instanceConditionClass, InstanceCondition.class);
                instanceCondition.setMinVersion(version);
            }
            return new SimpleServiceInterface<>(name, version, interfaceClass, failStrategyClass, instanceCondition);
        }
    }
}
