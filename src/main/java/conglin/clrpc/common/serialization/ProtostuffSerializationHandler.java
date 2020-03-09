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

    private final static ThreadLocal<LinkedBuffer> LOCAL_BUFFER = new ThreadLocal<>();

    private final static Map<Class<?>, Schema<?>> CACHED_SCHEMA = new ConcurrentHashMap<>();

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
     * 
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
     * 使用 {@link java.lang.ThreadLocal} 重用缓存区
     * 
     * @return
     */
    protected LinkedBuffer getBuffer() {
        LinkedBuffer buffer = LOCAL_BUFFER.get();
        if (buffer == null) {
            buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            LOCAL_BUFFER.set(buffer);
        }
        return buffer;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] data) {
        Schema<T> schema = getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] data, int offset, int length) {
        Schema<T> schema = getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, offset, length, t, schema);
        return t;
    }
}