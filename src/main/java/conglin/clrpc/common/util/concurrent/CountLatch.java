package conglin.clrpc.common.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * {@link java.util.concurrent.CountDownLatch} 的增强实现
 */
public class CountLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 7124291242240263498L;

        Sync(int count) {
            setState(count);
        }

        /**
         * 获取当前的值
         * 
         * @return
         */
        public int getCount() {
            return getState();
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return (getState() <= 0) ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                if (c <= 0)
                    return false;
                int nextc = c - releases;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    public CountLatch(int count) {
        if (count < 0)
            throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 等待
     * 
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 超时等待
     * 
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 减 {@code 1}
     * 
     * @see #countDown(int)
     */
    public void countDown() {
        countDown(1);
    }

    /**
     * 减 {@code count}
     * 
     * @param count
     */
    public void countDown(int count) {
        sync.releaseShared(count);
    }

    /**
     * 加 {@code 1}
     * 
     * @see #countUp(int)
     */
    public void countUp() {
        countUp(1);
    }

    /**
     * 加 {@code count}
     * 
     * @param count
     */
    public void countUp(int count) {
        countDown(-count);
    }

    /**
     * 当前数字
     * 
     * @return
     */
    public int getCount() {
        return sync.getCount();
    }

    private boolean isClear; // 记录是否是被强制清空

    /**
     * 清空数字
     */
    public void clear() {
        isClear = true;
        sync.releaseShared(getCount());
    }

    /**
     * 是否通过 {@link #clear()} 方法清空
     * 
     * @return
     */
    public boolean isClear() {
        return isClear;
    }

    @Override
    public String toString() {
        return "[Count = " + sync.getCount() + "]";
    }
}