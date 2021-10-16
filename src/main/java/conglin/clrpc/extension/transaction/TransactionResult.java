package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;

public interface TransactionResult {

    /**
     * 预提交回调
     *
     * @return
     */
    Callback callback();


    /**
     * 预提交结果
     *
     * @return
     */
    default Object result() {
        return null;
    }
}
