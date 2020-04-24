package conglin.clrpc.common;

import java.util.function.Consumer;

/**
 * 回调接口
 */
public interface Callback {

    /**
     * 成功
     * 
     * @param result
     */
    void success(Object result);

    /**
     * 失败
     * 
     * @param exception
     */
    void fail(Exception exception);

    /**
     * 合并
     * 
     * @param after
     * @return
     */
    default Callback andThen(Callback after) {
        if(after == null)
            return this;
        Callback current = this;
        return new Callback() {

            @Override
            public void success(Object result) {
                current.success(result);
                after.success(result);
            }

            @Override
            public void fail(Exception exception) {
                current.fail(exception);
                after.fail(exception);
            }
        };
    }

    /**
     * 转换对象
     * 
     * @param task
     * @return
     */
    static Callback convert(Runnable task) {
        return new Callback() {
            @Override
            public void success(Object result) {
                task.run();
            }

            @Override
            public void fail(Exception exception) {
                task.run();
            }
        };
    }

    /**
     * 转换对象
     * 
     * @param success
     * @param fail
     * @return
     */
    static Callback convert(Consumer<Object> success, Consumer<Exception> fail) {
        return new Callback() {

            @Override
            public void success(Object result) {
                success.accept(result);
            }

            @Override
            public void fail(Exception exception) {
                fail.accept(exception);
            }
        };
    }

    /**
     * 转换对象
     * 
     * @param success
     * @return
     */
    static Callback convertSuccess(Consumer<Object> success) {
        return new Callback() {

            @Override
            public void success(Object result) {
                success.accept(result);
            }

            @Override
            public void fail(Exception exception) {

            }
        };
    }

    /**
     * 转换对象
     * 
     * @param fail
     * @return
     */
    static Callback convertFail(Consumer<Exception> fail) {
        return new Callback() {

            @Override
            public void success(Object result) {

            }

            @Override
            public void fail(Exception exception) {
                fail.accept(exception);
            }
        };
    }

}