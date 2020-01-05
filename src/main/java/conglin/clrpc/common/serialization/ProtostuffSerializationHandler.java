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
public class ProtostuffSerializationHandler implements SerializationHandler{

    private final Map<Class<?>, Schema<?>> CACHED_SCHEMA;
    
    public ProtostuffSerializationHandler(){
        CACHED_SCHEMA = new ConcurrentHashMap<>();
    }

    @Override
    public <T> byte[] serialize(T t) {
        @SuppressWarnings("unchecked")
        Class<T> tClass = (Class<T>)t.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = getSchema(tClass);
        try{
            return ProtostuffIOUtil.toByteArray(t, schema, buffer);
        }finally{
            buffer.clear();
        }        
    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> clazz){
        return (Schema<T>)CACHED_SCHEMA.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) { 
        Schema<T> schema = getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }
}