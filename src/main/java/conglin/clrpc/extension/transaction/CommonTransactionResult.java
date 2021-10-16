package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;

public class CommonTransactionResult implements TransactionResult {

    private static final Callback defaultCallback = new Callback() {
        @Override
        public void success(Object result) {

        }

        @Override
        public void fail(Exception exception) {

        }
    };

    private final Object result;
    private final Callback callback;

    public CommonTransactionResult(Object result, Callback callback) {
        this.result = result;
        this.callback = callback;
    }

    public CommonTransactionResult(Object result) {
        this(result, defaultCallback);
    }

    @Override
    public Callback callback() {
        return callback;
    }

    @Override
    public Object result() {
        return result;
    }
}
