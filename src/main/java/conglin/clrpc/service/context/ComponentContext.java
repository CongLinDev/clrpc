package conglin.clrpc.service.context;

import conglin.clrpc.common.object.ObjectArrayHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentContext extends ObjectArrayHolder<ComponentContextEnum> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectArrayHolder.class);

    public ComponentContext() {
        super(ComponentContextEnum.values().length);
    }

    /**
     * 设置属性
     * 
     * @param key
     * @param value
     */
    @Override
    public void put(ComponentContextEnum key, Object value) {
        if (!key.accept(value)) {
            throw new IllegalArgumentException("unacceptable value");
        }
        super.put(key, value);
    }

    @Override
    protected void putIfContains(ComponentContextEnum key, Object oldValue, Object value) {
        LOGGER.warn("ComponentContext key={} oldValue={} will be replaced with newValue={}", key.name(), oldValue, value);
        doPut(key, value);
    }
}
