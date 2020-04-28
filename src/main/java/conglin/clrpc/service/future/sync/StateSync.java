package conglin.clrpc.service.future.sync;

import java.util.concurrent.TimeUnit;

public interface StateSync {

    // if pending, retry times == Math.abs(getState())
    int PENDING = 0; // 等待
    int CANCELLED = 1; // 取消
    int DONE = 2; // 完成
    int USED = 3; // 占用

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
     * 是否被占用
     * 
     * @return
     */
    default boolean isUsed() {
        return state() == USED;
    }

    /**
     * 是否等待中
     * 
     * @return
     */
    default boolean isPending() {
        return state() < CANCELLED;
    }

    /**
     * 取消
     */
    default boolean cancel() {
        int curState = state();
        return curState < DONE && casState(curState, CANCELLED);
    }

    /**
     * 重试
     * 
     * @return 是否重试成功
     */
    default boolean retry() {
        int curState = state();
        if (curState > PENDING)
            return false;
        return casState(curState, curState - 1);
    }

    /**
     * 已经重试的次数
     * 
     * @return
     */
    default int retryTimes() {
        return Math.max(-state(), 0);
    }
}