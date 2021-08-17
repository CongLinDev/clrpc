package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.object.ObjectArrayHolder;

public class RpcOption extends ObjectArrayHolder<RpcOptionEnum> {
    
    public RpcOption() {
        super(RpcOptionEnum.values().length);
    }

    /**
     * 设置属性
     * 
     * @param key
     * @param value
     */
    @Override
    public void put(RpcOptionEnum key, Object value) {
        if (!key.accept(value)) {
            throw new IllegalArgumentException("unacceptable value");
        }
        super.put(key, value);;
    }


    /**
     * 获取属性
     * 
     * @param key
     * @return
     */
    public Object getOrDefault(RpcOptionEnum key) {
        Object obj = get(key);
        if(obj == null) {
            obj = key.generate();
            put(key, obj);
        }
        return obj;
    }
    
}
