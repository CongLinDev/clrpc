package conglin.clrpc.common;

import java.util.function.Function;

/**
 * 回调函数对象
 */
@FunctionalInterface
public interface CallbackFunction extends Function<Boolean, Object> {

    /**
     * 转化为 {@link conglin.clrpc.common.Callback} 对象
     * 
     * @return
     */
    default Callback convert() {
        return new Callback() {
            @Override
            public void success(Object result) {
                apply(Boolean.TRUE);
            }

            @Override
            public void fail(Exception exception) {
                apply(Boolean.FALSE);
            }
        };
    }
}