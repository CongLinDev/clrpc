package conglin.clrpc.common;

public interface Callback<R> {
    /**
     * 成功
     * @param result
     */
    void success(R result);

    /**
     * 失败
     * @param exception
     */
    void fail(Exception exception);
}