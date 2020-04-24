package conglin.clrpc.service.context.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import conglin.clrpc.service.context.ProviderContext;

final public class ProviderChannelContext extends CommonChannelContext {

    public ProviderChannelContext(ProviderContext context) {
        super(context);

        Map<String, Object> map = new HashMap<>(context.getObjectBeans());
        context.getObjectFactories().forEach((key, value) -> map.put(key, value.get()));
        objectHolder = map::get;
    }

    private final Function<String, Object> objectHolder;

    /**
     * 获取服务对象持有者
     * 
     * @return
     */
    public Function<String, Object> objectHolder() {
        return objectHolder;
    }
}