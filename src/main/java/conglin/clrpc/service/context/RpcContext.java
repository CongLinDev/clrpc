package conglin.clrpc.service.context;

import conglin.clrpc.common.object.ObjectArrayHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcContext extends ObjectArrayHolder<RpcContextEnum> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectArrayHolder.class);

    public RpcContext() {
        super(RpcContextEnum.values().length);
    }

    /**
     * 设置属性
     * 
     * @param key
     * @param value
     */
    @Override
    public void put(RpcContextEnum key, Object value) {
        if (!key.accept(value)) {
            throw new IllegalArgumentException("unacceptable value");
        }
        super.put(key, value);;
    }

    @Override
    protected void putIfContains(RpcContextEnum key, Object oldValue, Object value) {
        LOGGER.warn("RpcContext key={} oldValue={} will be replaced with newValue={}", key.name(), oldValue, value);
    }

    @Override
    public Object get(RpcContextEnum key) {
        return super.get(key);
    }

}
