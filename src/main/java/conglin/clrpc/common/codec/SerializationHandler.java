package conglin.clrpc.common.codec;

/**
 * 实现了该接口的序列化处理器的类
 * 必须添加 getInstance() 静态方法
 */
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