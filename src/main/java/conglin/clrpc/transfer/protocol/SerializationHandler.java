package conglin.clrpc.transfer.protocol;

public interface SerializationHandler{
    /**
     * 序列化
     * @param <T>
     * @param t
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     * @param <T>
     * @param data
     * @param clazz
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}