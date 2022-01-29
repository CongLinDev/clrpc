package conglin.clrpc.service.future.sync;

import java.util.concurrent.TimeUnit;

public interface StateSync {

    // if pending, retry times == Math.abs(getState())
    int PENDING = 0; // 等待
    int CANCELLED = 1; // 取消
    int DONE = 2; // 完成

    /**
     * 等待
     * 
     * @throws InterruptedException
     */
    void await() throws InterruptedException;

    /**
     * 超时等待
     * 
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 提示不再等待
     */
    void signal();

    /**
     * 当前的状态值
     * 
     * @return
     */
    int state();

    /**
     * 设置状态值
     * 
     * @param value
     */
    void state(int value);

    /**
     * CAS更新状态值
     * 
     * @param expect
     * @param update
     * @return
     */
    boolean casState(int expect, int update);

    /**
     * 是否完成
     * 
     * @return
     */
    default boolean isDone() {
        return state() >= DONE;
    }

    /**
     * 是否取消
     * 
     * @return
     */
    default boolean isCancelled() {
        return state() == CANCELLED;
    }

    /**
     * 是否等待中
     * 
     * @return
     */
    default boolean isPending() {
        return state() == PENDING;
    }

    /**
     * 取消
     */
    default boolean cancel() {
        int curState = state();
        return !isDone() && casState(curState, CANCELLED);
    }
}