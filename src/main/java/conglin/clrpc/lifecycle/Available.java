package conglin.clrpc.lifecycle;

/**
 * 某些对象不是线程安全的，或是同一时间不可反复使用 该接口是为了对象重用
 */
public interface Available {
    /**
     * 对象是否可用
     * 
     * @return
     */
    default boolean isAvailable() {
        return false;
    }
}