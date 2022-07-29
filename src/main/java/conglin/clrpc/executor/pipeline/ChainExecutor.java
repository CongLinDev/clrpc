package conglin.clrpc.executor.pipeline;

import conglin.clrpc.executor.Executor;

public interface ChainExecutor extends Executor {

    /**
     * 直接发送给下一个 Executor
     * 
     * @param object
     */
    void nextInbound(Object object);

    /**
     * 直接发送给下一个 Executor
     * 
     * @param object
     */
    void nextOutbound(Object object);

    /**
     * order
     * 
     * @return
     */
    default int order() {
        return 0;
    }

    /**
     * name
     * 
     * @return
     */
    default String name() {
        return getClass().getName();
    }
}
