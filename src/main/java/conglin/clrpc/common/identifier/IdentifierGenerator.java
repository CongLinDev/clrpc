package conglin.clrpc.common.identifier;

import javax.security.auth.Destroyable;

public interface IdentifierGenerator extends Destroyable {

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
}