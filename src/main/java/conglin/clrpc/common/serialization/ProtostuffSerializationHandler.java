package conglin.clrpc.common.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Protostuff 序列化器
 * 
 * 使用Protostuff库进行序列化与反序列化
 */
public class ProtostuffSerializationHandler implements SerializationHandler {

    private final Map<Class<?>, Schema<?>> CACHED_SCHEMA;

    public ProtostuffSerializationHandler() {
        CACHED_SCHEMA = new ConcurrentHashMap<>();
    }

    @Override
    public <T> byte[] serialize(T t) {
        @SuppressWarnings("unchecked")
        Class<T> tClass = (Class<T>) t.getClass();
        Schema<T> schema = getSchema(tClass);
        LinkedBuffer buffer = getBuffer();
        try {
            return ProtostuffIOUtil.toByteArray(t, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 获取缓存的 Schema
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) CACHED_SCHEMA.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    /**
     * 获取可用的Buffer
     * 
     * 这里是创建新的Buffer
     * 
     * @return
     */
    protected LinkedBuffer getBuffer() {
        return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }
}