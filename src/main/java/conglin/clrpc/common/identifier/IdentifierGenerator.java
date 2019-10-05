package conglin.clrpc.common.identifier;

public interface IdentifierGenerator{

    /**
     * 生成一个标识符
     * @return
     */
    long generate();

    /**
     * 根据key生成一个标识符
     * @param key
     * @return
     */
    long generate(String key);

    /**
     * 关闭生成器
     */
    void destroy();
}