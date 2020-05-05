package conglin.clrpc.common.serialization;

/**
 * 序列化处理器接口
 */
public interface SerializationHandler {
    /**
     * 序列化
     * 
     * @param <T>
     * @param t
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     * 
     * @param <T>
     * @param clazz
     * @param data
     * @return
     */
    <T> T deserialize(Class<T> clazz, byte[] data);

    /**
     * 反序列化
     * 
     * @param <T>
     * @param clazz
     * @param data
     * @param offset 起始位置偏移量
     * @param length 长度
     * @return
     */
    default <T> T deserialize(Class<T> clazz, byte[] data, int offset, int length) {
        byte[] array = new byte[length];
        System.arraycopy(data, offset, array, 0, length);
        return deserialize(clazz, array);
    }
}