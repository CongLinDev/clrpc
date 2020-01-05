package conglin.clrpc.global;

import java.util.concurrent.atomic.AtomicInteger;

import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * clrpc 为了性能选择复用部分资源 全局资源管理器使用计数标记的方法管理资源
 * 
 * 若你想使用这些资源，请在使用前调用 {@link GlobalResourceManager#register()} 方法
 * 若你不再使用这些资源，请在使用后调用 {@link GlobalResourceManager#unregister()} 方法
 */
public class GlobalResourceManager {

    private AtomicInteger counter;

    private GlobalResourceManager() {
        counter = new AtomicInteger();
    }

    public static GlobalResourceManager manager() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final GlobalResourceManager INSTANCE = new GlobalResourceManager();
    }

    /**
     * 向全局资源管理器注册
     * 
     * @return 当前全局资源占用情况
     */
    public int register() {
        return counter.incrementAndGet();
    }

    /**
     * 向全局资源管理器取消注册
     * 
     * @return 当前全局资源占用情况
     */
    public int unregister() {
        int currentCount = counter.decrementAndGet();
        if (currentCount == 0)
            destroy();
        return currentCount;
    }

    /**
     * 查看当前占用情况
     * 
     * @return
     */
    public int current() {
        return counter.get();
    }

    /**
     * 销毁资源
     */
    private void destroy() {
        ZooKeeperUtils.disconnectAllZooKeeper();
    }
}