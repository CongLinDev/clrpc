package conglin.clrpc.common.codec;

import com.alibaba.fastjson.JSON;

public class JsonSerializationHandler implements SerializationHandler {
    @Override
    public <T> byte[] serialize(T t) {
        return JSON.toJSONString(t).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

}