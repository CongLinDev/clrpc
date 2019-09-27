package conglin.clrpc.common.identifier;

public interface IdentifierGenerator{

    /**
     * 生成一个标识符
     * @return
     */
    Long generateIdentifier();

    /**
     * 根据key生成一个标识符
     * @param key
     * @return
     */
    Long generateIndentifier(String key);

    /**
     * 关闭生成器
     */
    void close();
}