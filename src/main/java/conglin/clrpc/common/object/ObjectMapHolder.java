package conglin.clrpc.common.object;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapHolder<T> implements ObjectHolder<T> {

    protected final Map<T, Object> holder;

    public ObjectMapHolder() {
        this(new HashMap<>());
    }

    public ObjectMapHolder(Map<T, Object> holder) {
        this.holder = holder;
    }

    @Override
    public void put(T key, Object value) {
        holder.put(key, value);
    }

    @Override
    public Object get(T key) {
        return holder.get(key);
    }

}
