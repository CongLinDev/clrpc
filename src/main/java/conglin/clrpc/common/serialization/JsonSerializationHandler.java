package conglin.clrpc.common.serialization;

import com.alibaba.fastjson.JSON;

/**
 * JSON 序列化器
 * 
 * 使用fastjson进行序列化与反序列化
 */
public class JsonSerializationHandler implements SerializationHandler {

    @Override
    public <T> byte[] serialize(T t) {
        return JSON.toJSONBytes(t);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] data) {
        return JSON.parseObject(data, clazz);
    }
}