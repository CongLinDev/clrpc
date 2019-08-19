package conglin.clrpc.common.codec.protostuff;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import conglin.clrpc.common.codec.SerializationHandler;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializationHandler implements SerializationHandler{

    private final Map<Class<?>, Schema<?>> cachedSchema;
    
    private ProtostuffSerializationHandler(){
        cachedSchema = new ConcurrentHashMap<>();
    }

    public static SerializationHandler getInstance() {
        return SingletonHolder.SERIALIZATION_HANDLER;
    }

    private static class SingletonHolder {
        private static final SerializationHandler SERIALIZATION_HANDLER = new ProtostuffSerializationHandler();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T t) {
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
        return (Schema<T>)cachedSchema.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) { 
        Schema<T> schema = getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }
}