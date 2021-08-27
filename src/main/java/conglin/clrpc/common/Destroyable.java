package conglin.clrpc.common;

import conglin.clrpc.common.exception.DestroyFailedException;

public interface Destroyable {
    /**
     * 销毁
     * 
     * @throws DestroyFailedException
     */
    void destroy() throws DestroyFailedException;

    /**
     * 是否销毁
     * 
     * @return
     */
    default boolean isDestroyed() {
        return false;
    }
}