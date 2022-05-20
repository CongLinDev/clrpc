package conglin.clrpc.extension.transaction.context;

import java.util.ArrayList;
import java.util.List;

import conglin.clrpc.extension.transaction.TransactionState;
import conglin.clrpc.extension.transaction.future.TransactionFuture;
import conglin.clrpc.service.context.InvocationContext;

public class TransactionInvocationContext {
    private final long identifier; // init in sender
    private final List<InvocationContext> invocationContextList;
    private final TransactionFuture future;
    private TransactionState state;

    public TransactionInvocationContext(long identifier) {
        this.identifier = identifier;
        invocationContextList = new ArrayList<>();
        future = new TransactionFuture();
        state = TransactionState.PREPARE;
    }
    /**
     * @return the invocationContextList
     */
    public List<InvocationContext> getInvocationContextList() {
        return invocationContextList;
    }

    /**
     * @return the future
     */
    public TransactionFuture getFuture() {
        return future;
    }

    /**
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }
    /**
     * @return the state
     */
    public TransactionState getState() {
        return state;
    }
    /**
     * @param state the state to set
     */
    public void commit() {
        if (getState() == TransactionState.PREPARE) {
            state = TransactionState.COMMIT;
            future.combineDone();
        }
    }
    /**
     * @param state the state to set
     */
    public void abort() {
        if (getState() == TransactionState.PREPARE) {
            state = TransactionState.ABORT;
            future.combineDone();
        }
    }
}
