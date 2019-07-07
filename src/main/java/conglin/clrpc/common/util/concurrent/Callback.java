package conglin.clrpc.common.util.concurrent;

/**
 * 回调接口
 */
public interface Callback{
    /**
     * 成功
     * @param result
     */
    void success(Object result);

    /**
     * 失败
     * @param e
     */
    void fail(Exception e);
}