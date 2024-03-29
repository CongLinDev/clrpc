package conglin.clrpc.invocation.identifier;

/**
 * ID 生成器
 */
public interface IdentifierGenerator {

    /**
     * 生成一个标识符
     * 
     * @return
     */
    long generate();

    /**
     * 根据 {@code key} 生成一个标识符
     * 
     * @param key
     * @return
     */
    long generate(String key);
}