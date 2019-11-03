package conglin.clrpc.common;

public interface Callback {
    /**
     * 成功
     * @param result
     */
    void success(Object result);

    /**
     * 失败
     * @param exception
     */
    void fail(Exception exception);
}